package gg.uhc.matchpost.reddit;

import com.comphenix.executors.BukkitExecutors;
import com.comphenix.executors.BukkitScheduledExecutorService;
import com.google.common.base.Optional;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import gg.uhc.matchpost.async.CommandSenderMessenger;
import gg.uhc.matchpost.async.FileStringReader;
import gg.uhc.matchpost.async.FileStringWriter;
import gg.uhc.matchpost.async.PrintErrorsCallback;
import net.dean.jraw.http.oauth.OAuthException;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class MatchPostController {

    // helpers for async <-> sync calls
    protected final BukkitScheduledExecutorService sync;
    protected final BukkitScheduledExecutorService async;

    // handles parsing of markdown
    protected final MatchPostParser parser;

    // default messages to send
    protected final BaseComponent downloadingMessage;
    protected final BaseComponent noneSetMessage;

    // the base path to check for files in
    protected final Path basePath;

    // the path of the default save file
    protected final Path savePath;

    // provides callables with match post contents
    protected final RedditCallableFactory redditFactory;

    // the currently set match post
    // should only be interacted with
    // on the sync/server thread
    protected Optional<BaseComponent> matchPost = Optional.absent();

    // set to true if waiting for download
    protected boolean isDownloading = false;

    public MatchPostController(Plugin plugin, MatchPostParser parser, RedditCallableFactory redditFactory, File base) {
        this.redditFactory = redditFactory;
        this.parser = parser;

        this.async = BukkitExecutors.newAsynchronous(plugin);
        this.sync = BukkitExecutors.newSynchronous(plugin);


        this.basePath = base.toPath();
        this.savePath = basePath.resolve("current.md");

        this.downloadingMessage = new TextComponent("Current post is currently downloading or waiting to download, please try again later");
        this.downloadingMessage.setColor(ChatColor.RED);
        this.downloadingMessage.setBold(true);

        this.noneSetMessage = new TextComponent("There is no match post set");
        this.noneSetMessage.setColor(ChatColor.AQUA);

        // load from current.md on initialization
        readFromDataFolder(new CommandSenderMessenger(Bukkit.getConsoleSender()), "current.md");
    }

    public void readFromReddit(final CommandSenderMessenger sender, String code) {
        // start fetch from reddit
        ListenableFuture<String> contents = async.submit(redditFactory.createSubmissionCallable(code));

        // flag as currently downloading
        isDownloading = true;

        // on complete (sync callback)
        Futures.addCallback(contents, new FutureCallback<String>() {
            @Override
            public void onSuccess(String result) {
                // undo flag
                isDownloading = false;

                // save to current.md async
                ListenableFuture<Void> save = async.submit(new FileStringWriter(savePath, result));

                // when it's complete read call usual readFromFile to read from current.md (sync callback)
                Futures.addCallback(save, new FutureCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        readFromDataFolder(sender, "current.md");
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        throwable.printStackTrace();
                        sender.apply(ChatColor.RED + "Failed to save reddit post to current.md, see console for technical information");
                    }
                }, sync);
            }

            @Override
            public void onFailure(Throwable t) {
                // undo flag
                isDownloading = false;

                t.printStackTrace();

                if (t instanceof OAuthException) {
                    sender.apply(ChatColor.RED + "Error authenticating against Reddit, is reddit on fire?");
                } else {
                    sender.apply(ChatColor.RED + "An error occured fetching the match post, see console for technical information");
                }
            }
        }, sync);
    }

    public void readFromDataFolder(final CommandSenderMessenger sender, String name) {
        // read the contents async
        ListenableFuture<String> readFile = async.submit(new FileStringReader(basePath.resolve(name)));

        // parse with the parser async
        ListenableFuture<BaseComponent> parsedFile = Futures.transform(readFile, parser, async);

        // set match post (sync callback)
        Futures.addCallback(parsedFile, new FutureCallback<BaseComponent>() {
            @Override
            public void onSuccess(BaseComponent result) {
                matchPost = Optional.of(result);

                sender.apply(ChatColor.AQUA + "Match post updated");
            }

            @Override
            public void onFailure(Throwable t) {
                if (t instanceof NoSuchFileException) {
                    sender.apply(ChatColor.RED + "File not found");
                } else {
                    t.printStackTrace();
                    sender.apply(ChatColor.RED + "There was a problem parsing/reading the post file, check console for more information");
                }
            }
        }, sync);

        // only save to current.md if we're not reading it
        if (!name.equals("current.md")) {
            // set string contents into current.md async callback
            Futures.addCallback(readFile, new FutureCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    // run saving async
                    // we don't really care if this passes or fails, so just print errors
                    Futures.addCallback(async.submit(new FileStringWriter(savePath, result)), new PrintErrorsCallback<Void>(), async);
                }

                @Override
                public void onFailure(Throwable t) {} // should be handled by the other callback/s
            }, async);
        }
    }

    public BaseComponent getMatchPost() {
        if (isDownloading) return downloadingMessage;

        return matchPost.or(noneSetMessage);
    }

    public void clear(final CommandSenderMessenger sender) {
        // start delete on async
        ListenableFuture<Void> deleted = async.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Files.deleteIfExists(savePath);
                return null;
            }
        });

        // sync callback to clear post + give feedback
        Futures.addCallback(deleted, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                matchPost = Optional.absent();
                sender.apply(ChatColor.AQUA + "Current match post cleared and current.md deleted");
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
                sender.apply(ChatColor.RED + "Failed to delete current.md, could not clear match post. See console for error");
            }
        }, sync);
    }
}
