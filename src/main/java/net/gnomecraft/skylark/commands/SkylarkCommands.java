package net.gnomecraft.skylark.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.util.List;

import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.command.argument.BlockPosArgumentType.blockPos;
import static net.minecraft.command.argument.EntityArgumentType.players;
import static net.minecraft.server.command.CommandManager.*;

public class SkylarkCommands {
    protected static List<String> COMMANDS = List.of("help", "team", "teleport");

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("skylark")
                .then(literal("help")
                        .then(argument("command", word())
                                .executes(SkylarkHelpCommand::helpSpecific))
                        .executes(SkylarkHelpCommand::help))
                .then(literal("team")
                        .then(literal("list")
                                .then(literal("keys")
                                        .executes(SkylarkTeamCommand::listKeys))
                                .then(literal("names")
                                        .executes(SkylarkTeamCommand::listNames))
                                .executes(SkylarkTeamCommand::listNames))
                        .then(literal("spawn")
                                .requires(source -> source.hasPermissionLevel(4))
                                .then(argument("teamKey", word())
                                        .then(argument("position", blockPos())
                                                .executes(SkylarkTeamCommand::setSpawn)))))
                .then(literal("teleport")
                        .requires(source -> source.hasPermissionLevel(4))
                        .then(literal("spawn")
                                .then(argument("players", players())
                                        .executes(SkylarkTeleportCommand::toSpawn))))
                .executes(SkylarkHelpCommand::noargs))
        );
    }
}
