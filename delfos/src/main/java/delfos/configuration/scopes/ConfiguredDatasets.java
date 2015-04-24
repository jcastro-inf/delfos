package delfos.configuration.scopes;

import delfos.configuration.Configuration;

/**
 *
 * @author jcastro
 */
public class ConfiguredDatasets extends Configuration {

    private static SwingGUIConfiguration instance;

    public static SwingGUIConfiguration getInstance() {
        if (instance == null) {
            instance = new SwingGUIConfiguration();
        }

        return instance;
    }

    public ConfiguredDatasets() {
        super("configured-datasets");
    }

}
