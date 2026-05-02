package common.config;

import org.aeonbits.owner.ConfigCache;

public final class ConfigFactory {

    private ConfigFactory() {}

    public static FrameworkConfig getConfig() {
        if (System.getProperty("env") == null) {
            System.setProperty("env", "qa");
        }
        return ConfigCache.getOrCreate(FrameworkConfig.class);
    }
}
