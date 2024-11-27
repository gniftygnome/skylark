package net.gnomecraft.skylark.config;

import net.gnomecraft.skylark.Skylark;
import net.gnomecraft.skylark.spawn.SetupSpawnPoint;
import net.gnomecraft.skylark.util.LandLocator;
import net.gnomecraft.skylark.util.TeamDescription;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import static net.gnomecraft.skylark.util.TeamDescription.TEAM_TYPES;

public class SkylarkState extends PersistentState {
    public static final String DEFAULT_TEAM = "skylark.team.default";
    public static final String PLAYER_PREFIX = "skylark.team.player.";
    public static final String SCOREBOARD_PREFIX = "minecraft.team.";

    // Special-case the default team to make sure we don't accidentally store the default spawn pos.
    private final LinkedHashMap<String, BlockPos> teamSpawnPos = new LinkedHashMap<>() {
        @Override
        public BlockPos get(Object key) {
            if (key instanceof String keyString && keyString.equals(DEFAULT_TEAM)) {
                return Skylark.getConfig().getDefaultSpawnPos(world);
            }

            return super.get(key);
        }

        @Override
        public BlockPos put(String key, BlockPos value) {
            if (key.equals(DEFAULT_TEAM)) {
                return value;
            }

            return super.put(key, value);
        }
    };
    private ServerWorld world;

    private static final String STATE_ID = Skylark.MOD_ID + "_state";
    private static final int STATE_VERSION = 1;

    /**
     * Called once during level spawn setup to initialize state for the new Overworld.
     *
     * @param serverWorld The new Overworld server instance.
     */
    public void init(@NotNull ServerWorld serverWorld) {
        assert(serverWorld.getRegistryKey().equals(World.OVERWORLD));
        if (world != serverWorld) {
            world = serverWorld;
            world.getPersistentStateManager().set(STATE_ID, Skylark.STATE);
            this.readState();
        }
    }

    /**
     * Ensures a spawn position is ready for player spawns.
     * <ol>
     * <li>Generates a spawn platform if none is present.</li>
     * <li>Adjusts the spawn position to be on the platform surface.</li>
     * <li>Generates a starter chest at spawn if configured.</li>
     * </ol>
     *
     * @param spawnPos The target location for the spawn platform.
     * @return The updated final location for spawns on the platform.
     */
    public @NotNull BlockPos preparePlayerSpawn(@NotNull BlockPos spawnPos) {
        // Refine the spawn position so it is on land if possible.
        spawnPos = LandLocator.refineSpawnPos(world, spawnPos);

        // Check whether we found land when we refined the spawn position.
        WorldChunk spawnChunk = world.getChunk(ChunkSectionPos.getSectionCoord(spawnPos.getX()), ChunkSectionPos.getSectionCoord(spawnPos.getZ()));
        boolean generate = spawnChunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, spawnPos.getX() & 0xF, spawnPos.getZ() & 0xF) < world.getBottomY();

        // Generate a team spawn platform if there's nothing there already.
        // This can sometimes repair the situation where a player has removed all blocks at their spawn.
        if (generate) {
            SetupSpawnPoint.generatePlatform(world, spawnPos, spawnChunk);
            spawnPos = LandLocator.refineSpawnPos(world, spawnPos);
            SetupSpawnPoint.generateSpawnChest(world, spawnPos);
        }

