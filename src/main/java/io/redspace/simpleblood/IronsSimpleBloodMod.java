package io.redspace.simpleblood;

import com.mojang.logging.LogUtils;
import io.redspace.simpleblood.client.ClientConfig;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import io.redspace.simpleblood.client.ClientEvents;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;

@Mod(IronsSimpleBloodMod.MODID)
public class IronsSimpleBloodMod {
    public static final String MODID = "simpleblood";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsSimpleBloodMod(IEventBus modEventBus, ModContainer modContainer) {
        ParticleRegistry.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, String.format("%s-client.toml", IronsSimpleBloodMod.MODID));
    }
}
