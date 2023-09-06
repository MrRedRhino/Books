package org.pipeman.books;

import org.pipeman.pconf.AbstractConfig;

import java.nio.file.Path;

public class Config extends AbstractConfig {
    public final int port = this.get("server-port", 15000);
    public final String aiKey = this.get("ai-key", "");
    public final int dailyUsageLimit = this.get("daily-usage-limit", 233_333);

    public Config(String file) {
        super(file);
        store(Path.of(file), "Pipe-Books Config");
    }
}
