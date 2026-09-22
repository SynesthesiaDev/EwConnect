package dev.synesthesia.ewconnect.event.paige

import dev.synesthesia.ewconnect.extensions.toNMSComponent
import dev.synesthesia.ewconnect.extensions.translated
import dev.synesthesia.ewconnect.mixin.MannequinAccessor
import dev.synesthesia.ewconnect.utils.Location
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.decoration.Mannequin
import net.minecraft.world.item.component.ResolvableProfile
import net.minecraft.world.scores.Team
import org.joml.Vector2f

object BirthdayNpcs {
    
    val MESSAGES: MutableMap<String, NpcData> = mutableMapOf(
        ("Stelle" to NpcData("Stelle", "HAPPY BIRTHDAY PAIPAI, I LOVE YOU, KISSES!!!", "affixall")),
        ("Zara" to NpcData("Zara", "hai cutiepie paigeypie!!! ur getting OLDDDD ur a HAGGGGGG omg... im joking BEFORE u kill me! i hope u have a lovely birthday and that this year goez amazingly well for u and every other year to come treats u well cause u deserve it!! love u lots my friend of millions of years it feels like probably! baiiii", "glorpkitty")),
        ("Lolo" to NpcData("Lolo", "Happyyyy birthday Paige ur the goat :3", "Lazagna696")),
        ("jayjay" to NpcData("jayjay", "paige endings are unfortunately suspended for today... happy birthday girlie <3 you get prettier and sweetier everyday", "zecarioco")),
        ("elle" to NpcData("elle", "happy birthday, my yuriful paige! i'm sorry, i'm bad at writing messages, but always know that this message comes from the bottom of my heart. you're one of the best people i've ever met, and i'm really happy that you're my friend!  i hope you enjoy your day today! we love love love love you so much <3", "_m1ya")),
        ("AJ" to NpcData("AJ", "Happy birthday Paige I hope your having an amazing day", "Josh4amd")),
        ("em" to NpcData("em", "happy birthday paigie pie!! i hope you get the yummiest cake ever ♡( >0< )", "Nyfaaaaa")),
        ("Skelly" to NpcData("Skelly", "Happy birthday miss poopy!", "lucia69")),
        ("Maya" to NpcData("Maya", "happy birthdayyyyyyyyyyy!! hope you enjoy what I cooked up", "MayanoTopgun_")),
        ("J" to NpcData("J", "Happy Birthday Paige you’re as old and graceful as bina now", "Welcomeppls"))
    )
    
    fun spawnNpc(location: Location, data: NpcData, rotation: Vector2f) {
        val level = location.serverLevel
        val npc = Mannequin(EntityTypes.MANNEQUIN, level)
        val accessor = npc as MannequinAccessor;
        npc.teleportTo(level, location.x, location.y, location.z, emptySet(), rotation.x, rotation.y, false)

        val profile = ResolvableProfile.createUnresolved(data.skin)
        accessor.invokeSetProfile(profile)

        npc.addTag(data.name)
        npc.customName = data.name.translated().toNMSComponent()
        npc.isCustomNameVisible = true;
        npc.isPermanentlyInvulnerable = true;
        npc.speed = 0f;
        
        level.addFreshEntity(npc)
    }
    
    data class NpcData(val name: String, val message: String, val skin: String);
}