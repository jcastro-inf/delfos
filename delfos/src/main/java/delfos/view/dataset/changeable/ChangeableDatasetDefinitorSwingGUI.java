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
package delfos.view.dataset.changeable;

import delfos.common.parameters.view.EditParameterDialog;
import delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser;
import static delfos.configfile.rs.single.ChangeableDatasetConfigurationFileParser.CONFIGURATION_EXTENSION;
import delfos.configuration.scopes.SwingGUIScope;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.factories.DatasetLoadersFactory;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Frame to define a changeable dataset and save its xml file.
 *
 * @author Jorge Castro Gallardo
 */
public class ChangeableDatasetDefinitorSwingGUI extends Frame {

    public static final long serialVersionUID = 1L;

    private JComboBox<ChangeableDatasetLoader> datasetSelector;
    private JButton datasetSelectorParametersButton;

    private File configFile;
    private JButton saveConfigAndInitialiseButton;

    public ChangeableDatasetDefinitorSwingGUI(File configFile, boolean exitOnClose) {
        super("Configure database");
        this.configFile = configFile;

        initComponents();

        if (exitOnClose) {
            this.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    super.windowClosed(e);
                    System.exit(0);
                }
            });
        }

        this.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        this.setLocation((d.width - this.getWidth()) / 2, (d.height - this.getHeight()) / 2);
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelDatasetSelector(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.saveConfigAndInitialiseButton = new JButton("Save config file");
        this.saveConfigAndInitialiseButton.addActionListener((ActionEvent e) -> {
            saveConfig();
        });

        this.add(this.saveConfigAndInitialiseButton, constraints);
    }

    private Component panelDatasetSelector() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Dataset"));

        GridBagConstraints constraints = new GridBagConstraints();

        Collection<ChangeableDatasetLoader> items = DatasetLoadersFactory.getInstance().getAllClasses(ChangeableDatasetLoader.class);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.datasetSelector = new JComboBox(items.toArray());
        ret.add(datasetSelector, constraints);

        datasetSelector.addActionListener((ActionEvent e) -> {
            DatasetLoader<? extends Rating> cbdci = (DatasetLoader) datasetSelector.getSelectedItem();
            datasetSelectorParametersButton.setEnabled(cbdci.hasParameters());
        });

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.datasetSelectorParametersButton = new JButton("Parameters");
        this.datasetSelectorParametersButton.addActionListener((ActionEvent e) -> {
            EditParameterDialog dialog = new EditParameterDialog(ChangeableDatasetDefinitorSwingGUI.this, false);
            dialog.setParameterTaker((DatasetLoader) datasetSelector.getSelectedItem());
            dialog.setVisible(true);
        });
        ret.add(datasetSelectorParametersButton, constraints);

        return ret;
    }

    private void saveConfig() {

        boolean isConfigFileSaved = false;
        boolean isCancelled = false;
        while (!isConfigFileSaved && !isCancelled) {
            JFileChooser jfc = new JFileChooser(SwingGUIScope.getInstance().getCurrentDirectory());
            jfc.setSelectedFile(configFile);
            jfc.setFileFilter(new FileNameExtensionFilter("Database configuration (XML)", "xml"));
            jfc.setDialogTitle("Save the database configuration XML file");
            int option = jfc.showSaveDialog(ChangeableDatasetDefinitorSwingGUI.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                SwingGUIScope.getInstance().setCurrentDirectory(jfc.getSelectedFile());
                configFile = jfc.getSelectedFile().getAbsoluteFile();

                if (!configFile.getAbsolutePath().endsWith("." + CONFIGURATION_EXTENSION)) {
                    configFile = new File(configFile.getAbsolutePath() + "." + CONFIGURATION_EXTENSION);
                }

                boolean isPreparedToSave = true;

                if (configFile.exists()) {
                    int userAnswerConfirmDialog = JOptionPane.showConfirmDialog(jfc, "Overwrite '" + configFile.getAbsolutePath() + "' file?", "Confirm overwrite", JOptionPane.YES_NO_CANCEL_OPTION);

                    switch (userAnswerConfirmDialog) {
                        case JOptionPane.YES_OPTION:
                            isPreparedToSave = true;
                            break;
                        case JOptionPane.NO_OPTION:
                            isPreparedToSave = false;
                            break;
                        case JOptionPane.CANCEL_OPTION:
                            isPreparedToSave = false;
                            isCancelled = true;
                            break;
                    }
                }

                if (isPreparedToSave) {
                    final ChangeableDatasetLoader loader = (ChangeableDatasetLoader) datasetSelector.getSelectedItem();
                    try {
                        ChangeableDatasetConfigurationFileParser.saveConfigFile(configFile, loader);
                        isConfigFileSaved = true;
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this,
                                ex.getMessage(),
                                "The db-config file '" + configFile.getAbsolutePath() + "' cannot be saved.",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }
}
