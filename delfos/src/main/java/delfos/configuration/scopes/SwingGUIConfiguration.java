package delfos.configuration.scopes;

import delfos.configuration.Configuration;
import java.io.File;

/**
 *
 * @author jcastro
 */
public class SwingGUIConfiguration extends Configuration {

    private static SwingGUIConfiguration instance;

    public static SwingGUIConfiguration getInstance() {
        if (instance == null) {
            instance = new SwingGUIConfiguration();
        }

        return instance;
    }

    public SwingGUIConfiguration() {
        super("swing-gui");
    }

    public File getCurrentDirectory() {
        return new File(this.getProperty("gui.defaults.currentDirectory"));
    }

    public void setCurrentDirectory(File value) {
        this.setProperty("gui.defaults.currentDirectory", value.getAbsolutePath());
    }

}
