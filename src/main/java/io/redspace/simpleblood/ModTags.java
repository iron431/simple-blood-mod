package io.redspace.simpleblood;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class ModTags {
    public static final TagKey<EntityType<?>> BLEEDS = TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(IronsSimpleBloodMod.MODID, "bleeds"));
}
