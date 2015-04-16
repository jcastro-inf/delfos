package delfos.view.rsbuilder;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import delfos.Path;
import delfos.common.Global;
import delfos.common.parameters.view.EditParameterDialog;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.factories.DatasetLoadersFactory;
import delfos.factories.RecommendationCandidatesSelectorFactory;
import delfos.factories.RecommendationsOutputMethodFactory;
import delfos.factories.RecommenderSystemsFactory;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.RecommenderSystemBuildingProgressListener;
import delfos.rs.RecommenderSystemBuildingProgressListener_default;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.persistence.PersistenceMethod;

/**
 * Ventana para elegir los parámetros de un sistema de recomendación
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 * @version 1.2 28-Mayo-2013 Ahora se puede elegir un método de persistencia
 * independientemente del dataset elegido.
 */
public class RSBuilderFrame extends Frame {

    public static final long serialVersionUID = 1L;
    private JComboBox<GenericRecommenderSystem> recommenderSelector;
    private JButton botonParametrosRecommenderSystemSelector;

    private JComboBox<DatasetLoader> datasetSelector;
    private JButton botonParametrosDatasetSelector;

    private JComboBox<RecommendationsOutputMethod> recommendationsOutputMethodSelector;
    private JButton botonParametrosRecommendationsOutputMethodSelector;

    private JComboBox<PersistenceMethod> persistenceMethodSelector;
    private JButton botonParametrosPersistenceMethodSelector;

    private JSpinner spinerRelevancia;

    private String configFile;
    private JButton saveConfig;
    private JButton saveAndBuild;

    private JProgressBar progreso;
    private JComboBox<RecommendationCandidatesSelector> recommendationCandidateSelectorComboBox;
    private JButton botonParametrosRecommendationCandidateSelector;

