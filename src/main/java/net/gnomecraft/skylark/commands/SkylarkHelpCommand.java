package net.gnomecraft.skylark.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SkylarkHelpCommand {
    protected static int noargs(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.translatable("skylark.command.hint"));
        context.getSource().sendMessage(Text.literal("  For the blackness of the interstellar void was not"));
        context.getSource().sendMessage(Text.literal("    the dark of an earthly night,"));
        context.getSource().sendMessage(Text.literal("  but the absolute black of the absence of all light,"));
        context.getSource().sendMessage(Text.literal("  beside which the black of platinum dust is pale and gray;"));
        context.getSource().sendMessage(Text.literal("  and laid upon this velvet were the jewel stars."));
        context.getSource().sendMessage(Text.literal("    - \"The Skylark of Space\""));
        context.getSource().sendMessage(Text.literal("      by E. E. \"Doc\" Smith and Lee Hawkins Garby"));

        return 1;
    }

    protected static int help(CommandContext<ServerCommandSource> context) {
        context.getSource().sendMessage(Text.translatable("skylark.command.help"));

        return 1;
    }

    protected static int helpSpecific(CommandContext<ServerCommandSource> context) {
        String subcommand = StringArgumentType.getString(context, "command");

        if (subcommand.compareTo("help") == 0) {
            // Very Funny
            return help(context);
        }

        if (SkylarkCommands.COMMANDS.contains(subcommand)) {
            context.getSource().sendMessage(Text.translatable("skylark.command.help." + subcommand));
            return 1;
        } else {
            context.getSource().sendMessage(Text.translatable("skylark.command.help.missing"));
            return -1;
        }
    }
}
