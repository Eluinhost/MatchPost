package gg.uhc.matchpost.async;

import com.google.common.base.Function;
import org.bukkit.command.CommandSender;

import java.lang.ref.WeakReference;

/**
 * Stores a sender in a weak reference to be sent messages at a later point.
 * If the sender no longer exists no message will be send on call.
 */
public class CommandSenderMessenger implements Function<String, Void> {

    protected final WeakReference<CommandSender> senderRef;

    public CommandSenderMessenger(CommandSender sender) {
        this.senderRef = new WeakReference<>(sender);
    }

    @Override
    public Void apply(String input) {
        CommandSender sender = senderRef.get();

        if (sender != null) {
            sender.sendMessage(input);
        }

        return null;
    }
}
