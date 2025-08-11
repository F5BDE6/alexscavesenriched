package net.hellomouse.alexscavesenriched.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.hellomouse.alexscavesenriched.client.particle.texture.DemonCoreGlowTexture;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ReloadDemonCoreTextureCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("reloadDemonCoreTexture")
                .executes(ReloadDemonCoreTextureCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> command) {
        command.getSource().sendMessage(Text.literal("Reloading demon core glow texture."));
        DemonCoreGlowTexture.reset();
        command.getSource().sendMessage(Text.literal("Reload finished."));
        return Command.SINGLE_SUCCESS;
    }
}
