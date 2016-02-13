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
package delfos.view.configureddatasets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.factories.DatasetLoadersFactory;
import delfos.common.parameters.view.EditParameterDialog;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 13-mar-2014
 */
public class NewConfiguredDatasetDialog extends JDialog {

    private JTextField nameJTextField;
    private JTextArea descriptionJTextArea;
    private JComboBox<DatasetLoader<? extends Rating>> datasetTypeSelector;
    private JButton parametersButton;
    private JButton aceptarButton;

    public NewConfiguredDatasetDialog(JFrame owner, boolean modal) {
        super(owner, modal);

        initComponents();

        pack();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints constraints;

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(new Label("Name"), constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.nameJTextField = new JTextField();
        this.add(nameJTextField, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(new Label("Description"), constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.descriptionJTextArea = new JTextArea();
        JScrollPane jScrollPane = new JScrollPane(this.descriptionJTextArea);

        this.add(jScrollPane, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(new JLabel("Type"), constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        Collection<DatasetLoader> allRatingsDatasetLoader = DatasetLoadersFactory.getInstance().getAllClasses();
        this.datasetTypeSelector = new JComboBox<DatasetLoader<? extends Rating>>(allRatingsDatasetLoader.toArray(new DatasetLoader[0]));
        this.add(datasetTypeSelector, constraints);

        datasetTypeSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DatasetLoader<? extends Rating> cbdci = (DatasetLoader) datasetTypeSelector.getSelectedItem();

                //Activa el boton si tiene dialogo de parametros y lo desactiva
                //en caso contrario
                parametersButton.setEnabled(cbdci.getParameters().size() > 0);
            }
        });

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 5;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.parametersButton = new JButton("Parameters");
        this.parametersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditParameterDialog dialog = new EditParameterDialog(NewConfiguredDatasetDialog.this, false);
                dialog.setParameterTaker((DatasetLoader) datasetTypeSelector.getSelectedItem());
                dialog.pack();
                dialog.setVisible(true);
            }
        });
        this.add(parametersButton, constraints);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.aceptarButton = new JButton("Submit");
        this.add(aceptarButton, constraints);

        this.aceptarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                NewConfiguredDatasetDialog.this.dispose();

                String name = nameJTextField.getText();
                String description = descriptionJTextArea.getText();
                DatasetLoader<? extends Rating> datasetLoader = (DatasetLoader<? extends Rating>) datasetTypeSelector.getSelectedItem();

                ConfiguredDatasetsFactory.getInstance().addDatasetLoader(name, description, datasetLoader);

                NewConfiguredDatasetDialog.this.setVisible(false);
                NewConfiguredDatasetDialog.this.dispose();

            }
        });
    }

}
