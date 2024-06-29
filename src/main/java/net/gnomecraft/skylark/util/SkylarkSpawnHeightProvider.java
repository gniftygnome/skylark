package net.gnomecraft.skylark.util;

import com.mojang.serialization.MapCodec;
import net.gnomecraft.skylark.Skylark;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.HeightContext;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.heightprovider.HeightProviderType;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class SkylarkSpawnHeightProvider extends HeightProvider {
    public static final SkylarkSpawnHeightProvider AT_SPAWN_HEIGHT = new SkylarkSpawnHeightProvider(YOffset.fixed(0));
    public static final MapCodec<SkylarkSpawnHeightProvider> SKYLARK_SPAWN_CODEC = YOffset.OFFSET_CODEC.fieldOf("value").xmap(SkylarkSpawnHeightProvider::new, SkylarkSpawnHeightProvider::getOffset);
    public static final HeightProviderType<SkylarkSpawnHeightProvider> SKYLARK_SPAWN = HeightProviderType.register("skylark:skylark_spawn", SkylarkSpawnHeightProvider.SKYLARK_SPAWN_CODEC);
    private final YOffset offset;

    public static SkylarkSpawnHeightProvider create(YOffset offset) {
        return new SkylarkSpawnHeightProvider(offset);
    }

    private SkylarkSpawnHeightProvider(YOffset offset) {
        this.offset = offset;
    }

    public YOffset getOffset() {
        return this.offset;
    }

    @Override
    public int get(@Nullable Random random, HeightContext context) {
        return Skylark.getConfig().spawnHeight + this.offset.getY(context);
    }

    @Override
    public HeightProviderType<?> getType() {
        return SkylarkSpawnHeightProvider.SKYLARK_SPAWN;
    }

    public String toString() {
        return this.offset.toString();
    }

    public static void register() {}
}
