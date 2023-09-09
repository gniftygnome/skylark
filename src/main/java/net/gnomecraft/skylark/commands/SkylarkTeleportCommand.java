package net.gnomecraft.skylark.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.gnomecraft.skylark.Skylark;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Collection;

public class SkylarkTeleportCommand {
    protected static int toSpawn(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if (!context.getSource().hasPermissionLevel(4)) {
            // This should not be reached, as Brigadier should prevent processing for sources without ops.
            Skylark.LOGGER.warn("Attempt to call SkylarkTeleportCommand.toSpawn() by non-operator: {}", context.getSource().getName());
            return -1;
        }

        Collection<ServerPlayerEntity> players = EntityArgumentType.getPlayers(context, "players");
        int successCount = 0;

        for (ServerPlayerEntity player : players) {
            if (Skylark.STATE.movePlayerToSpawn(player)) {
                ++successCount;
            }
        }

        if (successCount < 1) {
            context.getSource().sendMessage(Text.translatable("skylark.command.teleport.spawn.none"));
            return -1;
        }

        context.getSource().sendMessage(Text.literal(String.valueOf(successCount))
                .append(Text.translatable("skylark.command.teleport.spawn.success")));
        return 1;
    }
}
