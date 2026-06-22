package dev.synesthesia.ewconnect.utils

import net.minecraft.server.MinecraftServer
import java.util.concurrent.CopyOnWriteArrayList

object FabricScheduler {
    private val tasks = CopyOnWriteArrayList<ScheduledTask>()

    private class ScheduledTask(
        val delayTicks: Int,
        val periodTicks: Int,
        val action: () -> Boolean,
        var currentDelay: Int = delayTicks
    )

    fun runLater(delayTicks: Int, action: () -> Unit) {
        tasks.add(ScheduledTask(delayTicks, -1, { action(); false }))
    }

    fun runRepeating(delayTicks: Int, periodTicks: Int, action: () -> Boolean) {
        tasks.add(ScheduledTask(delayTicks, periodTicks, action))
    }

    fun tick(server: MinecraftServer) {
        if (tasks.isEmpty()) return

        val iterator = tasks.iterator()
        while (iterator.hasNext()) {
            val task = iterator.next()

            if (task.currentDelay > 0) {
                task.currentDelay--
                continue
            }

            val keepRunning = task.action()

            if (task.periodTicks > 0 && keepRunning) {
                task.currentDelay = task.periodTicks
            } else {
                tasks.remove(task)
            }
        }
    }

    fun repeatWithDelay(times: Int, delayTicks: Int, action: (index: Int) -> Unit) {
        if (times <= 0) return

        var currentIteration = 0

        runRepeating(delayTicks, delayTicks) {
            action(currentIteration)
            currentIteration++

            currentIteration < times
        }
    }
}