    /**
     * Constructor para la interfaz de selección de los parámetros de un sistema
     * de recomendación.
     *
     * @param configFile Fichero de configuración en el que se guardan los
     * parámetros.
     */
    public RSBuilderFrame(String configFile) {
        super("Build: Configure recommender");
        this.configFile = configFile;

        initComponents();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                System.out.println("Exiting");
                System.exit(0);
            }
        });

        this.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        this.setLocation((d.width - this.getWidth()) / 2, (d.height - this.getHeight()) / 2);
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        int grid_y = 0;

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = grid_y;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelRecommenderSystems(), constraints);

        grid_y++;

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = grid_y++;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelDatasetSelector(), constraints);

        grid_y++;

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = grid_y;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelPersistenceMethod(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = grid_y;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelCriterioRelevancia(), constraints);

        grid_y++;

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = grid_y;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelRecommendationCandidatesSelector(), constraints);

        grid_y++;

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = grid_y;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelRecommendationsOutputMethod(), constraints);

        grid_y++;

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = grid_y;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelAceptarCancelar(), constraints);
    }

    private Component panelRecommenderSystems() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Recommender Systems"));

        ArrayList<GenericRecommenderSystem> lista = new ArrayList<>();
        lista.addAll(RecommenderSystemsFactory.getInstance().getAllClasses());

        Object[] items = lista.toArray();

        this.recommenderSelector = new JComboBox(items);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(this.recommenderSelector, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        botonParametrosRecommenderSystemSelector = new JButton("Parameters");
        botonParametrosRecommenderSystemSelector.addActionListener((ActionEvent e) -> {
            EditParameterDialog dialogoParametrosSR = new EditParameterDialog(RSBuilderFrame.this, false);
            GenericRecommenderSystem rs = (GenericRecommenderSystem) recommenderSelector.getSelectedItem();

            dialogoParametrosSR.setParameterTaker(rs);
            dialogoParametrosSR.setVisible(true);
        });
        ret.add(botonParametrosRecommenderSystemSelector, constraints);

        return ret;
    }

    private Component panelDatasetSelector() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Dataset"));

        GridBagConstraints constraints = new GridBagConstraints();

        Collection<DatasetLoader> items = DatasetLoadersFactory.getInstance().getAllClasses();
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

            //Activa el boton si tiene dialogo de parametros y lo desactiva
            //en caso contrario
            botonParametrosDatasetSelector.setEnabled(cbdci.hasParameters());
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
        this.botonParametrosDatasetSelector = new JButton("Parameters");
        this.botonParametrosDatasetSelector.addActionListener((ActionEvent e) -> {
            EditParameterDialog dialog = new EditParameterDialog(RSBuilderFrame.this, false);
            dialog.setParameterTaker((DatasetLoader) datasetSelector.getSelectedItem());
            dialog.setVisible(true);
        });
        ret.add(botonParametrosDatasetSelector, constraints);

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
        ret.add(new JLabel("Rating threshold"), constraints);

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

    private Component panelAceptarCancelar() {
        JPanel ret = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        progreso = new JProgressBar(SwingConstants.HORIZONTAL);
        ret.add(progreso, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        saveConfig = new JButton("Save");
        saveConfig.addActionListener((ActionEvent ae) -> {
            saveConfig(false);
        });
        ret.add(saveConfig, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        saveAndBuild = new JButton("Save & build");
        saveAndBuild.addActionListener((ActionEvent ae) -> {
            RSBuilderFrame.this.saveConfig(true);
        });
        ret.add(saveAndBuild, constraints);

        return ret;
    }

    /**
     * Método para guardar el archivo de configuración con los parámetros
     * elegidos en la interfaz. Lanza un cuadro de diálogo para que el usuario
     * elija dónde y con qué nombre guardar el fichero de configuración.
     *
     * @param build Parámetro para indicar si se debe construir el modelo en
     * este momento o no.
     */
    private void saveConfig(boolean build) {
        JFileChooser jfc = new JFileChooser(Path.getPath());
        jfc.setFileFilter(new FileNameExtensionFilter("Recommender System Configuration (XML)", "xml"));
        jfc.setDialogTitle("Save recommender system configuration XML file");
        int option = jfc.showSaveDialog(RSBuilderFrame.this);
        if (option == JFileChooser.APPROVE_OPTION) {
            Path.setPath(jfc.getSelectedFile());
            configFile = jfc.getSelectedFile().getAbsolutePath();
            if (configFile.endsWith("." + RecommenderSystemConfigurationFileParser.CONFIGURATION_EXTENSION)) {
                configFile = configFile.substring(0, configFile.lastIndexOf("." + RecommenderSystemConfigurationFileParser.CONFIGURATION_EXTENSION));
            }
            final GenericRecommenderSystem rs_generic = (GenericRecommenderSystem) recommenderSelector.getSelectedItem();
            final DatasetLoader<? extends Rating> loader = (DatasetLoader) datasetSelector.getSelectedItem();
            final RelevanceCriteria relevanceCriteria = new RelevanceCriteria(((Number) spinerRelevancia.getValue()).floatValue());
            final PersistenceMethod persistenceMethod = (PersistenceMethod) persistenceMethodSelector.getSelectedItem();
            final RecommendationCandidatesSelector recommendationCandidatesSelector = recommendationCandidateSelectorComboBox.getItemAt(recommendationCandidateSelectorComboBox.getSelectedIndex());
            final RecommendationsOutputMethod recommendationsOutputMethod = (RecommendationsOutputMethod) recommendationsOutputMethodSelector.getSelectedItem();

            RecommenderSystemConfigurationFileParser.saveConfigFile(configFile, rs_generic, loader, relevanceCriteria, persistenceMethod, recommendationCandidatesSelector, recommendationsOutputMethod);

            class Worker extends SwingWorker<Void, Void> implements RecommenderSystemBuildingProgressListener {

                private String actualJob = "Starting";
                private boolean error = false;
                private Throwable errorException = null;
                private final GenericRecommenderSystem rs;

                public Worker(GenericRecommenderSystem rs) {
                    this.rs = rs;
                }

                @Override
                protected Void doInBackground() throws Exception {

                    try {
                        rs.addBuildingProgressListener(this);
                        rs.addBuildingProgressListener(new RecommenderSystemBuildingProgressListener_default(System.out, 10000));

                        PersistenceMethod persistenceTechnique = (PersistenceMethod) persistenceMethodSelector.getSelectedItem();

                        if (persistenceTechnique instanceof FilePersistence) {
                            FilePersistence filePersistence = (FilePersistence) persistenceTechnique;
                            buildingProgressChanged(actualJob, 0, -1);
                            Object model = rs.build(loader);
                            rs.saveModel(filePersistence, model);
                        } else {
                            System.out.println("");
                            if (persistenceTechnique instanceof DatabasePersistence) {
                                DatabasePersistence databasePersistence = (DatabasePersistence) persistenceTechnique;
                                buildingProgressChanged(actualJob, 0, -1);
                                Object model = rs.build(loader);
                                rs.saveModel(databasePersistence, model);

                            } else {
                                throw new IllegalStateException("The persistence technique is not recognised: " + persistenceTechnique.getName());
                            }
                        }

                        rs.removeBuildingProgressListener(this);
                    } catch (Throwable ex) {
                        JOptionPane.showMessageDialog(RSBuilderFrame.this, ex.getMessage() + "\n" + Arrays.toString(ex.getStackTrace()), "ERROR in build", JOptionPane.ERROR_MESSAGE);
                        error = true;
                        errorException = ex;
                        ex.printStackTrace(System.err);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    super.done();
                    saveAndBuild.setEnabled(true);
                    saveConfig.setEnabled(true);
                    if (!error) {
                        buildingProgressChanged("Finished", 100, -1);
                    } else {
                        buildingProgressChanged("Error in construction! " + errorException.getMessage(), 100, -1);
                    }
                    throw new ThreadDeath();
                }

                @Override
                public void buildingProgressChanged(String actualJob, int percent, long remainingSeconds) {
                    this.actualJob = actualJob;
                    setProgress(percent);

                    progreso.setValue(getProgress());
                    progreso.setString(actualJob);
                    progreso.setStringPainted(true);
                }
            }

            if (rs_generic instanceof GenericRecommenderSystem) {
                GenericRecommenderSystem rsa = rs_generic;
                final Worker w = new Worker(rs_generic);
                w.addPropertyChangeListener((PropertyChangeEvent pce) -> {
                    progreso.setValue(w.getProgress());
                    progreso.setString(w.actualJob);
                    progreso.setStringPainted(true);
                });

                if (build) {
                    saveConfig.setEnabled(false);
                    saveAndBuild.setEnabled(false);
                    try {
                        w.execute();
                    } catch (Throwable ex) {
                        Global.showError(ex);
                        saveAndBuild.setEnabled(true);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(RSBuilderFrame.this, "Error on building " + rs_generic.getName() + ":\n"
                        + "The recommender does not extends the GenericRecommenderSystem class.\n"
                        + "The configuration file has been saved.", "Recommender system not supported.", JOptionPane.ERROR_MESSAGE);
            }
        }

    }

    private Component panelPersistenceMethod() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Persistence method"));

        GridBagConstraints constraints = new GridBagConstraints();

        Collection<PersistenceMethod> items = new LinkedList<>();
        items.add(new FilePersistence());
        items.add(new DatabasePersistence());

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.persistenceMethodSelector = new JComboBox(items.toArray());
        ret.add(persistenceMethodSelector, constraints);

        persistenceMethodSelector.addActionListener((ActionEvent e) -> {
            PersistenceMethod persistenceMethod = (PersistenceMethod) persistenceMethodSelector.getSelectedItem();
            botonParametrosPersistenceMethodSelector.setEnabled(persistenceMethod.hasParameters());
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
        this.botonParametrosPersistenceMethodSelector = new JButton("Parameters");
        this.botonParametrosPersistenceMethodSelector.addActionListener((ActionEvent e) -> {
            EditParameterDialog dialog = new EditParameterDialog(RSBuilderFrame.this, false);
            dialog.setParameterTaker((PersistenceMethod) persistenceMethodSelector.getSelectedItem());
            dialog.setVisible(true);
        });
        ret.add(botonParametrosPersistenceMethodSelector, constraints);

        return ret;
    }

    private Component panelRecommendationCandidatesSelector() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Recommendation Candidates Item Selector"));

        GridBagConstraints constraints = new GridBagConstraints();

        Collection<RecommendationCandidatesSelector> methods = RecommendationCandidatesSelectorFactory.getInstance().getAllClasses();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.recommendationCandidateSelectorComboBox = new JComboBox<>(methods.toArray(new RecommendationCandidatesSelector[0]));
        ret.add(recommendationCandidateSelectorComboBox, constraints);

        recommendationCandidateSelectorComboBox.addActionListener((ActionEvent e) -> {
            int index = recommendationCandidateSelectorComboBox.getSelectedIndex();
            RecommendationCandidatesSelector persistenceMethod = recommendationCandidateSelectorComboBox.getItemAt(index);
            botonParametrosRecommendationCandidateSelector.setEnabled(persistenceMethod.hasParameters());
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
        this.botonParametrosRecommendationCandidateSelector = new JButton("Parameters");
        this.botonParametrosRecommendationCandidateSelector.addActionListener((ActionEvent e) -> {
            EditParameterDialog dialog = new EditParameterDialog(RSBuilderFrame.this, false);
            int selectedIndex = recommendationCandidateSelectorComboBox.getSelectedIndex();
            dialog.setParameterTaker(recommendationCandidateSelectorComboBox.getItemAt(selectedIndex));
            dialog.setVisible(true);
        });
        ret.add(botonParametrosRecommendationCandidateSelector, constraints);

        return ret;
    }

    private Component panelRecommendationsOutputMethod() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Recommendations Output Method"));

        GridBagConstraints constraints = new GridBagConstraints();

        Collection<RecommendationsOutputMethod> methods = RecommendationsOutputMethodFactory.getInstance().getAllClasses();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTH;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.recommendationsOutputMethodSelector = new JComboBox(methods.toArray());
        ret.add(recommendationsOutputMethodSelector, constraints);

        recommendationsOutputMethodSelector.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecommendationsOutputMethod persistenceMethod = (RecommendationsOutputMethod) recommendationsOutputMethodSelector.getSelectedItem();

                //Activa el boton si tiene dialogo de parametros y lo desactiva
                //en caso contrario
                botonParametrosRecommendationsOutputMethodSelector.setEnabled(persistenceMethod.hasParameters());
            }
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
        this.botonParametrosRecommendationsOutputMethodSelector = new JButton("Parameters");
        this.botonParametrosRecommendationsOutputMethodSelector.addActionListener((ActionEvent e) -> {
            EditParameterDialog dialog = new EditParameterDialog(RSBuilderFrame.this, false);
            dialog.setParameterTaker((RecommendationsOutputMethod) recommendationsOutputMethodSelector.getSelectedItem());
            dialog.setVisible(true);
        });
        ret.add(botonParametrosRecommendationsOutputMethodSelector, constraints);

        return ret;
    }
}
