package org.pipeman.books;

import org.pipeman.pconf.AbstractConfig;

import java.nio.file.Path;

public class Config extends AbstractConfig {
    public final int port = this.get("server-port", 15000);
    public final String aiKey = this.get("ai-key", "");

    public Config(String file) {
        super(file);
        store(Path.of(file), "Pipe-Books Config");
    }
}
