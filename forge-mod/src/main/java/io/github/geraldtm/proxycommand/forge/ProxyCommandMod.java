package io.github.geraldtm.proxycommand.forge;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
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


@Mod("proxycommandmod")
public class ProxyCommandMod {

    public static final String MODID = "proxycommandmod";

    public static final Logger LOGGER = LogManager.getLogger("ProxyCommand");

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            NetworkRegistry.ACCEPTVANILLA::equals,
            PROTOCOL_VERSION::equals
    );
    int index = 0;

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

        //INSTANCE.registerMessage(index++, )

        // To communicate with the proxy, a S2C packet sent via the players connection is needed (the player's connection is the means of communication with the proxy)
        //INSTANCE.send(
        //         player,


        return 1;
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Listener {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            LOGGER.info("Registering command");
            //registerCommand(event.getDispatcher());
        }
    }


}
