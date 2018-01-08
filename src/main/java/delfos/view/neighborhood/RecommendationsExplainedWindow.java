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
package delfos.view.neighborhood;

import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.view.EditParameterDialog;
import delfos.configureddatasets.ConfiguredDataset;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.RecommendationModelBuildingProgressListener;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.recommendation.Recommendations;
import delfos.view.neighborhood.components.iknn.KnnModelCFRSRecommendationsGUI;
import delfos.view.neighborhood.components.uknn.KnnMemoryCFRSRecommendationsGUI;
import delfos.view.neighborhood.results.RecommendationsDefaultGUI;
import delfos.view.neighborhood.results.RecommendationsGUI;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RecommendationsExplainedWindow extends JFrame {

    public static final long serialVersionUID = 1L;

    private static final String TARGET_USER_BORDER_TITLE = "User selection";
    private static final String DATASET_BORDER_TITLE = "Dataset";
    private static final String RECOMMENDER_SYSTEM_BORDER_TITLE = "Recommender System";

    private JLabel timeMessage;
    private JLabel progressMessage;
    private JProgressBar progressBar;

    private JComboBox<RecommenderSystem> recommenderSystemSelector;
    private JButton recommenderSystemParametersButton;

    private JComboBox<User> userSelector;
    private JComboBox<ConfiguredDataset> datasetLoaderSelector;
    private JButton datasetLoaderParametersButton;

    private JSpinner relevanceThresholdSelector;

    private RecommendationsGUI resultsPanel = new RecommendationsDefaultGUI();

    private RecommendationModelHolder recommendationModelHolder = new RecommendationModelHolder();

    private void addRecommenderSystemParametersButtonBehavior() {

        recommenderSystemParametersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditParameterDialog dialogoParametrosSR = new EditParameterDialog(RecommendationsExplainedWindow.this);
                RecommenderSystem rs = (RecommenderSystem) recommenderSystemSelected();

                dialogoParametrosSR.setParameterTaker(rs);
                dialogoParametrosSR.pack();
                dialogoParametrosSR.setVisible(true);
            }
        });
        recommenderSystemParametersButton.setEnabled(true);
    }

    private void addDatasetLoaderParametersButtonBehavior() {

        datasetLoaderParametersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                EditParameterDialog dialogoParametrosSR = new EditParameterDialog(RecommendationsExplainedWindow.this);
                DatasetLoader<? extends Rating> datasetLoader = configuredDatasetSelected().getDatasetLoader();

                dialogoParametrosSR.setParameterTaker(datasetLoader);
                dialogoParametrosSR.pack();
                dialogoParametrosSR.setVisible(true);
            }
        });
        datasetLoaderParametersButton.setEnabled(true);
    }

    public class RecommendationModelHolder {

        Object recommendationModel = null;

        private RecommendationModelHolder() {

        }

        public synchronized Object getRecommendationModel() {
            return recommendationModel;
        }

        private synchronized Object buildRecommendationModel() {
            ConfiguredDataset configuredDataset
                    = configuredDatasetSelected();

            RecommenderSystem<? extends Object> recommenderSystem = recommenderSystemSelected();
            return recommenderSystem.buildRecommendationModel(configuredDataset.getDatasetLoader());
        }

        public synchronized void reloadRecommendationModel() {

            recommendationModel = null;

            class ModelBuilder implements Runnable, RecommendationModelBuildingProgressListener {

                @Override
                public void run() {
                    Chronometer chronometer = new Chronometer();
                    recommenderSystemSelected().addRecommendationModelBuildingProgressListener(this);
                    recommendationModel = buildRecommendationModel();
                    computeRecommendations(recommendationModel);
                    recommenderSystemSelected().removeRecommendationModelBuildingProgressListener(this);

                    updateGUI(
                            "Recommendation model built",
                            "Built in " + DateCollapse.collapse(chronometer.getTotalElapsed()),
                            100);
                }

                @Override
                public void buildingProgressChanged(String actualJob, int percent, long remainingTime) {
                    updateGUI(
                            "Building recommendation model (" + actualJob + ")",
                            "Remaining time: " + DateCollapse.collapse(remainingTime),
                            percent);
                }

                private void updateGUI(String progressMessage, String timeMessage, int percent) {
                    RecommendationsExplainedWindow.this.progressMessage.setText(progressMessage);
                    RecommendationsExplainedWindow.this.timeMessage.setText(timeMessage);
                    RecommendationsExplainedWindow.this.progressBar.setValue(percent);
                }
            }

            ModelBuilder modelBuilder = new ModelBuilder();
            Thread thread = new Thread(modelBuilder);
            thread.start();
        }

    }

    public RecommendationModelHolder getRecommendationModelHolder() {
        return recommendationModelHolder;
    }

    /**
     * Crea la ventana general para la interacción con el módulo de
     * recomendaciones.
     */
    public RecommendationsExplainedWindow() {
        super("Recommendations - " + ManagementFactory.getRuntimeMXBean().getName());

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        initComponents();

        plugListeners();

        fillData();

        this.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        this.setLocation((d.width - this.getWidth()) / 2, (d.height - this.getHeight()) / 2);
        this.toFront();

    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(inputPanel(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        resultsPanel = getRecommenderSystemRecommendationGUI(recommenderSystemSelected());
        this.add(resultsPanel.getComponent(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelProgress(), constraints);
    }

    private Component panelProgress() {
        JPanel ret = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        this.progressMessage = new JLabel();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(progressMessage, constraints);

        this.timeMessage = new JLabel();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(timeMessage, constraints);

        this.progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        this.progressBar.setStringPainted(true);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(progressBar, constraints);

        return ret;
    }

    private Component panelRecommenderSystems() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder(RECOMMENDER_SYSTEM_BORDER_TITLE));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.recommenderSystemSelector = new JComboBox<>();
        ret.add(this.recommenderSystemSelector, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        recommenderSystemParametersButton = new JButton("Parameters");
        ret.add(recommenderSystemParametersButton, constraints);

        return ret;
    }

    private Component panelDatasetSelector() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder(DATASET_BORDER_TITLE));

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.datasetLoaderSelector = new JComboBox<>();
        ret.add(datasetLoaderSelector, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        datasetLoaderParametersButton = new JButton("Parameters");
        ret.add(datasetLoaderParametersButton, constraints);

        return ret;
    }

    private Component panelSelectorUsuario() {

        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder(TARGET_USER_BORDER_TITLE));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(new JLabel("User"), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        userSelector = new JComboBox();
        ret.add(userSelector, constraints);

        return ret;

    }

    private Component inputPanel() {
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        inputPanel.add(panelDatasetSelector(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        inputPanel.add(panelRecommenderSystems(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        inputPanel.add(panelSelectorUsuario(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        inputPanel.add(new JPanel(), constraints);

        return inputPanel;
    }

    private void fillData() {
        ConfiguredDataset[] configuredDatasets
                = ConfiguredDatasetsFactory.getInstance().getAllConfiguredDatasets().toArray(new ConfiguredDataset[0]);
        datasetLoaderSelector.setModel(new DefaultComboBoxModel<>(configuredDatasets));

        ConfiguredDataset configuredDataset = configuredDatasetSelected();
        reloadUsersSelector(configuredDataset.getDatasetLoader());

        RecommenderSystem[] recommenderSystems = new RecommenderSystem[2];

        recommenderSystems[0] = new KnnMemoryBasedCFRS();

        KnnModelBasedCFRS knnModelBasedCFRS = new KnnModelBasedCFRS();
        knnModelBasedCFRS.setParameterValue(KnnModelBasedCFRS.RELEVANCE_FACTOR, false);
        recommenderSystems[1] = knnModelBasedCFRS;
        recommenderSystemSelector.setModel(new DefaultComboBoxModel<>(recommenderSystems));
        addRecommenderSystemGUI(recommenderSystems[0]);

        recommendationModelHolder.reloadRecommendationModel();
    }

    private void reloadUsersSelector(DatasetLoader<? extends Rating> datasetLoader) throws IllegalStateException, CannotLoadUsersDataset {
        if (datasetLoader instanceof UsersDatasetLoader) {

            UsersDataset usersDataset = ((UsersDatasetLoader) datasetLoader).getUsersDataset();

            User[] users = new User[usersDataset.allIDs().size()];

            {
                List<User> usersList = usersDataset.allIDs().parallelStream().map((idUser) -> {
                    return ((User) usersDataset.get(idUser));
                }).collect(Collectors.toList());
                Collections.sort(usersList, User.BY_ID);
                users = usersList.toArray(new User[0]);
            }

            userSelector.setModel(new DefaultComboBoxModel(users));
        } else {
            throw new IllegalStateException("arg");
        }
    }

    private void plugListeners() {
        addDatasetLoaderListener();
        addDatasetLoaderParametersButtonBehavior();

        addRecommenderSystemListener();
        addRecommenderSystemParametersButtonBehavior();

        addUserListener();
    }

    private ParameterListener datasetLoaderParameterListener = () -> {
        ConfiguredDataset selectedItem = datasetLoaderSelector.getItemAt(datasetLoaderSelector.getSelectedIndex());
        DatasetLoader<? extends Rating> datasetLoader = selectedItem.getDatasetLoader();

        recommendationModelHolder.reloadRecommendationModel();
        reloadUsersSelector(datasetLoader);
    };

    private final ItemListener datasetLoaderSelectedListener = (ItemEvent e) -> {
        ConfiguredDataset item = (ConfiguredDataset) e.getItem();
        DatasetLoader<? extends Rating> datasetLoader = item.getDatasetLoader();

        switch (e.getStateChange()) {
            case ItemEvent.SELECTED:
                datasetLoader.addParammeterListener(datasetLoaderParameterListener);
                break;
            case ItemEvent.DESELECTED:
                datasetLoader.removeParammeterListener(datasetLoaderParameterListener);
                resultsPanel.clearData();
                break;
            default:
                break;
        }
    };

    private final ParameterListener recommenderSystemParameterListener = () -> {
        recommendationModelHolder.reloadRecommendationModel();
    };

    private final ItemListener recommenderSystemItemListener = (ItemEvent e) -> {
        RecommenderSystem recommenderSystem = (RecommenderSystem) e.getItem();
        switch (e.getStateChange()) {
            case ItemEvent.SELECTED:
                addRecommenderSystemGUI(recommenderSystem);
                break;
            case ItemEvent.DESELECTED:
                removeRecommenderSystemGUI(recommenderSystem);
                break;
            default:
                break;
        }

    };

    public void addRecommenderSystemGUI(RecommenderSystem recommenderSystem) {
        Global.showln("ADD rs: " + recommenderSystem.getAlias());

        this.remove(resultsPanel.getComponent());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        resultsPanel = getRecommenderSystemRecommendationGUI(recommenderSystemSelected());
        this.add(resultsPanel.getComponent(), constraints);
        this.setVisible(true);

        recommenderSystem.addParammeterListener(recommenderSystemParameterListener);
    }

    public void removeRecommenderSystemGUI(RecommenderSystem recommenderSystem) {
        Global.showln("REMOVE rs: " + recommenderSystem.getAlias());

        recommenderSystem.removeParammeterListener(recommenderSystemParameterListener);
        this.remove(resultsPanel.getComponent());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        resultsPanel = getRecommenderSystemRecommendationGUI(null);
        this.add(resultsPanel.getComponent(), constraints);
        this.setVisible(true);
    }

    private void addRecommenderSystemListener() {
        recommenderSystemSelector.addItemListener(recommenderSystemItemListener);
    }

    private void addDatasetLoaderListener() {
        this.datasetLoaderSelector.addItemListener(datasetLoaderSelectedListener);
    }

    private void addUserListener() {
        userSelector.addActionListener((actionEvent) -> {
            Object recommendationModel = recommendationModelHolder.getRecommendationModel();
            computeRecommendations(recommendationModel);
        });
    }

    private void computeRecommendations(Object recommendationModel) {

        SwingUtilities.invokeLater(() -> {
            User userSelected = userSelected();

            RecommenderSystem recommenderSystem = recommenderSystemSelected();
            DatasetLoader<? extends Rating> datasetLoader = configuredDatasetSelected().getDatasetLoader();

            OnlyNewItems onlyNewItems = new OnlyNewItems();
            Set<Item> candidateItems = onlyNewItems.candidateItems(datasetLoader, userSelected);

            Recommendations recommendations = recommenderSystem.recommendToUser(datasetLoader, recommendationModel, userSelected, candidateItems);

            resultsPanel.updateResult(datasetLoader, recommendationModel, userSelected, recommendations, candidateItems);
        });
    }

    public RecommenderSystem recommenderSystemSelected() {
        return recommenderSystemSelector.getItemAt(recommenderSystemSelector.getSelectedIndex());
    }

    public User userSelected() {
        return userSelector.getItemAt(userSelector.getSelectedIndex());
    }

    public ConfiguredDataset configuredDatasetSelected() {
        return datasetLoaderSelector.getItemAt(datasetLoaderSelector.getSelectedIndex());
    }

    private RecommendationsGUI getRecommenderSystemRecommendationGUI(RecommenderSystem recommenderSystem) {

        Global.showln("Updating interface for: " + recommenderSystem);

        if (recommenderSystem instanceof KnnMemoryBasedCFRS) {
            return new KnnMemoryCFRSRecommendationsGUI(this);
        } else if (recommenderSystem instanceof KnnModelBasedCFRS) {
            return new KnnModelCFRSRecommendationsGUI(this);
        } else {
            return new RecommendationsDefaultGUI();
        }
    }

}
