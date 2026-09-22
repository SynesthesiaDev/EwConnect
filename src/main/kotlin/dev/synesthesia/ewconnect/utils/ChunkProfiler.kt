package dev.synesthesia.ewconnect.utils

import dev.synesthesia.ewconnect.ChatUtils
import net.minecraft.world.level.ChunkPos
import org.eclipse.collections.impl.map.mutable.ConcurrentHashMap

object ChunkProfiler {

    private val chunkDataCounts = ConcurrentHashMap<ChunkPos, Long>()
    private var totalBytesRecorded: Long = 0
    private var totalPacketsRecorded: Long = 0

    private const val HEAVY_CHUNK_THRESHOLD_BYTES = 100_000
    
    fun push(chunk: ChunkPos, bytes: Int) {
        if(bytes <= 0) return

        chunkDataCounts[chunk] = bytes.toLong()
        
        totalBytesRecorded += bytes
        totalPacketsRecorded++
        
        if (bytes > HEAVY_CHUNK_THRESHOLD_BYTES) {
            val kb = bytes / 1024
            ChatUtils.sendMessage("<red>Heavy chunk at [${chunk.x}, ${chunk.z}] sent a ${kb} KB packet!")
        }
    }

    fun getTopHeavyChunks(limit: Int = 5): List<MutableMap.MutableEntry<ChunkPos, Long>> {
        return chunkDataCounts.entries
            .sortedByDescending { it.value }
            .take(limit)
            .toList()
    }

    fun reset() {
        chunkDataCounts.clear()
        totalBytesRecorded = 0
        totalPacketsRecorded = 0
    }
}