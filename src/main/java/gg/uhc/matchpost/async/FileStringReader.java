package gg.uhc.matchpost.async;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class FileStringReader implements Callable<String> {

    protected final Path filePath;

    public FileStringReader(Path filePath) {
        this.filePath = filePath;
    }

    @Override
    public String call() throws Exception {
        byte[] encoded = Files.readAllBytes(filePath);
        return new String(encoded, Charset.forName("UTF8"));
    }
}
