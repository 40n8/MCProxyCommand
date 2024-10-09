package sh.aiko.minecraft.plugin.proxycommand;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.michiruf.proxycommand.common.ProxyCommandConstants;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Proxycommand extends JavaPlugin {

    static Proxycommand plugin;

    @Override
    public void onEnable() {
        plugin = this;
        this.getServer()
                .getMessenger()
                .registerOutgoingPluginChannel(
                        this,
                        ProxyCommandConstants.COMMAND_PACKET_ID
                );

        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS.newHandler(event -> {
            final Commands commands = event.registrar();
            commands.register(
                    Commands.literal("proxycommand")
                            .then(
                                    Commands.argument("command", StringArgumentType.string())
                                            .executes(
                                                    Proxycommand::sendMessage
                                            )
                            )
                            .build(),
                    "A command that allows to send commands from your minecraft server instances to a velocity proxy."
            );
        }));
    }

    private static int sendMessage(CommandContext<CommandSourceStack> context) {
        StringBuilder command = new StringBuilder();
        try {
            command.append(StringArgumentType.getString(context, "command"));
            do {
                command.append(" ");
            } while (command.length() < 16);
        } catch (IllegalArgumentException e) {
            Proxycommand.plugin.getLogger().warning("Proxycommand: exception: " + e.getMessage());
            return Command.SINGLE_SUCCESS;
        }

        Player player = null;
        if (context.getSource().getSender() instanceof Player maybePlayer) {
            player = maybePlayer;
        } else if (context.getSource().getExecutor() instanceof Player maybePlayer) {
            // eg via commandblock
            player = maybePlayer;
        }

        String sendCommand = command.toString().stripLeading();

        if (null != player) {

            ByteArrayDataOutput out = ByteStreams.newDataOutput(sendCommand.length());
            out.writeUTF(sendCommand);

            Proxycommand.plugin.getLogger().info(
                    "Proxycommand: Command \"" +
                            sendCommand +
                            "\" was triggered by " +
                            player.getName()
            );

            player.sendPluginMessage(
                    Proxycommand.plugin,
                    ProxyCommandConstants.COMMAND_PACKET_ID,
                    out.toByteArray()
            );
        } else {
            Proxycommand.plugin.getLogger().warning(
                    "\"Proxycommand: Command \"" +
                            sendCommand +
                            "\" was executed without the player as source"
            );
            Objects.requireNonNull(context
                            .getSource().getExecutor())
                    .sendMessage("Command source must be a player: " + context
                            .getSource().getExecutor().toString());
            return -1;

        }
        return Command.SINGLE_SUCCESS;
    }


    @Override
    public void onDisable() {
        this.getServer()
                .getMessenger()
                .unregisterOutgoingPluginChannel(
                        this,
                        ProxyCommandConstants.COMMAND_PACKET_ID
                );
    }
}