        return spawnPos;
    }

    /**
     * Ensure the player's team spawn is ready, then teleport the player to it.
     *
     * @param player The ServerPlayerEntity to be relocated.
     * @return True unless the player definitely could not be moved.
     */
    public boolean movePlayerToSpawn(@Nullable ServerPlayerEntity player) {
        // A few sanity checks first...
        if (player == null || world == null) {
            return false;
        }

        // Get the player's team spawn point coordinates.
        BlockPos spawnPos = Skylark.STATE.getPlayerSpawnPos(world, player);

        // Make sure there is a spawn platform and it is targeted by the spawn coordinates.
        spawnPos = Skylark.STATE.preparePlayerSpawn(spawnPos);

        // Relocate the player to the team spawn point.
        player.teleport(world, 0.5d + spawnPos.getX(), 0.1d + spawnPos.getY(), 0.5d + spawnPos.getZ(), 0.0f, 0.0f);

        return true;
    }

    /**
     * Get the coordinates of a player's team spawn point via read-through cache.
     * The first call to this method during a running game triggers a full Skylark state reload
     * and stores the new ServerWorld instance for the Overworld in the state manager.
     *
     * @param world This value must always be the current Overworld ServerWorld object.
     * @param player The player for whom spawn coordinates are requested.
     * @return Either team spawn coordinates or the default spawn coordinates.
     */
    public @NotNull BlockPos getPlayerSpawnPos(@NotNull ServerWorld world, @NotNull PlayerEntity player) {
        // Make sure we've got the server world stowed for state loads/saves.
        init(world);

        String team = getPlayerTeam(player);
        BlockPos spawnPos = teamSpawnPos.get(team);

        if (spawnPos == null) {
            spawnPos = getTeamSpawnPos(team);
        }

        Skylark.LOGGER.debug("'{}' is in team '{}' with default spawn at: {}", player.getNameForScoreboard(), team, spawnPos);
        return spawnPos;
    }

    /**
     * Get the coordinates of a team's spawn point.
     *
     * @param team The team key of the team whose spawn point is to be returned.
     * @return The spawn point of the requested team.
     */
    public @NotNull BlockPos getTeamSpawnPos(@NotNull String team) {
        // The default team is hard-coded to the origin.
        if (team.equals(DEFAULT_TEAM)) {
            return Skylark.getConfig().getDefaultSpawnPos(world);
        }

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
            teamSpawnPos.put(team, BlockPos.ofFloored(posX, posY, posZ));

            this.writeState();
        }

        BlockPos teamSpawn = teamSpawnPos.get(team);
        if (teamSpawn == null) {
            // This should never happen; if it does we will use the default team (central) spawn position.
            Skylark.LOGGER.error("Data integrity violation in SkylarkState.teamSpawnPos, team '{}'", team);

            teamSpawn = Skylark.getConfig().getDefaultSpawnPos(world);
            teamSpawnPos.put(team, teamSpawn);

            this.writeState();
        }

        return teamSpawn;
    }

    /**
     * Update a team spawn point to the requested coordinates.
     *
     * @param team The team key of the team whose spawn point is to be set.
     * @param pos The new spawn point for the team.
     * @return True unless the spawn point could not be updated as requested.
     */
    public boolean setTeamSpawnPos(@NotNull String team, @NotNull BlockPos pos) {
        if (teamSpawnPos.containsKey(team) && world.isInBuildLimit(pos)) {
            teamSpawnPos.replace(team, pos);

            this.writeState();

            return true;
        }

        Skylark.LOGGER.debug("Failed to update teamSpawnPos for team '{}' to: {}", team, pos.toShortString());
        return false;
    }

    /**
     * Returns the team key of the requested player's team.
     *
     * @param player The player whose team is to be looked up.
     * @return The team key of the requested player.
     */
    public @NotNull String getPlayerTeam(@NotNull PlayerEntity player) {
        AbstractTeam team = player.getScoreboardTeam();
        String teamName;

        if (team == null) {
            // The team of players who are not on a Minecraft Team is determined by configuration.
            if (Skylark.getConfig().separateTeams) {
                // Assign a hopefully-unique team name.
                teamName = PLAYER_PREFIX + player.getNameForScoreboard();
            } else {
                // Assign all non-Team players to a single default team.
                teamName = DEFAULT_TEAM;
            }
        } else {
            // Team players use the real Minecraft Team name.
            teamName = SCOREBOARD_PREFIX + team.getName();
        }

        return teamName;
    }

    /**
     * Returns a list of team members (ServerPlayerEntity) for the specified team key.
     *
     * @param team The team key of the team for which to list members.
     * @return A (possibly empty) list of members of the specified team.
     */
    public @NotNull List<ServerPlayerEntity> getTeamMembers(@NotNull String team) {
        if (!Skylark.getConfig().separateTeams && team.equals(DEFAULT_TEAM)) {
            return world.getPlayers().stream()
                    .filter(player -> player.getScoreboardTeam() == null)
                    .toList();
        } else if (Skylark.getConfig().separateTeams && team.startsWith(PLAYER_PREFIX)) {
            return world.getPlayers().stream()
                    .filter(player -> player.getScoreboardTeam() == null &&
                            player.getNameForScoreboard().compareTo(team.substring(PLAYER_PREFIX.length())) == 0)
                    .toList();
        } else if (team.startsWith(SCOREBOARD_PREFIX)) {
            return world.getPlayers().stream()
                    .filter(player -> player.isTeamPlayer(world.getScoreboard()
                            .getTeam(team.substring(SCOREBOARD_PREFIX.length()))))
                    .toList();
        } else {
            return List.of();
        }
    }

    /**
     * Fetches a list of teams with details and members as of a point in time.
     *
     * @return List of teams with details and members.
     */
    public @NotNull ArrayList<TeamDescription> getTeams() {
        ArrayList<TeamDescription> teams = new ArrayList<>(512);

        // Add the fake default team, which always exists.
        teams.add(new TeamDescription(DEFAULT_TEAM, TEAM_TYPES.DEFAULT, Text.translatable(DEFAULT_TEAM + ".name"), teamSpawnPos.get(DEFAULT_TEAM), getTeamMembers(DEFAULT_TEAM)));

        // Add the Minecraft Teams.
        world.getScoreboard().getTeams().forEach(team -> {
            String name = SCOREBOARD_PREFIX + team.getName();
            teams.add(new TeamDescription(name, TEAM_TYPES.MINECRAFT, team.getDisplayName(), teamSpawnPos.get(name), getTeamMembers(name)));
        });

        // Add any separate team mode Player teams.
        teamSpawnPos.keySet().forEach(team -> {
            if (team.startsWith(PLAYER_PREFIX)) {
                String name = team.substring(PLAYER_PREFIX.length());
                MutableText fancyName = Text.literal(name);
                List<ServerPlayerEntity> matches = world.getPlayers(player -> name.compareTo(player.getNameForScoreboard()) == 0);
                if (matches.size() > 0 && matches.get(0).getDisplayName() != null) {
                    fancyName = Objects.requireNonNull(matches.get(0).getDisplayName()).copy();
                }
                teams.add(new TeamDescription(team, TEAM_TYPES.PLAYER, fancyName, teamSpawnPos.get(team), getTeamMembers(team)));
            }
        });

        return teams;
    }

    private void writeState() {
        this.markDirty();
        world.getPersistentStateManager().save();
    }

    private void readState() {
        int nbtVersion = STATE_VERSION;
        NbtCompound nbt = null;
        NbtCompound nbtState = null;

        try {
            nbt = world.getPersistentStateManager().readNbt(STATE_ID, DataFixTypes.LEVEL, STATE_VERSION);
        } catch (IOException e) {
            Skylark.LOGGER.info("No saved state found; starting anew...");
        }
        if (nbt != null && nbt.contains("data")) {
            nbtVersion = nbt.getInt("DataVersion");
            nbtState = nbt.getCompound("data");
        }

        teamSpawnPos.clear();

        if (nbtState != null && !nbtState.isEmpty()) {
            NbtList teamSpawnPosNbt = nbtState.getList("TeamSpawnPosList", NbtList.COMPOUND_TYPE);
            if (nbtVersion < 1) {
                // FIXUP 1: Version 0 used Player UUID as team key; 1+ use Minecraft Team name (String)
                teamSpawnPosNbt.forEach(nbtElement -> {
                    NbtCompound teamSpawnPosEntry = ((NbtCompound) nbtElement);
                    ServerPlayerEntity playerEntity = null;
                    try {
                        playerEntity = world.getServer().getPlayerManager().getPlayer(teamSpawnPosEntry.getUuid("uuid"));
                    } catch (Exception ignored) {}
                    if (playerEntity != null) {
                        teamSpawnPos.put(PLAYER_PREFIX + playerEntity.getNameForScoreboard(),
                                BlockPos.ofFloored(teamSpawnPosEntry.getLong("x"),
                                        teamSpawnPosEntry.getLong("y"),
                                        teamSpawnPosEntry.getLong("z")
                                ));
                    }
                });
            } else {
                teamSpawnPosNbt.forEach(nbtElement -> {
                    NbtCompound teamSpawnPosEntry = ((NbtCompound) nbtElement);
                    teamSpawnPos.put(teamSpawnPosEntry.getString("name"),
                            BlockPos.ofFloored(teamSpawnPosEntry.getLong("x"),
                                    teamSpawnPosEntry.getLong("y"),
                                    teamSpawnPosEntry.getLong("z")
                            ));
                });
            }
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList teamSpawnPosNbt = new NbtList();
        teamSpawnPos.forEach((name, blockPos) -> {
            NbtCompound teamSpawnPosEntry = new NbtCompound();
            teamSpawnPosEntry.putString("name", name);
            teamSpawnPosEntry.putLong("x", blockPos.getX());
            teamSpawnPosEntry.putLong("y", blockPos.getY());
            teamSpawnPosEntry.putLong("z", blockPos.getZ());
            teamSpawnPosNbt.add(teamSpawnPosEntry);
        });
        nbt.put("TeamSpawnPosList", teamSpawnPosNbt);

        return nbt;
    }
}
