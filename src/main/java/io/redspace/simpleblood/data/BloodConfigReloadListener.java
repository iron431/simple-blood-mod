package io.redspace.simpleblood.data;

import io.redspace.simpleblood.IronsSimpleBloodMod;
import io.redspace.simpleblood.registry.BloodRegistry;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;

import java.util.List;
import java.util.Map;

@EventBusSubscriber
public class BloodConfigReloadListener extends SimpleJsonResourceReloadListener<BloodConfig> {
    public static final String DIRECTORY = "blood_types";
    public static final Identifier ID = Identifier.fromNamespaceAndPath(IronsSimpleBloodMod.MODID, DIRECTORY);

    public BloodConfigReloadListener() {
        super(BloodConfig.CODEC, FileToIdConverter.json(DIRECTORY));
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(ID, new BloodConfigReloadListener());
    }

    @Override
    protected void apply(Map<Identifier, BloodConfig> resources, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<BloodConfig> configs = List.copyOf(resources.values());
        BloodRegistry.load(configs);
        IronsSimpleBloodMod.LOGGER.info("Loaded {} blood type(s)", configs.size());
    }
}
