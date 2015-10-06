package delfos.view.neighborhood;

import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.view.EditParameterDialog;
import delfos.configureddatasets.ConfiguredDataset;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.recommendation.Recommendation;
import delfos.view.recommendation.RecommendationsJTableModel;
import delfos.view.recommendation.RecommendationsTable;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
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
    private static Object recommendationModel;

    private JLabel remainingTime;
    private JLabel progressMessage;
    private JProgressBar progressBar;

    private JComboBox<RecommenderSystem> recommenderSystemSelector;

    private JComboBox<ConfiguredDataset> datasetLoaderSelector;
    private JButton datasetLoaderParameters;

    private JSpinner relevanceThresholdSelector;

    private RecommendationsJTableModel recommendationsJTableModel;
    private RecommendationsTable recommendationsTable;

    private JComboBox userSelector;

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
        this.add(panelResultados(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelProgreso(), constraints);
    }

    private Component panelProgreso() {
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
        datasetLoaderParameters = new JButton("Parameters");
        datasetLoaderParameters.addActionListener((ActionEvent e) -> {
            EditParameterDialog recommenderSystemParameterDialog = new EditParameterDialog(UserUserNeighborhoodWindow.this, false);
            RecommenderSystem rs = (RecommenderSystem) recommenderSystemSelector.getSelectedItem();

            recommenderSystemParameterDialog.setParameterTaker(rs);
            recommenderSystemParameterDialog.setVisible(true);
        });
        ret.add(datasetLoaderParameters, constraints);

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
        this.datasetLoaderParameters = new JButton("Parameters");

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
        userSelector.addActionListener((ActionEvent e) -> {
            if (recommendationModel == null) {
                recommendationsJTableModel.setRecomendaciones(new ArrayList<>());
            } else {
                computeRecommendations();
            }

        });
        ret.add(userSelector, constraints);

        return ret;

    }

    private Component panelRecomendaciones() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Recommendations"));
        this.recommendationsJTableModel = new RecommendationsJTableModel();
        this.recommendationsTable = new RecommendationsTable(recommendationsJTableModel);
        JScrollPane scroll = new JScrollPane(recommendationsTable);
        scroll.setMinimumSize(new Dimension(200, 200));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(scroll, constraints);
        return ret;
    }

    private Component panelNeighbors() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Neighbors"));
        this.recommendationsJTableModel = new RecommendationsJTableModel();
        this.recommendationsTable = new RecommendationsTable(recommendationsJTableModel);
        JScrollPane scroll = new JScrollPane(recommendationsTable);
        scroll.setMinimumSize(new Dimension(200, 200));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(scroll, constraints);
        return ret;
    }

    private Component panelRatings() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Ratings"));
        this.recommendationsJTableModel = new RecommendationsJTableModel();
        this.recommendationsTable = new RecommendationsTable(recommendationsJTableModel);
        JScrollPane scroll = new JScrollPane(recommendationsTable);
        scroll.setMinimumSize(new Dimension(200, 200));
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(scroll, constraints);
        return ret;
    }

    private Component panelResultados() {
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
        constraints.weighty = 1.0;
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

        return inputPanel;
    }

    private Chronometer chrono;
    private int previousPercent;
    private String previousProceso;

    /**
     * Lanza el evento de cambio en el progreso de ejecución.
     *
     * @param proceso Proceso que se ejecuta actualmente.
     * @param percent Porcentaje completado.
     * @param remainingSeconds Tiempo restante estimado.
     */
    private void executionProgressChanged(String proceso, int percent, long remainingSeconds) {
        progressMessage.setText(proceso);
        progressBar.setValue(percent);
        if (chrono == null) {
            chrono = new Chronometer();
            chrono.reset();
            this.remainingTime.setText(DateCollapse.collapse(remainingSeconds));
            this.remainingTime.setVisible(true);
            this.previousPercent = percent;
            this.previousProceso = proceso;
        } else {
            if (previousProceso.equals(proceso) && previousPercent == percent) {
                if (chrono.getPartialElapsed() > 5000) {
                    this.remainingTime.setText(DateCollapse.collapse(remainingSeconds));
                    this.remainingTime.setVisible(true);
                    chrono.reset();
                }
            } else {
                this.previousPercent = percent;
                this.previousProceso = proceso;
                this.remainingTime.setVisible(false);
                this.remainingTime.setText(DateCollapse.collapse(remainingSeconds));
                this.remainingTime.setVisible(true);
                chrono.reset();
            }
        }
    }

    private void fillData() {
        RecommenderSystem[] recommenderSystems = new RecommenderSystem[2];
        recommenderSystems[0] = new KnnMemoryBasedCFRS();
        recommenderSystems[1] = new KnnModelBasedCFRS();
        recommenderSystemSelector.setModel(new DefaultComboBoxModel<>(recommenderSystems));

        ConfiguredDataset[] configuredDatasets
                = ConfiguredDatasetsFactory.getInstance().getAllConfiguredDatasets().toArray(new ConfiguredDataset[0]);
        datasetLoaderSelector.setModel(new DefaultComboBoxModel<>(configuredDatasets));

        fillUsersSelector(((ConfiguredDataset) datasetLoaderSelector.getSelectedItem()).getDatasetLoader());

        fillRecommendationTable();
    }

    private void fillUsersSelector(DatasetLoader<? extends Rating> datasetLoader) throws IllegalStateException, CannotLoadUsersDataset {
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

        updateUsersWhenDatasetLoaderChanges();

        if (1 == 1) {
            return;
        }

        datasetLoaderSelector.addActionListener((ActionEvent e) -> {
            try {
                final DatasetLoader<? extends Rating> dl = ((ConfiguredDataset) datasetLoaderSelector.getSelectedItem()).getDatasetLoader();
                try {
                    userSelector.setModel(new DefaultComboBoxModel(dl.getRatingsDataset().allUsers().toArray()));

                } catch (CannotLoadContentDataset ex) {
                    Global.showError(ex);
                    userSelector.setModel(new DefaultComboBoxModel(new Object[0]));

                }

                dl.addParammeterListener(() -> {
                    try {

                        try {
                            userSelector.setModel(new DefaultComboBoxModel(dl.getRatingsDataset().allUsers().toArray()));
                        } catch (CannotLoadRatingsDataset ex) {
                            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                        }

                        return;
                    } catch (CannotLoadContentDataset ex) {
                        ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
                    } catch (CannotLoadRatingsDataset ex) {
                        ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                    }

                    userSelector.setModel(new DefaultComboBoxModel<>(new Object[0]));
                });
                datasetLoaderParameters.setEnabled(dl.getParameters().size() > 0);
            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            }
            recommendationModel = null;
        });

        ((DatasetLoader) datasetLoaderSelector.getSelectedItem()).addParammeterListener(() -> {
            try {
                try {
                    userSelector.setModel(new DefaultComboBoxModel<>(((DatasetLoader) datasetLoaderSelector.getSelectedItem()).getRatingsDataset().allUsers().toArray(new Integer[0])));
                } catch (CannotLoadRatingsDataset ex) {
                    ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                }
                return;

            } catch (CannotLoadContentDataset ex) {
                ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            }

            userSelector.setModel(new DefaultComboBoxModel(new Object[0]));
        });

        recommenderSystemSelector.addActionListener((ActionEvent e) -> {
            recommendationModel = null;
        });
    }

    private void computeRecommendations() throws RuntimeException {

        try {
            RecommenderSystemAdapter<Object> recommenderSystem = (RecommenderSystemAdapter<Object>) recommenderSystemSelector.getSelectedItem();
            int idUser = ((Number) userSelector.getSelectedItem()).intValue();
            DatasetLoader<? extends Rating> datasetLoader = (DatasetLoader) datasetLoaderSelector.getSelectedItem();
            RelevanceCriteria relevanceCriteria = new RelevanceCriteria((Number) relevanceThresholdSelector.getValue());
            final ContentDataset contentDataset;
            if (datasetLoader instanceof ContentDatasetLoader) {
                ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
                contentDataset = contentDatasetLoader.getContentDataset();
            } else {
                throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
            }
            Set<Integer> noValoradas = new TreeSet<>(contentDataset.allID());
            noValoradas.removeAll(datasetLoader.getRatingsDataset().getUserRated(idUser));
            recommendationsJTableModel.setContentDataset(contentDataset);

            Collection<Recommendation> recommendations = recommenderSystem.recommendToUser(datasetLoader, recommendationModel, idUser, noValoradas);
            UserUserNeighborhoodWindow.this.recommendationsJTableModel.setRecomendaciones(recommendations);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
        } catch (NotEnoughtUserInformation ex) {
            ERROR_CODES.USER_NOT_ENOUGHT_INFORMATION.exit(ex);
        }
    }

    private ParameterListener datasetLoaderParameterListener = () -> {
        ConfiguredDataset selectedItem = datasetLoaderSelector.getItemAt(datasetLoaderSelector.getSelectedIndex());
        DatasetLoader<? extends Rating> datasetLoader = selectedItem.getDatasetLoader();
        fillUsersSelector(datasetLoader);
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

    private void updateUsersWhenDatasetLoaderChanges() {
        this.datasetLoaderSelector.addItemListener(datasetLoaderSelectedListener);
    }

    class RecommendationModelHolder {

        Object recommendationModel = null;

        public void buildRecommendationModel() {
            ConfiguredDataset configuredDataset
                    = (ConfiguredDataset) datasetLoaderSelector.getSelectedItem();

            RecommenderSystem<? extends Object> recommenderSystem
                    = (RecommenderSystem<? extends Object>) recommenderSystemSelector
                    .getSelectedItem();
        }

        public void setRecommendationModel(Object recommendationModel) {
            recommendationModelChanged();
        }

        private void recommendationModelChanged() {

        }

    }

    private void fillRecommendationTable() {

    }

}
