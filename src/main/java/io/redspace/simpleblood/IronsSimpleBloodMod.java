package io.redspace.simpleblood;

import com.mojang.logging.LogUtils;
import io.redspace.simpleblood.client.ClientConfig;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(IronsSimpleBloodMod.MODID)
public class IronsSimpleBloodMod {
    public static final String MODID = "simpleblood";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsSimpleBloodMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ParticleRegistry.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, String.format("%s-client.toml", IronsSimpleBloodMod.MODID));
    }
}
