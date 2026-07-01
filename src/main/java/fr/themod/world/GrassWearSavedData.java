package fr.themod.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import fr.themod.TheMod;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class GrassWearSavedData extends SavedData {
    private static final Codec<PressureEntry> PRESSURE_ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("pos").forGetter(PressureEntry::pos),
            Codec.INT.fieldOf("value").forGetter(PressureEntry::value),
            Codec.LONG.optionalFieldOf("last_touched", 0L).forGetter(PressureEntry::lastTouchedGameTime)
    ).apply(instance, PressureEntry::new));

    public static final Codec<GrassWearSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PRESSURE_ENTRY_CODEC.listOf().optionalFieldOf("pressure", List.of()).forGetter(GrassWearSavedData::entries)
    ).apply(instance, GrassWearSavedData::new));

    public static final SavedDataType<GrassWearSavedData> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath(TheMod.MODID, "grass_wear_pressure"),
            GrassWearSavedData::new,
            CODEC
    );

    private final Map<Long, WearPressure> pressureByPos = new HashMap<>();

    public GrassWearSavedData() {
    }

    private GrassWearSavedData(List<PressureEntry> entries) {
        for (PressureEntry entry : entries) {
            if (entry.value() > 0 || entry.lastTouchedGameTime() > 0) {
                pressureByPos.put(entry.pos(), new WearPressure(entry.value(), entry.lastTouchedGameTime()));
            }
        }
    }

    public int addPressure(BlockPos pos, int amount, long gameTime) {
        WearPressure pressure = pressureByPos.computeIfAbsent(pos.asLong(), ignored -> new WearPressure(0, gameTime));
        pressure.value += amount;
        pressure.lastTouchedGameTime = gameTime;
        setDirty();
        return pressure.value;
    }

    public void resetPressure(BlockPos pos, long gameTime) {
        pressureByPos.put(pos.asLong(), new WearPressure(0, gameTime));
        setDirty();
    }

    public void removePressure(BlockPos pos) {
        if (pressureByPos.remove(pos.asLong()) != null) {
            setDirty();
        }
    }

    public int pressureAt(BlockPos pos) {
        WearPressure pressure = pressureByPos.get(pos.asLong());
        return pressure == null ? 0 : pressure.value;
    }

    public boolean wasTouchedRecently(BlockPos pos, long gameTime, long recentWindowTicks) {
        WearPressure pressure = pressureByPos.get(pos.asLong());
        return pressure != null && gameTime - pressure.lastTouchedGameTime < recentWindowTicks;
    }

    public void trim(int maxEntries, int removeCount) {
        if (pressureByPos.size() <= maxEntries) {
            return;
        }

        Iterator<Long> iterator = pressureByPos.keySet().iterator();
        int removed = 0;

        while (iterator.hasNext() && removed < removeCount) {
            iterator.next();
            iterator.remove();
            removed++;
        }

        if (removed > 0) {
            setDirty();
        }
    }

    // Zero-pressure entries still matter because lastTouched slows regrowth after reloads.
    private List<PressureEntry> entries() {
        return pressureByPos.entrySet().stream()
                .filter(entry -> entry.getValue().value > 0 || entry.getValue().lastTouchedGameTime > 0)
                .map(entry -> new PressureEntry(entry.getKey(), entry.getValue().value, entry.getValue().lastTouchedGameTime))
                .toList();
    }

    private record PressureEntry(long pos, int value, long lastTouchedGameTime) {
    }

    private static final class WearPressure {
        private int value;
        private long lastTouchedGameTime;

        private WearPressure(int value, long lastTouchedGameTime) {
            this.value = value;
            this.lastTouchedGameTime = lastTouchedGameTime;
        }
    }
}
