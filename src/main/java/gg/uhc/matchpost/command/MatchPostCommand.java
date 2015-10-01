package gg.uhc.matchpost.command;

import gg.uhc.matchpost.async.CommandSenderMessenger;
import gg.uhc.matchpost.reddit.MatchPostController;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MatchPostCommand implements CommandExecutor {

    public static final String USE_PERMISSION = "uhc.matchpost.command.use";
    public static final String SET_PERMISSION = "uhc.matchpost.command.set";

    protected final MatchPostController matchPostController;

    public MatchPostCommand(MatchPostController matchPostController) {
        this.matchPostController = matchPostController;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        // check general permission
        if (!commandSender.hasPermission(USE_PERMISSION)) {
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to use this command");
            return true;
        }

        // no-arg is show post
        if (args.length == 0) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(ChatColor.RED + "This command can only be ran by a player");
                return true;
            }

            ((Player) commandSender).spigot().sendMessage(matchPostController.getMatchPost());
            return true;
        }

        // check setting permissions
        if (!commandSender.hasPermission(SET_PERMISSION)) {
            commandSender.sendMessage(ChatColor.RED + "You do not have permission to set the matchpost values");
            return true;
        }

        // wrap the sender for async usage
        CommandSenderMessenger sender = new CommandSenderMessenger(commandSender);

        // clear the current post
        if (args[0].equalsIgnoreCase("clear")) {
            matchPostController.clear(sender);
            commandSender.sendMessage(ChatColor.GOLD + "Cleared match post");
            return true;
        }

        // read from reddit
        if (args[0].equalsIgnoreCase("reddit")) {
            if (args.length < 2) {
                commandSender.sendMessage(ChatColor.RED + "Reddit usage: /matchpost reddit <reddit id>");
                return true;
            }

            commandSender.sendMessage(ChatColor.GOLD + "Starting download of match post...");
            matchPostController.readFromReddit(sender, args[1]);
            return true;
        }

        // read from a file
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "File usage: /matchpost <filename>");
            return true;
        }

        // read from the file
        matchPostController.readFromDataFolder(sender, args[0]);

        return true;
    }
}
