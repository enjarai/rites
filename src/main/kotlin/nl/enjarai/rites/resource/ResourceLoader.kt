package nl.enjarai.rites.resource

import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceType

object ResourceLoader {
    fun register() {
        val manager = ResourceManagerHelper.get(ResourceType.SERVER_DATA)
        manager.registerReloadListener(CircleTypes)
        manager.registerReloadListener(Rituals)
    }
}