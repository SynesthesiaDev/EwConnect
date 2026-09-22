package dev.synesthesia.ewconnect.utils

import dev.synesthesia.ewconnect.EwConnect
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.network.chat.numbers.StyledFormat
import net.minecraft.server.level.ServerPlayer
import net.minecraft.stats.Stats
import net.minecraft.world.scores.DisplaySlot
import net.minecraft.world.scores.criteria.ObjectiveCriteria

object DeathCounter {
    
    fun update(player: ServerPlayer) {

        val deaths = player.stats.getValue(Stats.CUSTOM.get(Stats.DEATHS))
        val scoreboard = EwConnect.server.scoreboard
        var objective = scoreboard.getObjective("death_count")
        if (objective == null) {
            objective = scoreboard.addObjective(
                "death_count",
                ObjectiveCriteria.DUMMY,
                Component.literal("Deaths"),
                ObjectiveCriteria.RenderType.INTEGER,
                true,
                StyledFormat(Style.EMPTY.withColor(TextColor.RED))
            )
        }

        scoreboard.setDisplayObjective(DisplaySlot.LIST, objective)
        val score = scoreboard.getOrCreatePlayerScore(player, objective)
        score.set(deaths)
    }
    
}