package delfos.view.neighborhood;

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
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsWithNeighbors;
import delfos.view.neighborhood.components.recommendations.RecommendationsTable;
import delfos.view.neighborhood.components.uknn.UserNeighborsTable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

/**
 *
 * @author jcastro
 */
public class UserUserNeighborhoodWindow extends JFrame {

    public static final long serialVersionUID = 1L;

    private JLabel remainingTime;
    private JLabel progressMessage;
    private JProgressBar progressBar;

    private JComboBox<RecommenderSystem> recommenderSystemSelector;
    private JComboBox<User> userSelector;
    private JComboBox<ConfiguredDataset> configuredDatasetSelector;

    private JSpinner relevanceThresholdSelector;

    private RecommendationsTable recommendationsTable;

    private UserNeighborsTable neighborsTable;

    RecommendationModelHolder recommendationModelHolder = new RecommendationModelHolder();

    class RecommendationModelHolder {

        Object recommendationModel = null;

        public synchronized Object getRecommendationModel() {
            if (recommendationModel == null) {
                recommendationModel = buildRecommendationModel();
            }
            return recommendationModel;
        }

        public synchronized Object buildRecommendationModel() {
            ConfiguredDataset configuredDataset
                    = configuredDatasetSelected();

            RecommenderSystem<? extends Object> recommenderSystem = recommenderSystemSelected();
            return recommenderSystem.buildRecommendationModel(configuredDataset.getDatasetLoader());
        }

        public synchronized void reloadRecommendationModel() {
            recommendationModel = null;
            recommendationModel = buildRecommendationModel();
        }

    }

    /**
     * Crea la ventana general para la interacción con el módulo de
     * recomendaciones.
     */
    public UserUserNeighborhoodWindow() {
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
        this.add(panelResults(), constraints);

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

        this.remainingTime = new JLabel();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(remainingTime, constraints);

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
        ret.setBorder(BorderFactory.createTitledBorder("Recommender Systems"));

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
        recommenderSystemParameters.addActionListener((ActionEvent e) -> {
            EditParameterDialog recommenderSystemParameterDialog = new EditParameterDialog(UserUserNeighborhoodWindow.this, false);
            RecommenderSystem rs = recommenderSystemSelected();

            recommenderSystemParameterDialog.setParameterTaker(rs);
            recommenderSystemParameterDialog.setVisible(true);
        });
        ret.add(recommenderSystemParameters, constraints);

        return ret;
    }

    private Component panelDatasetSelector() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Dataset"));

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

    private Component panelCriterioRelevancia() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Relevance criteria"));

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
        ret.add(new JLabel("Relevance threshold"), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.relevanceThresholdSelector = new JSpinner(new SpinnerNumberModel(4.0, 0.1, 5.0, 0.2));
        ret.add(relevanceThresholdSelector, constraints);

        return ret;
    }

    private Component panelSelectorUsuario() {

        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("User selection"));

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

    private Component panelRecomendaciones() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Recommendations"));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.recommendationsTable = new RecommendationsTable();
        ret.add(recommendationsTable.getComponent(), constraints);

        return ret;
    }

    private Component panelNeighbors() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Neighbors"));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.neighborsTable = new UserNeighborsTable();
        ret.add(neighborsTable.getComponent(), constraints);
        return ret;
    }

    private Component panelRatings() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Ratings"));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.recommendationsTable = new RecommendationsTable();
        ret.add(recommendationsTable.getComponent(), constraints);
        return ret;
    }

    private Component panelResults() {
        GridBagConstraints constraints = new GridBagConstraints();
        JPanel results = new JPanel();
        results.setLayout(new GridBagLayout());

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        results.add(panelRecomendaciones(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        results.add(panelNeighbors(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        results.add(panelRatings(), constraints);

        JScrollPane scroll = new JScrollPane();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        scroll.add(results, constraints);

        return scroll;
    }

    private Component inputPanel() {
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridBagLayout());
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
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        inputPanel.add(panelCriterioRelevancia(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        inputPanel.add(new JPanel(), constraints);

        return inputPanel;
    }

    private void fillData() {
        RecommenderSystem[] recommenderSystems = new RecommenderSystem[2];
        recommenderSystems[0] = new KnnMemoryBasedCFRS();
        recommenderSystems[1] = new KnnModelBasedCFRS();
        recommenderSystemSelector.setModel(new DefaultComboBoxModel<>(recommenderSystems));

        ConfiguredDataset[] configuredDatasets
                = ConfiguredDatasetsFactory.getInstance().getAllConfiguredDatasets().toArray(new ConfiguredDataset[0]);
        configuredDatasetSelector.setModel(new DefaultComboBoxModel<>(configuredDatasets));

        ConfiguredDataset configuredDataset = configuredDatasetSelected();
        reloadUsersSelector(configuredDataset.getDatasetLoader());

        reloadRecommendations();
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
        reloadRecommendations();
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
                break;
            default:
                break;
        }
    };

    private final ParameterListener recommenderSystemParameterListener = () -> {
        recommendationModelHolder.reloadRecommendationModel();
        reloadRecommendations();
    };

    private final ItemListener recommenderSystemItemListener = (ItemEvent e) -> {
        RecommenderSystem recommenderSystemSelected = recommenderSystemSelected();
        switch (e.getStateChange()) {
            case ItemEvent.SELECTED:
                recommenderSystemSelected.addParammeterListener(recommenderSystemParameterListener);
                break;
            case ItemEvent.DESELECTED:
                recommenderSystemSelected.removeParammeterListener(recommenderSystemParameterListener);
                break;
            default:
                break;
        }

    };

    private void addRecommenderSystemListener() {
        recommenderSystemSelector.addItemListener(recommenderSystemItemListener);
    }

    private void addDatasetLoaderListener() {
        this.configuredDatasetSelector.addItemListener(datasetLoaderSelectedListener);
    }

    private void addUserListener() {
        userSelector.addActionListener((actionEvent) -> reloadRecommendations());
    }

    private void reloadRecommendations() {
        Object recommendationModel = recommendationModelHolder.getRecommendationModel();

        User userSelected = userSelected();

        RecommenderSystem recommenderSystem = recommenderSystemSelected();
        DatasetLoader<? extends Rating> datasetLoader = configuredDatasetSelected().getDatasetLoader();

        OnlyNewItems onlyNewItems = new OnlyNewItems();
        Set<Item> candidateItems = onlyNewItems.candidateItemsNew(datasetLoader, userSelected);

        Recommendations recommendations = recommenderSystem.recommendToUser(datasetLoader, recommendationModel, userSelected, candidateItems);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw(3);

        output.writeRecommendations(recommendations);

        recommendationsTable.setRecomendaciones(recommendations);

        if (recommendations instanceof RecommendationsWithNeighbors) {
            RecommendationsWithNeighbors recommendationsWithNeighbors = (RecommendationsWithNeighbors) recommendations;
            neighborsTable.setNeighbors(recommendationsWithNeighbors);
        }

    }

    public RecommenderSystem recommenderSystemSelected() {
        return recommenderSystemSelector.getItemAt(recommenderSystemSelector.getSelectedIndex());
    }

    private User userSelected() {
        return userSelector.getItemAt(userSelector.getSelectedIndex());
    }

    private ConfiguredDataset configuredDatasetSelected() {
        return configuredDatasetSelector.getItemAt(configuredDatasetSelector.getSelectedIndex());
    }

}
