package dev.synesthesia.ewconnect.event.halloween

import dev.synesthesia.ewconnect.ChatUtils
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.extensions.playSound
import dev.synesthesia.ewconnect.extensions.send
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.kyori.adventure.title.Title
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.projectile.FishingHook
import java.time.Duration
import java.util.UUID
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

object FishingMinigame {

    @JvmField
    var enabled = true

    const val BAR_LENGTH = 21
    const val TIME_LIMIT_TICKS = 3 * 20
    const val LENIENCY_CELLS = 1
    const val RENDER_INTERVAL_TICKS = 1
    const val ROUND_PAUSE_TICKS = 15
    const val MIN_ZONE_SIZE = 2
    const val TITLE_STAY_TICKS = 10
    const val TITLE_FADE_OUT_TICKS = 4

    enum class Difficulty(
        val weight: Int,
        val zoneSize: Int,
        val speed: Double,
        val color: String,
        val label: String,
        val minRounds: Int = 1,
        val maxRounds: Int = 1,
        val speedPerRound: Double = 0.0,
        val zoneShrinkPerRound: Int = 0,
        val missesAllowed: Int = 0
    ) {
        COMMON(60, 6, 0.55, "<gray>", "Common", minRounds = 1, maxRounds = 2),
        UNCOMMON(25, 5, 0.70, "<green>", "Uncommon", minRounds =2, maxRounds = 2),
        RARE(12, 4, 0.85, "<aqua>", "Rare", minRounds = 2, maxRounds = 3),
        LEGENDARY(3, 3, 1.00, "<gold>", "Legendary", minRounds = 3, maxRounds = 3)
    }

    class FishingSession(
        val player: ServerPlayer,
        val hook: FishingHook,
        val difficulty: Difficulty,
        val totalRounds: Int
    ) {
        var round = 1
        var misses = 0
        var perfectRounds = 0

        var roundStartTick = 0
        var speed = 0.0
        var offset = 0.0
        var zoneStart = 0
        var zoneSize = 0
        var lastPos = 0.0

        var pauseUntilTick = -1
        val paused get() = pauseUntilTick >= 0
    }

    private val sessions = HashMap<UUID, FishingSession>()
    private var registered = false

    fun init() {
        if (registered) return
        registered = true
        ServerTickEvents.END_SERVER_TICK.register { server -> tick(server) }
    }

    @JvmStatic
    fun onHookTick(hook: FishingHook, nibbling: Boolean): Boolean {
        if (!enabled) return false
        val player = hook.playerOwner as? ServerPlayer ?: return false

        val existing = sessions[player.uuid]
        if (existing != null) return existing.hook === hook

        if (!nibbling) return false
        start(player, hook)
        return true
    }

    fun start(player: ServerPlayer, hook: FishingHook) {
        val difficulty = Difficulty.entries.toTypedArray().random()
        val rounds = Random.nextInt(difficulty.minRounds, difficulty.maxRounds + 1)

        val session = FishingSession(player, hook, difficulty, rounds)
        sessions[player.uuid] = session
        beginRound(session, EwConnect.server.tickCount)

        //TODO play sound
    }

    private fun beginRound(session: FishingSession, now: Int) {
        val d = session.difficulty
        val roundsIn = session.round - 1

        session.roundStartTick = now
        session.pauseUntilTick = -1
        session.speed = d.speed * (1.0 + d.speedPerRound * roundsIn)
        session.zoneSize = (d.zoneSize - d.zoneShrinkPerRound * roundsIn).coerceIn(MIN_ZONE_SIZE, BAR_LENGTH)
        session.zoneStart = Random.nextInt(0, BAR_LENGTH - session.zoneSize + 1)
        session.offset = Random.nextDouble(0.0, 2.0 * (BAR_LENGTH - 1))
        session.lastPos = positionAt(session, now)
    }

