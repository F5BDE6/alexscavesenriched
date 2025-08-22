package net.hellomouse.alexscavesenriched.client.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.hellomouse.alexscavesenriched.client.particle.texture.DemonCoreGlowTexture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ReloadDemonCoreTextureCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("reloadDemonCoreTexture")
                .executes(ReloadDemonCoreTextureCommand::execute));
    }

    private static int execute(CommandContext<CommandSourceStack> command) {
        command.getSource().sendSystemMessage(Component.literal("Reloading demon core glow texture."));
        DemonCoreGlowTexture.reset();
        command.getSource().sendSystemMessage(Component.literal("Reload finished."));
        return Command.SINGLE_SUCCESS;
    }
}
