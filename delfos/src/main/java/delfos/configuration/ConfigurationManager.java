/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.configuration;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.configuration.scopes.ConfiguredDatasetsScope;
import delfos.configuration.scopes.SwingGUIScope;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jcastro
 */
public class ConfigurationManager {

    /**
     * Directorio dentro del que sólo se guardan archivos de configuración.
     */
    public static File CONFIGURATION_DIRECTORY;

    /**
     * Extension associated to the library configurations.
     */
    private static final String CONFIGURATION_FILE_EXTENSION = ".xml";

    static {
        String userHome = System.getenv(Constants.EnvironmentVariables.HOME);

        if (userHome == null || userHome.isEmpty()) {
            throw new IllegalStateException("Please, set the $HOME environment variable.");
        }

        if (!new File(userHome).exists()) {
            Global.showWarning("The home directory '" + userHome + "' does not exists.");
        }

        CONFIGURATION_DIRECTORY = new File(userHome + File.separator + ".config" + File.separator + "delfos" + File.separator);
    }

    public static void setConfigurationDirectory(File configurationDirectory) {

        checkConfigurationDirectory(configurationDirectory);

        CONFIGURATION_DIRECTORY = configurationDirectory;

        if (configurationDirectory.exists()) {
            ConfiguredDatasetsScope.getInstance().loadConfigurationScope();
            SwingGUIScope.getInstance().loadConfigurationScope();
        } else {
            createConfigurationDirectoryPathIfNotExists();
        }
    }

    public static void checkConfigurationDirectory(File configurationDirectory) {

        if (!configurationDirectory.exists()) {
            Global.showInfoMessage("The configuration directory '" + configurationDirectory + "' does not exists. Creating.");
        } else if (!configurationDirectory.isDirectory()) {
            throw new IllegalArgumentException("The file '" + configurationDirectory + "' is not a directory, cannot be the configuration directory");
        }
    }

    public static void createConfigurationDirectoryPathIfNotExists() {
        if (!CONFIGURATION_DIRECTORY.exists()) {
            boolean mkdirs = ConfigurationManager.CONFIGURATION_DIRECTORY.mkdirs();
            if (!mkdirs) {
                IOException ex = new IOException("Cannot create '" + ConfigurationManager.CONFIGURATION_DIRECTORY.getAbsolutePath() + "' directory");
                ERROR_CODES.CANNOT_WRITE_LIBRARY_CONFIG_FILE.exit(ex);
            }
        }
    }

    /**
     * Returns the configuration file associated to the specified scope of the
     * library. Examples of scopes: path, swing-gui, configured-datasets.
     *
     * If the configuration file does not exists, it is created.
     *
     * @param configurationScope Scope to retrieve its configuration file.
     * @return File pointing to a xml file that has the configuration.
     */
    public static File getConfigurationFile(ConfigurationScope configurationScope) {

        File configurationFile = new File(CONFIGURATION_DIRECTORY.getAbsolutePath()
                + File.separator + configurationScope.getScopeName() + CONFIGURATION_FILE_EXTENSION);

        return configurationFile;
    }

}
