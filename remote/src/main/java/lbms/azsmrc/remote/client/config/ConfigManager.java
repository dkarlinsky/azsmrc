package lbms.azsmrc.remote.client.config;

import lbms.tools.ExtendedProperties;

/**
 * Created by dmitry on 7/28/16.
 */
public class ConfigManager {
    private static final String LAST_DIRECTORY_KEY = "Last.Directory";
    private ExtendedProperties properties;
    private ConfigStore configStore;

    public ConfigManager(ExtendedProperties properties) {
        this.properties = properties;
    }

    public ConfigManager(ConfigStore configStore) {
        this.configStore = configStore;
        properties = configStore.getProperties();
    }


    public String getLastDir() {
        return properties.getProperty(LAST_DIRECTORY_KEY, "");
    }

    public void setLastDir(String lastDir) {
        properties.setProperty(LAST_DIRECTORY_KEY, lastDir);
    }

    public void saveConfig() {
        configStore.saveConfig();
    }
}
