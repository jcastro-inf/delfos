package delfos.configuration.scopes;

import delfos.configuration.Configuration;
import java.io.File;
import javax.swing.JFrame;

/**
 *
 * @author jcastro
 */
public class SwingGUIConfiguration extends Configuration {

    public static final String singleUserExperimentWindow_sizeWidth = "SingleUser.Experiment.Window.Dimension.Width";
    public static final String singleUserExperimentWindow_sizeHeight = "SingleUser.Experiment.Window.Dimension.Height";
    public static final String singleUserExperimentWindow_locationX = "SingleUser.Experiment.Window.Position.X";
    public static final String singleUserExperimentWindow_locationY = "SingleUser.Experiment.Window.Position.Y";

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

    public void saveSingleUserExperimentWindowProperties(JFrame jframe) {
        setProperty(singleUserExperimentWindow_locationX, Integer.toString(jframe.getLocation().x));
        setProperty(singleUserExperimentWindow_locationY, Integer.toString(jframe.getLocation().y));
        setProperty(singleUserExperimentWindow_sizeWidth, Integer.toString(jframe.getSize().width));
        setProperty(singleUserExperimentWindow_sizeHeight, Integer.toString(jframe.getSize().height));
    }

    public void loadSingleUserExperimentWindowProperties(JFrame jframe) {

        int x, y, width, height;

        try {
            x = Integer.parseInt(getProperty(singleUserExperimentWindow_locationX));
        } catch (NumberFormatException ex) {
            x = jframe.getLocation().x;
        }

        try {
            y = Integer.parseInt(getProperty(singleUserExperimentWindow_locationY));
        } catch (NumberFormatException ex) {
            y = jframe.getLocation().y;
        }

        try {
            width = Integer.parseInt(getProperty(singleUserExperimentWindow_sizeWidth));
        } catch (NumberFormatException ex) {
            width = jframe.getSize().width;
        }

        try {
            height = Integer.parseInt(getProperty(singleUserExperimentWindow_sizeHeight));
        } catch (NumberFormatException ex) {
            height = jframe.getSize().height;
        }

        jframe.setSize(width, height);
        jframe.setLocation(x, y);

    }
}
