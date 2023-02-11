package nl.enjarai.rites

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer
import net.minecraft.util.Identifier
import nl.enjarai.rites.block.ModBlocks
import nl.enjarai.rites.item.ModItems
import nl.enjarai.rites.resource.ResourceLoader
import nl.enjarai.rites.type.ritual_effect.RitualEffect
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object RitesMod : ModInitializer {
    val MODID = "rites"
    val LOGGER: Logger = LoggerFactory.getLogger(MODID)

    lateinit var SERVER: MinecraftServer

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting)

        ModBlocks.register()
        ModItems.register()
        RitualEffect.registerAll()
        ResourceLoader.register()
        Commands.register()

        LOGGER.info("Rites loaded")
    }

    private fun onServerStarting(server: MinecraftServer) {
        SERVER = server
    }

    fun id(name: String): Identifier {
        return Identifier(MODID, name)
    }
}