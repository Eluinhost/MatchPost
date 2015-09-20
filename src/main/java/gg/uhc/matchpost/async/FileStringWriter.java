package gg.uhc.matchpost.async;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Callable;

public class FileStringWriter implements Callable<Void> {

    protected final Path path;
    protected final String contents;

    public FileStringWriter(Path path, String contents) {
        this.path = path;
        this.contents = contents;
    }

    @Override
    public Void call() throws Exception {
        Files.write(path, contents.getBytes(Charset.forName("UTF8")), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        return null;
    }
}
