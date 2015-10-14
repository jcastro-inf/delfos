package delfos.view.neighborhood;

import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.parameters.ParameterListener;
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

/**
 *
 * @author jcastro
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
    private JComboBox<User> userSelector;
    private JComboBox<ConfiguredDataset> configuredDatasetSelector;
    private JSpinner relevanceThresholdSelector;

    private RecommendationsGUI resultsPanel = new RecommendationsDefaultGUI();

    private RecommendationModelHolder recommendationModelHolder = new RecommendationModelHolder();

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
                    Recommendations recommendations = computeRecommendations(recommendationModel);
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
        JButton recommenderSystemParameters = new JButton("Parameters");
        recommenderSystemParameters.setEnabled(false);
        ret.add(recommenderSystemParameters, constraints);

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
        this.configuredDatasetSelector = new JComboBox<>();
        ret.add(configuredDatasetSelector, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        JButton datasetLoaderParameters = new JButton("Parameters");
        datasetLoaderParameters.setEnabled(false);
        ret.add(datasetLoaderParameters, constraints);

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
        configuredDatasetSelector.setModel(new DefaultComboBoxModel<>(configuredDatasets));

        ConfiguredDataset configuredDataset = configuredDatasetSelected();
        reloadUsersSelector(configuredDataset.getDatasetLoader());

        RecommenderSystem[] recommenderSystems = new RecommenderSystem[2];
        recommenderSystems[0] = new KnnMemoryBasedCFRS();
        recommenderSystems[1] = new KnnModelBasedCFRS();
        recommenderSystemSelector.setModel(new DefaultComboBoxModel<>(recommenderSystems));
        addRecommenderSystemGUI(recommenderSystems[0]);

        recommendationModelHolder.reloadRecommendationModel();
    }

    private void reloadUsersSelector(DatasetLoader<? extends Rating> datasetLoader) throws IllegalStateException, CannotLoadUsersDataset {
        if (datasetLoader instanceof UsersDatasetLoader) {

            UsersDataset usersDataset = ((UsersDatasetLoader) datasetLoader).getUsersDataset();

            User[] users = new User[usersDataset.getAllID().size()];

            {
                List<User> usersList = usersDataset.getAllID().parallelStream().map((idUser) -> {
                    return ((User) usersDataset.get(idUser));
                }).collect(Collectors.toList());
                Collections.sort(usersList, (User o1, User o2) -> o1.getId() - o2.getId());
                users = usersList.toArray(new User[0]);
            }

            userSelector.setModel(new DefaultComboBoxModel(users));
        } else {
            throw new IllegalStateException("arg");
        }
    }

    private void plugListeners() {
        addDatasetLoaderListener();
        addRecommenderSystemListener();
        addUserListener();
    }

    private ParameterListener datasetLoaderParameterListener = () -> {
        ConfiguredDataset selectedItem = configuredDatasetSelector.getItemAt(configuredDatasetSelector.getSelectedIndex());
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
        System.out.println("ADD rs: " + recommenderSystem.getAlias());

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
        System.out.println("REMOVE rs: " + recommenderSystem.getAlias());

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
        this.configuredDatasetSelector.addItemListener(datasetLoaderSelectedListener);
    }

    private void addUserListener() {
        userSelector.addActionListener((actionEvent) -> {
            Object recommendationModel = recommendationModelHolder.getRecommendationModel();
            computeRecommendations(recommendationModel);
        });
    }

    private Recommendations computeRecommendations(Object recommendationModel) {
        User userSelected = userSelected();

        RecommenderSystem recommenderSystem = recommenderSystemSelected();
        DatasetLoader<? extends Rating> datasetLoader = configuredDatasetSelected().getDatasetLoader();

        OnlyNewItems onlyNewItems = new OnlyNewItems();
        Set<Item> candidateItems = onlyNewItems.candidateItemsNew(datasetLoader, userSelected);

        Recommendations recommendations = recommenderSystem.recommendToUser(datasetLoader, recommendationModel, userSelected, candidateItems);

        resultsPanel.updateResult(datasetLoader, recommendationModel, userSelected, recommendations, candidateItems);
        return recommendations;
    }

    public RecommenderSystem recommenderSystemSelected() {
        return recommenderSystemSelector.getItemAt(recommenderSystemSelector.getSelectedIndex());
    }

    public User userSelected() {
        return userSelector.getItemAt(userSelector.getSelectedIndex());
    }

    public ConfiguredDataset configuredDatasetSelected() {
        return configuredDatasetSelector.getItemAt(configuredDatasetSelector.getSelectedIndex());
    }

    private RecommendationsGUI getRecommenderSystemRecommendationGUI(RecommenderSystem recommenderSystem) {

        System.out.println("Updating interface for: " + recommenderSystem);

        if (recommenderSystem instanceof KnnMemoryBasedCFRS) {
            return new KnnMemoryCFRSRecommendationsGUI();
        } else if (recommenderSystem instanceof KnnModelBasedCFRS) {
            return new KnnModelCFRSRecommendationsGUI(this);
        } else {
            return new RecommendationsDefaultGUI();
        }
    }

}
