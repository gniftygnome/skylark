package net.gnomecraft.skylark.config;

import net.gnomecraft.skylark.Skylark;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

public class SkylarkState extends PersistentState {
    private final LinkedHashMap<UUID, BlockPos> teamSpawnPos = new LinkedHashMap<>();
    private ServerWorld world;

    private static final String STATE_ID = Skylark.modId + "_state";
    private static final int STATE_VERSION = 0;

    public void init(ServerWorld serverWorld) {
        assert(serverWorld.getRegistryKey().equals(World.OVERWORLD));
        if (world != serverWorld) {
            world = serverWorld;
            world.getPersistentStateManager().set(STATE_ID, Skylark.STATE);
            this.readState();
        }
    }

    public BlockPos getPlayerSpawnPos(ServerWorld world, PlayerEntity player) {
        // Make sure we've got the server world stowed for state loads/saves.
        init(world);

        UUID team = getPlayerTeam(player);
        BlockPos spawnPos = teamSpawnPos.get(team);

        if (spawnPos == null) {
            spawnPos = getTeamSpawnPos(team);
            teamSpawnPos.put(team, spawnPos);
        }

        Skylark.LOGGER.debug(player.getEntityName() + " is in team " + team + " with default spawn at: " + spawnPos);
        return spawnPos;
    }

    private BlockPos getTeamSpawnPos(UUID team) {
        if (!teamSpawnPos.containsKey(team)) {
            int teamCount = teamSpawnPos.size();
            double rotation = 0;

            if (teamCount > 0) {
                int cycle = 1 << (int) (Math.log(teamCount) / Math.log(2));
                int position = 2 * (teamCount - cycle) - 1;
                rotation = Math.PI * position / cycle;
            }

            long radius = Skylark.getConfig().spawnRingRadius;
            double posY = Skylark.getConfig().spawnHeight;
            double posX = radius * Math.cos(rotation);
            double posZ = radius * Math.sin(rotation);
            teamSpawnPos.put(team, new BlockPos(posX, posY, posZ));
        }

        this.writeState();

        return teamSpawnPos.get(team);
    }

    public UUID getPlayerTeam(PlayerEntity player) {
        // For now the player's team is their UUID.
        // UUID uuid = UUID.randomUUID();

        return player.getUuid();
    }

    private void writeState() {
        this.markDirty();
        world.getPersistentStateManager().save();
    }

    private void readState() {
        NbtCompound nbt = null;
        NbtCompound nbtState = null;

        try {
            nbt = world.getPersistentStateManager().readNbt(STATE_ID, STATE_VERSION);
        } catch (IOException e) {
            Skylark.LOGGER.info("No saved state found; starting anew...");
        }
        if (nbt != null && nbt.contains("data")) {
            int nbtVersion = nbt.getInt("DataVersion");
            nbtState = nbt.getCompound("data");
        }

        teamSpawnPos.clear();
        if (nbtState != null && !nbtState.isEmpty()) {
            NbtList teamSpawnPosNbt = nbtState.getList("TeamSpawnPosList", NbtList.COMPOUND_TYPE);
            teamSpawnPosNbt.forEach(nbtElement -> {
                NbtCompound teamSpawnPosEntry = ((NbtCompound) nbtElement);
                teamSpawnPos.put(teamSpawnPosEntry.getUuid("uuid"),
                        new BlockPos(teamSpawnPosEntry.getLong("x"),
                                teamSpawnPosEntry.getLong("y"),
                                teamSpawnPosEntry.getLong("z")
                                ));
            });
        }

    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList teamSpawnPosNbt = new NbtList();
        teamSpawnPos.forEach((uuid, blockPos) -> {
            NbtCompound teamSpawnPosEntry = new NbtCompound();
            teamSpawnPosEntry.putUuid("uuid", uuid);
            teamSpawnPosEntry.putLong("x", blockPos.getX());
            teamSpawnPosEntry.putLong("y", blockPos.getY());
            teamSpawnPosEntry.putLong("z", blockPos.getZ());
            teamSpawnPosNbt.add(teamSpawnPosEntry);
        });
        nbt.put("TeamSpawnPosList", teamSpawnPosNbt);

        return nbt;
    }
}
