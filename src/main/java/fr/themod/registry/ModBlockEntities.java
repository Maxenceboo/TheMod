package fr.themod.registry;

import fr.themod.TheMod;
import fr.themod.block.entity.PatternReplacerBlockEntity;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, TheMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PatternReplacerBlockEntity>> PATTERN_REPLACER =
            BLOCK_ENTITIES.register(
                    "pattern_replacer",
                    () -> new BlockEntityType<>(
                            PatternReplacerBlockEntity::new,
                            Set.of(ModBlocks.PATTERN_REPLACER.get())
                    )
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}