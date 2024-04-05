package net.gnomecraft.skylark.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.gnomecraft.skylark.Skylark;
import net.gnomecraft.skylark.util.TeamDescription;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class SkylarkTeamCommand {
    protected static int listKeys(CommandContext<ServerCommandSource> context) {
        ArrayList<TeamDescription> teams = Skylark.STATE.getTeams();

        context.getSource().sendMessage(Text.translatable("skylark.command.team.list.keys"));

        for (TeamDescription team : teams) {
            context.getSource().sendMessage(formattedTeamListLine(team, true, context.getSource().hasPermissionLevel(4)));
        }

        return 1;
    }

    protected static int listNames(CommandContext<ServerCommandSource> context) {
        ArrayList<TeamDescription> teams = Skylark.STATE.getTeams();

        context.getSource().sendMessage(Text.translatable("skylark.command.team.list.names"));

        for (TeamDescription team : teams) {
            context.getSource().sendMessage(formattedTeamListLine(team, false, context.getSource().hasPermissionLevel(4)));
        }

        return 1;
    }

    private static Text formattedTeamListLine(TeamDescription team, boolean keys, boolean ops) {
        String teamNameString;
        if (keys) {
            teamNameString = "§b" + team.name() + "§r";
        } else {
            teamNameString = "§b" + team.displayName().getString() + "§r";
        }
        MutableText teamName = Text.literal(String.format("%-20s  ", teamNameString));

        String spawnPosString = "";
        MutableText spawnPos = Text.empty();
        if (ops) {
            if (team.spawnPos() == null) {
                spawnPosString = "§8unassigned§r";
            } else {
                spawnPosString = "§e(" + team.spawnPos().toShortString() + ")§r";
            }
        }
        spawnPos.append(String.format("%-20s  ", spawnPosString));

        MutableText playerList = Text.literal("  ");
        for (int i = 0; i < team.members().size(); ++i) {
            if (i > 0) {
                playerList.append(", ");
            }
            if (keys) {
                playerList.append("§7" + team.members().get(i).getNameForScoreboard() + "§r");
            } else {
                playerList.append("§7" + team.members().get(i).getDisplayName().getString() + "§r");
            }
        }

        return teamName.append(spawnPos).append(playerList);
    }

    protected static int setSpawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().hasPermissionLevel(4)) {
            // This should not be reached, as Brigadier should prevent processing for sources without ops.
            Skylark.LOGGER.warn("Attempt to call SkylarkTeamCommand.setSpawn() by non-operator: {}", context.getSource().getName());
            return -1;
        }

        String teamKey = StringArgumentType.getString(context, "teamKey");
        BlockPos position = BlockPosArgumentType.getValidBlockPos(context, "position");
        if (teamKey == null || position == null || !Skylark.STATE.setTeamSpawnPos(teamKey, position)) {
            context.getSource().sendMessage(Text.translatable("skylark.command.team.spawn.failed")
                    .append(": " + (position != null ? position.toShortString() : "-")));
            return -1;
        }

        context.getSource().sendMessage(Text.translatable("skylark.command.team.spawn.success")
                .append(": " + position.toShortString()));
        return 1;
    }
}
