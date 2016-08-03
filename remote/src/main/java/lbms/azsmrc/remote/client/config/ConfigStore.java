package lbms.azsmrc.remote.client.config;

import lbms.tools.ExtendedProperties;

/**
 * Created by dmitry on 7/28/16.
 */
public interface ConfigStore {
    ExtendedProperties getProperties();

    void saveConfig();
}
