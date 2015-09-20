package gg.uhc.matchpost;

import com.google.common.base.Joiner;
import gg.uhc.matchpost.chat.ChatSender;
import gg.uhc.matchpost.command.MatchPostCommand;
import gg.uhc.matchpost.reddit.MatchPostController;
import gg.uhc.matchpost.reddit.MatchPostParser;
import gg.uhc.matchpost.reddit.RedditCallableFactory;
import gg.uhc.matchpost.serialization.DefaultSerializationProvider;
import net.dean.jraw.RedditClient;
import net.dean.jraw.http.UserAgent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class Entry extends JavaPlugin {

    public void onEnable() {
        try {
            // create the data folder if it doesn't already exist
            File dataFolder = getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }

            // build a user agent
            PluginDescriptionFile pdf = getDescription();
            UserAgent ua = UserAgent.of("desktop", pdf.getName(), pdf.getVersion(), Joiner.on(",").join(pdf.getAuthors()));

            // build a reddit client
            RedditClient client = new RedditClient(ua);

            // creates callables for to get submissions
            RedditCallableFactory factory = new RedditCallableFactory(client);

            // handles parsing of markdown
            MatchPostParser parser = new MatchPostParser(new DefaultSerializationProvider());

            // create the controller in charge of the match post
            MatchPostController controller = new MatchPostController(this, parser, factory, dataFolder);

            // handles sending JSON chat to clients
            ChatSender chatSender = new ChatSender(this);

            // register the command
            MatchPostCommand command = new MatchPostCommand(controller, chatSender);
            getCommand("matchpost").setExecutor(command);

        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            setEnabled(false);
            getLogger().severe("This version of Spigot is unsupported");
        }
    }
}
