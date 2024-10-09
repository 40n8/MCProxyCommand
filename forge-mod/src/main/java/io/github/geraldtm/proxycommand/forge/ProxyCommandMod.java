package io.github.geraldtm.proxycommand.forge;

import de.michiruf.proxycommand.common.ProxyCommandConstants;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;


import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;


@Mod("proxycommandmod")
public class ProxyCommandMod {

    public static final String MODID = "proxycommandmod";

    public static final Logger LOGGER = LogManager.getLogger("ProxyCommand");

    private static final String PROTOCOL_VERSION = NetworkRegistry.ACCEPTVANILLA;

    private static int id = 0;
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("proxycommand", "command_packet"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

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


        // To communicate with the proxy,
        // a S2C packet sent via the players connection is needed
        // (the player's connection is the means of communication with the proxy)
        INSTANCE.registerMessage(id, Message.class,
                Message::encoder,
                Message::decoder,
                Message::messageConsumer);
        INSTANCE.messageBuilder(Message.class, id)
                .encoder(Message::encoder)
                .decoder(Message::decoder)
                .consumerMainThread(Message::messageConsumer);

        INSTANCE.send(PacketDistributor.PLAYER.with((Supplier<ServerPlayer>) player), new Message(command));
        return 1;
    }

    private static class Message {

        private String command;

        public Message(String command){
            this.command = command;
        }

        public void encoder(FriendlyByteBuf buffer) {
            buffer.writeUtf(this.command);
            LOGGER.debug(buffer.readUtf());
        }

        public static Message decoder(FriendlyByteBuf buffer) {
            buffer.skipBytes(1);
            String command = buffer.readUtf();
            LOGGER.error("Recieved proxycommand packet. This should not be possible. " + command);
            //This should not run as packet is intercpted the velocity plugin
            return new Message(command);
        }

        public void messageConsumer(Supplier<NetworkEvent.Context> ctx) {
            //This should not run as packet is intercpted the velocity plugin
            LOGGER.error("Tried to handle ProxyCommand packet. This should not be possible");
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Listener {
        @SubscribeEvent
        public static void registerCommands(RegisterCommandsEvent event) {
            LOGGER.info("Registering command");
            registerCommand(event.getDispatcher());
        }
    }

}
