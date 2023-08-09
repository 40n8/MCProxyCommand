package io.github.geraldtm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;




@Mod("proxycommand")
public class ProxyCommandMod {

    public static final String MODID = "proxycommand";

    public static final Logger LOGGER = LogManager.getLogger("ProxyCommand");

    public ProxyCommandMod() {
        LOGGER.info("ProxyCommand is active");

    }

    
    private static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
            Commands.literal("proxycommand")
            .requires(cmd -> cmd.hasPermission(2))
            .then(Commands.argument("command", StringArgumentType.string())
                .executes(ProxyCommandMod::sendMessage)
                .build())
        );

    }

    private static int sendMessage(CommandContext<CommandSourceStack> context) {
        var command = StringArgumentType.getString(context, "command");

        var player = context.getSource().getPlayer();
        if (player == null) {
            LOGGER.warn("Command \"" + command + "\" was executed without the player as source");
            context.getSource().sendFailure(Component.literal("Command source must be a player"));
            
            return -1;
        }

        LOGGER.info("Proxycommand \"" + command + "\" was triggered by " + player.getName().getString());
        return 0;
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public class Listener {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            LOGGER.info("Registering command");
            registerCommand(event.getDispatcher());
        }
    }

}
