package dev.synesthesia.ewconnect.entities

import dev.synesthesia.ewconnect.IDisposable
import dev.synesthesia.ewconnect.utils.FabricScheduler
import net.minecraft.world.entity.decoration.ArmorStand

class HologramManager : IDisposable {

    private val holograms: MutableMap<String, HologramEntity> = mutableMapOf()
    
    fun isActiveArmorStand(armorStand: ArmorStand): Boolean = holograms.any { it.value.isActiveArmorStand(armorStand) }
    
    fun getById(id: String): HologramEntity? = holograms[id]
    
    init {
        var currentSecond: Long = 0
        FabricScheduler.runRepeating(0, 20) {
            if(currentSecond == 3600L) {
                currentSecond = 0
            }
            
            currentSecond++
            
            var updatable = holograms.filter { (_, holo) ->
                val interval = holo.updateSeconds
                interval != null && interval > 0 && currentSecond % interval == 0L
            }
            
            updatable.values.forEach(HologramEntity::update)
            return@runRepeating true
        }
    }
    
    
    fun create(id: String, lambda: HologramEntity.Builder.() -> Unit): HologramEntity {
        throwIfExists(id)

        val builder = HologramEntity.Builder()
        lambda.invoke(builder)
        
        val entity = builder.build()
        holograms[id] = entity
        
        entity.create()
        
        return entity
    }
    
    fun remove(id: String) {
        val holo = holograms[id] ?: return
        
        holo.dispose()
        holograms.remove(id)
    }
    
    private fun throwIfExists(id: String) {
        if(holograms.containsKey(id)) throw Exception("Hologram with the same id already exists!")
    }

    override fun dispose() {
        holograms.forEach { (_, entity) -> entity.dispose() }
        holograms.clear()
    }

}