    fun tick(server: MinecraftServer) {
        if (sessions.isEmpty()) return

        val now = server.tickCount

        sessions.values.toList().forEach { session ->
            val player = session.player

            if (player.isRemoved || !player.isAlive || session.hook.isRemoved) {
                sessions.remove(player.uuid)
                return@forEach
            }

            if (session.paused) {
                if (now >= session.pauseUntilTick) beginRound(session, now)
                return@forEach
            }

            val elapsed = now - session.roundStartTick

            if (elapsed >= TIME_LIMIT_TICKS) {
                onMiss(session, now, timedOut = true)
                return@forEach
            }

            if (elapsed % RENDER_INTERVAL_TICKS == 0) {
                session.lastPos = positionAt(session, now)
                player.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 0.5f, 2f, SoundSource.MASTER)
                render(session)
            }
        }
    }

    @JvmStatic
    fun onReel(hook: FishingHook): Boolean {
        if (!enabled) return false

        val player = hook.playerOwner as? ServerPlayer ?: return false
        val session = sessions[player.uuid] ?: return false

        if (session.hook !== hook) return false

        handleClick(session)
        return true
    }

    private fun handleClick(session: FishingSession) {
        if (session.paused) return

        val now = EwConnect.server.tickCount
        val player = session.player

        val cell = cellOf(session.lastPos)
        val zoneEnd = session.zoneStart + session.zoneSize - 1
        val center = session.zoneStart + (session.zoneSize - 1) / 2.0
        val hit = cell in (session.zoneStart - LENIENCY_CELLS)..(zoneEnd + LENIENCY_CELLS)
        val perfect = hit && abs(cell - center) <= 0.5

        if (!hit) {
            onMiss(session, now, timedOut = false)
            return
        }

        if (perfect) session.perfectRounds++

        if (session.round < session.totalRounds) {
            session.round++
            session.pauseUntilTick = now + ROUND_PAUSE_TICKS
            showMessage(player, "", "<gray>Round ${session.round}/${session.totalRounds}...", ROUND_PAUSE_TICKS)
            //TODO play sound (round cleared)
            return
        }

        finish(session)
    }

    private fun onMiss(session: FishingSession, now: Int, timedOut: Boolean) {
        val player = session.player

        if (session.misses < session.difficulty.missesAllowed) {
            session.misses++
            session.pauseUntilTick = now + ROUND_PAUSE_TICKS
            val left = session.difficulty.missesAllowed - session.misses
            showMessage(player, "<red><bold>Missed!", "<gray>It fights back... ($left chance(s) left)", ROUND_PAUSE_TICKS)
            return
        }

        sessions.remove(player.uuid)
        session.hook.discard()
        player.clearTitle()

        if (timedOut) {
            player.send("<red>you fucked up fish got away")
        } else {
            player.send("<red>fis got away")
        }
    }

    private fun finish(session: FishingSession) {
        val player = session.player
        sessions.remove(player.uuid)

        val allPerfect = session.perfectRounds == session.totalRounds

        session.hook.discard()
        player.clearTitle()

        player.send("<green> caught fih")
        //TODO on catch probably (session.difficulty, allPerfect, session.misses are all available here)
    }

    fun positionAt(session: FishingSession, tick: Int): Double {
        val max = (BAR_LENGTH - 1).toDouble()
        val period = max * 2
        val raw = (session.offset + session.speed * (tick - session.roundStartTick)) % period
        return if (raw <= max) raw else period - raw
    }

    private fun cellOf(pos: Double) = pos.roundToInt().coerceIn(0, BAR_LENGTH - 1)

    private fun ticks(t: Int): Duration = Duration.ofMillis(t * 50L)

    fun render(session: FishingSession) {
        val cursor = cellOf(session.lastPos)
        val zone = session.zoneStart until (session.zoneStart + session.zoneSize)

        val bar = buildString {
            append("<dark_gray>[")
            for (i in 0 until BAR_LENGTH) {
                append(
                    when (i) {
                        cursor if i in zone -> "<white>█"
                        cursor -> "<gray>█"
                        in zone -> "<green>█"
                        else -> "<dark_gray>█"
                    }
                )
            }
            append("<dark_gray>]")
        }

        session.player.showTitle(
            Title.title(
                ChatUtils.translated(""),
                ChatUtils.translated(bar),
                Title.Times.times(Duration.ZERO, ticks(TITLE_STAY_TICKS), ticks(TITLE_FADE_OUT_TICKS))
            )
        )
    }

    private fun showMessage(player: ServerPlayer, title: String, subtitle: String, stayTicks: Int) {
        player.showTitle(
            Title.title(
                ChatUtils.translated(title),
                ChatUtils.translated(subtitle),
                Title.Times.times(Duration.ZERO, ticks(stayTicks), ticks(TITLE_FADE_OUT_TICKS))
            )
        )
    }
}