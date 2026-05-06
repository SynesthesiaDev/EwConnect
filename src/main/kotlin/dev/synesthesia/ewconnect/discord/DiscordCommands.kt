package dev.synesthesia.ewconnect.discord

import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.messages.Embed
import dev.synesthesia.ewconnect.EwConnect
import dev.synesthesia.ewconnect.extensions.formattedNickname
import dev.synesthesia.ewconnect.extensions.nickname
import dev.synesthesia.ewconnect.format
import me.lucko.spark.api.statistic.StatisticWindow


class DiscordCommands(val bot: DiscordBot) {
    init {
        val jda = bot.jda

        bot.guild?.upsertCommand("list", "list users on the ew smp")?.queue()
        bot.guild?.upsertCommand("status", "gets the ew smp server status")?.queue()

        jda.onCommand("list") { event ->
            if (event.guild?.idLong != bot.settings.guildId) return@onCommand

            val players = EwConnect.server.playerList.players

            val embed = Embed {
                title = "🎮 Online Players (${players.size})"
                color = 0x5865F2

                if (players.isEmpty()) {
                    description = "_The server is currently a ghost town... sadge_"
                } else {
                    val randomPlayer = players.random()
                    thumbnail = "https://mc-heads.net/avatar/${randomPlayer.uuid}/100"

                    description = players.joinToString("\n") { player ->
                        val ping = player.connection.latency()
                        "> ${player.formattedNickname} `${ping}ms`"
                    }
                }
            }

            event.replyEmbeds(embed).queue()
        }

        jda.onCommand("status") { event ->
            if (event.guild?.idLong != bot.settings.guildId) return@onCommand

            val spark = EwConnect.spark
            val tps = spark.tps() ?: return@onCommand
            val mspt = spark.mspt() ?: return@onCommand
            val cpuUsage = spark.cpuSystem()
            val gc = spark.gc()

            val t5s = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_5)
            val t10s = tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10)
            val t1m = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_1)
            val t15m = tps.poll(StatisticWindow.TicksPerSecond.MINUTES_15)

            val msptMin = mspt.poll(StatisticWindow.MillisPerTick.MINUTES_1)
            val cpuLastMin = cpuUsage.poll(StatisticWindow.CpuUsage.MINUTES_1) * 100

            val allLevels = EwConnect.server.allLevels.toList()
            val totalEntities = allLevels.sumOf { it.level.allEntities.count() }
            val totalChunks = allLevels.sumOf { it.chunkSource.loadedChunksCount }
            
            val embed = Embed {
                title = "🖥️ Server Status"
                color = if (t1m >= 18.0) 0x57F287 else 0xED4245

                field {
                    name = "📈 Ticks Per Second"
                    value = """
                ```kotlin
                5s  : ${t5s.format()}
                10s : ${t10s.format()}
                1m  : ${t1m.format()}
                15m : ${t15m.format()}
                ```
            """.trimIndent()
                    inline = false
                }

                field {
                    name = "⏱️ MSPT"
                    value = "Median: `${msptMin.median().format()}ms`\n95th %: `${msptMin.percentile95th().format()}ms`\nMax: `${msptMin.max().format()}ms`"
                    inline = true
                }

                field {
                    name = "⚙️ System"
                    value = "CPU: `${cpuLastMin.format()}%`"
                    inline = true
                }

                if (gc.values.isNotEmpty()) {
                    field {
                        name = "🧹 Garbage Collection"
                        value = gc.values.joinToString("\n") { collector ->
                            "${collector.name()}: `${collector.avgTime().format()}ms` (${collector.avgFrequency().toDouble().format()} avg)"
                        }
                        inline = false
                    }
                }

                field {
                    name = "🌍 World Stats"
                    value = """
                Loaded Entities: `$totalEntities`
                Loaded Chunks: `$totalChunks`
            """.trimIndent()
                }
                
                footer {
                    name = "im so gay for women"
                }
            }

            event.replyEmbeds(embed).queue()
        }
    }
}