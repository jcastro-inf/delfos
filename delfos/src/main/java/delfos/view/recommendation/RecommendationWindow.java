package delfos.view.recommendation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.swing.SwingWorker;
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
import delfos.common.parameters.view.EditParameterDialog;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.factories.RecommenderSystemsFactory;
import delfos.rs.RecommenderSystem;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;

/**
 * Clase que encapsula el funcionamiento de la interfaz destinada a la
 * realización de ejecuciones de evaluación con algoritmos de recomendación
 * colaborativos, es decir, algoritmos que predicen la valoración que el usuario
 * daría a un item no valorado basándose en las valoraciones del resto de
 * usuarios.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class RecommendationWindow extends JFrame {

    public static final long serialVersionUID = 1L;
    private JButton buildModel;
    private JLabel progressMessage;
    private JProgressBar progressBar;
    private JComboBox SRSelector;
    private JProgressBar generalProgressBar;
    private JComboBox datasetSelector;
    private JButton botonParametros;
    private EditParameterDialog dialogoParametrosSR;
    private JButton calcularRecomendaciones;
    private JSpinner spinerRelevancia;
    private JLabel remainingTime;
    private RecommendationsJTableModel recommendationsJTableModel;
    private RecommendationsTable recommendationsTable;
    private JComboBox selectorUsuario;

    /**
     * Crea la ventana general para la interacción con el módulo de
     * recomendaciones.
     */
    public RecommendationWindow() {
        super("Recommendations - " + ManagementFactory.getRuntimeMXBean().getName());
        initComponents();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

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
        this.add(panelSelectorUsuario(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelCriterioRelevancia(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelRecommenderSystems(), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelProgreso(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelDatasetSelector(), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 3;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelRecomendaciones(), constraints);
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
        ret.add(this.progressBar, constraints);

        this.generalProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        this.generalProgressBar.setStringPainted(true);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(this.generalProgressBar, constraints);
        buildModel = new JButton("Build model");
        buildModel.setEnabled(false);
        buildModel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calcularRecomendaciones.setEnabled(false);
                final DatasetLoader<? extends Rating> datasetLoader = (DatasetLoader) datasetSelector.getSelectedItem();
                final RecommenderSystemAdapter rs = (RecommenderSystemAdapter) SRSelector.getSelectedItem();
                final RelevanceCriteria rc = new RelevanceCriteria(((Number) spinerRelevancia.getValue()).floatValue());

                class worker extends SwingWorker<Void, Void> {

                    private boolean correcto = false;

                    @Override
                    protected Void doInBackground() {
                        rs.addRecommendationModelBuildingProgressListener((String actualJob, int percent, long remainingTime1) -> {
                            progressBar.setValue(percent * 2);
                            progressMessage.setText(actualJob);
                        });

                        try {
                            rs.buildRecommendationModel(datasetLoader);
                            correcto = true;
                        } catch (CannotLoadRatingsDataset | CannotLoadContentDataset | CannotLoadUsersDataset ex) {
                            Logger.getLogger(RecommendationWindow.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        if (this.correcto) {
                            calcularRecomendaciones.setEnabled(true);
                            progressBar.setValue(100);
                            progressMessage.setText("Finished");
                        } else {
                            progressBar.setValue(0);
                            progressMessage.setText("Error in construction");
                        }

                        throw new ThreadDeath();
                    }
                }
                worker w = new worker();
                w.execute();
            }
        });
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(buildModel, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.calcularRecomendaciones = new JButton("Compute recommendations");

        calcularRecomendaciones.setEnabled(false);
        ret.add(calcularRecomendaciones, constraints);
        this.calcularRecomendaciones.addActionListener((ActionEvent e) -> {
            try {
                RecommenderSystemAdapter<Object> recommenderSystem = (RecommenderSystemAdapter<Object>) SRSelector.getSelectedItem();
                int idUser = ((Number) selectorUsuario.getSelectedItem()).intValue();
                DatasetLoader<? extends Rating> datasetLoader = (DatasetLoader) datasetSelector.getSelectedItem();
                RelevanceCriteria relevanceCriteria = new RelevanceCriteria((Number) spinerRelevancia.getValue());
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

                Object model = recommenderSystem.buildRecommendationModel(datasetLoader);
                Collection<Recommendation> recommendations = recommenderSystem.recommendToUser(datasetLoader, model, idUser, noValoradas);
                RecommendationWindow.this.recommendationsJTableModel.setRecomendaciones(recommendations);
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
        });

        return ret;
    }

    private Component panelRecommenderSystems() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Recommender Systems"));

        this.SRSelector = new JComboBox(RecommenderSystemsFactory.getInstance().getRecommenderSystems().toArray());

        SRSelector.addActionListener((ActionEvent e) -> {
            calcularRecomendaciones.setEnabled(false);
        });

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(this.SRSelector, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        botonParametros = new JButton("Parameters");
        botonParametros.addActionListener((ActionEvent e) -> {
            dialogoParametrosSR = new EditParameterDialog(RecommendationWindow.this, false);
            RecommenderSystem rs = (RecommenderSystem) SRSelector.getSelectedItem();

            dialogoParametrosSR.setParameterTaker(rs);
            dialogoParametrosSR.setVisible(true);
        });
        ret.add(botonParametros, constraints);

        return ret;
    }

    private Component panelDatasetSelector() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Dataset"));

        GridBagConstraints constraints = new GridBagConstraints();

        Collection<DatasetLoader> allRatingsDatasetLoader = ConfiguredDatasetsFactory.getInstance().getAllConfiguredDatasetLoaders();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.datasetSelector = new JComboBox(allRatingsDatasetLoader.toArray());
        ret.add(datasetSelector, constraints);

        datasetSelector.addActionListener((ActionEvent e) -> {
            try {
                final DatasetLoader<? extends Rating> dl = (DatasetLoader) datasetSelector.getSelectedItem();
                try {
                    selectorUsuario.setModel(new DefaultComboBoxModel(dl.getRatingsDataset().allUsers().toArray()));
                    buildModel.setEnabled(true);
                } catch (CannotLoadContentDataset ex) {
                    Global.showError(ex);
                    selectorUsuario.setModel(new DefaultComboBoxModel(new Object[0]));
                    buildModel.setEnabled(false);
                }

                dl.addParammeterListener(() -> {
                    try {

                        try {
                            selectorUsuario.setModel(new DefaultComboBoxModel(dl.getRatingsDataset().allUsers().toArray()));
                        } catch (CannotLoadRatingsDataset ex) {
                            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                        }
                        buildModel.setEnabled(true);

                        return;
                    } catch (CannotLoadContentDataset ex) {
                        ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
                    } catch (CannotLoadRatingsDataset ex) {
                        ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                    }

                    selectorUsuario.setModel(new DefaultComboBoxModel<>(new Object[0]));
                    buildModel.setEnabled(false);
                });
                botonParametros.setEnabled(dl.getParameters().size() > 0);
            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            }
        });

        ((DatasetLoader) datasetSelector.getSelectedItem()).addParammeterListener(() -> {
            try {
                try {
                    selectorUsuario.setModel(new DefaultComboBoxModel<>(((DatasetLoader) datasetSelector.getSelectedItem()).getRatingsDataset().allUsers().toArray(new Integer[0])));
                } catch (CannotLoadRatingsDataset ex) {
                    ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
                }
                buildModel.setEnabled(true);

                return;

            } catch (CannotLoadContentDataset ex) {
                ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            }

            selectorUsuario.setModel(new DefaultComboBoxModel(new Object[0]));
            buildModel.setEnabled(false);
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
        this.botonParametros = new JButton("Parameters");
        this.botonParametros.addActionListener((ActionEvent e) -> {
            EditParameterDialog dialog = new EditParameterDialog(RecommendationWindow.this, false);
            dialog.setParameterTaker((DatasetLoader) datasetSelector.getSelectedItem());
            dialog.setVisible(true);
        });
        ret.add(botonParametros, constraints);

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
        this.spinerRelevancia = new JSpinner(new SpinnerNumberModel(4.0, 0.1, 5.0, 0.2));
        ret.add(spinerRelevancia, constraints);

        return ret;
    }

    /**
     * Establece el porcentaje del caso de estudio.
     *
     * @param percent Porcentaje completado.
     */
    public void caseStudyProgressChanged(int percent) {
        generalProgressBar.setValue(percent);
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
    protected void executionProgressChanged(String proceso, int percent, long remainingSeconds) {
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
        selectorUsuario = new JComboBox();
        selectorUsuario.addActionListener((ActionEvent e) -> {
            recommendationsJTableModel.setRecomendaciones(new ArrayList<>());
        });
        ret.add(selectorUsuario, constraints);

        return ret;

    }

    private Component panelRecomendaciones() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Recommendations"));
        this.recommendationsJTableModel = new RecommendationsJTableModel();
        this.recommendationsTable = new RecommendationsTable(recommendationsJTableModel);
        JScrollPane scroll = new JScrollPane(recommendationsTable);
        scroll.setMinimumSize(new Dimension(200, 200));
        scroll.setPreferredSize(new Dimension(200, 200));
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
}
