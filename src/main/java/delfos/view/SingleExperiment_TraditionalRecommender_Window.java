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
package delfos.view;

import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.view.EditParameterDialog;
import delfos.configuration.scopes.SwingGUIScope;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.Experiment;
import delfos.experiment.ExperimentListener;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.DatasetLoadersFactory;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.factories.PredictionProtocolFactory;
import delfos.factories.RecommenderSystemsFactory;
import delfos.factories.ValidationTechniquesFactory;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommenderSystem;
import delfos.view.results.ResultsDialog;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Clase que encapsula el funcionamiento de la interfaz destinada a la realización de ejecuciones de evaluación con
 * algoritmos de recomendación colaborativos, es decir, algoritmos que predicen la valoración que el usuario daría a un
 * item no valorado basándose en las valoraciones del resto de usuarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class SingleExperiment_TraditionalRecommender_Window extends JFrame implements ExperimentListener {

    public static final long serialVersionUID = 1L;
    private static boolean ejecutando;
    JButton ejecutar;
    private JSpinner spinerN;
    private JLabel executionProgressMessage;
    private JProgressBar executionProgressBar;
    private CaseStudy caseStudy;
    private ParallelExecution parallelExecution;
    private JComboBox<RecommenderSystem> SRSelector;
    private EvaluationMeasuresTable evaluationMeassuresTable;
    private EvaluationMeasuresJTableModel evaluationMeasuresTableModel;
    private JProgressBar experimentProgressBar;
    private JComboBox datasetSelector;
    private JButton botonParametros;
    private EditParameterDialog dialogoParametrosSR;
    private JButton guardarResultado;
    private JComboBox selectorValidacion;
    private JButton botonParametrosTecnicaValidacion;
    private JSpinner spinerRelevancia;
    private JLabel remainingTime;
    private JComboBox comboPredictionValidationTechniques;
    private JButton botonPredictionValidation;

    public SingleExperiment_TraditionalRecommender_Window(InitialFrame initialFrame) {
        super("Single User Recommender Systems Experimentation - " + ManagementFactory.getRuntimeMXBean().getName());
        initComponents();

        configureWindow(initialFrame);
    }

    private void configureWindow(InitialFrame initialFrame) {
        this.addWindowListener(new ComportamientoSubVentanas(initialFrame, this));

        this.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                saveWindowState();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                saveWindowState();
            }

            @Override
            public void componentShown(ComponentEvent e) {
                saveWindowState();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                saveWindowState();
            }

            public void saveWindowState() {
                SwingGUIScope.getInstance().saveSingleUserExperimentWindowProperties(
                        SingleExperiment_TraditionalRecommender_Window.this);

            }
        });
        this.pack();

        SwingGUIScope.getInstance().loadSingleUserExperimentWindowProperties(this);
        this.toFront();
    }

    private void initComponents() {
        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 2;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelEvaluationMeasures(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.gridx = 1;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelCriterioRelevancia(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelRecommenderSystems(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelValidationParameters(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelDatasetSelector(), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelProgreso(), constraints);
    }

    private Component panelProgreso() {
        JPanel ret = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        this.executionProgressMessage = new JLabel();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(executionProgressMessage, constraints);

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

        this.executionProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        this.executionProgressBar.setStringPainted(true);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(this.executionProgressBar, constraints);

        this.experimentProgressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        this.experimentProgressBar.setStringPainted(true);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(this.experimentProgressBar, constraints);

        ejecutar = new JButton("Execute");
        ejecutar.addActionListener((ActionEvent e) -> {
            RecommenderSystem cbrs = (RecommenderSystem) SRSelector.getSelectedItem();
            Collection<EvaluationMeasure> ems = evaluationMeasuresTableModel.getSelectedEvaluationMeasures();
            boolean doExecution = false;
            if (cbrs.isRatingPredictorRS()) {
                doExecution = true;
            } else {
                for (EvaluationMeasure em : ems) {
                    if (em.usesRatingPrediction()) {
                        doExecution = false;
                    }
                }
            }

            if (doExecution == false) {
                int n = JOptionPane.showOptionDialog(
                        SingleExperiment_TraditionalRecommender_Window.this,
                        "There are evaluation measures which require rating prediction \n"
                        + "and the selected recommender system does not predict ratings. \n"
                        + "\n"
                        + "Do you wish to execute anyway or to revise the evaluation measures selection?",
                        "Evaluation Measures Selection",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE,
                        null,
                        new String[]{"Execute case", "Revise evaluation measures"}, // this is the array
                        "Revise evaluation measures");

                if (n == JOptionPane.NO_OPTION) {
                    setProgress("Unselect prediction-based evaluation measures", 0, 0);
                    return;
                }
            }

            //Si llega aquí, ha elegido ejecutar.
            SingleExperiment_TraditionalRecommender_Window.this.setProgress("Starting", 0, 0);
            SingleExperiment_TraditionalRecommender_Window.ejecutando = true;
            guardarResultado.setEnabled(false);
            ejecutar.setEnabled(false);
            ValidationTechnique validation = (ValidationTechnique) selectorValidacion.getSelectedItem();
            int n = (Integer) spinerN.getValue();
            DatasetLoader<? extends Rating> mdc = (DatasetLoader) datasetSelector.getSelectedItem();

            RelevanceCriteria rc = new RelevanceCriteria(((Number) spinerRelevancia.getValue()).doubleValue());

            PredictionProtocol pvt = (PredictionProtocol) comboPredictionValidationTechniques.getSelectedItem();

            caseStudy = CaseStudy.create(cbrs, mdc, validation, pvt, rc, ems, n);
            if (parallelExecution != null) {
                parallelExecution.cancel(true);
            }
            parallelExecution = new ParallelExecution(caseStudy, SingleExperiment_TraditionalRecommender_Window.this);

            caseStudy.addExperimentListener(SingleExperiment_TraditionalRecommender_Window.this);
            parallelExecution.execute();
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

        ret.add(ejecutar, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.guardarResultado = new JButton("Save result");

        guardarResultado.setEnabled(false);
        ret.add(guardarResultado, constraints);

        this.guardarResultado.addActionListener((ActionEvent e) -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Save result to XML");
            chooser.setCurrentDirectory(SwingGUIScope.getInstance().getCurrentDirectory());

            String[] extensions = {CaseStudyXML.RESULT_EXTENSION};
            chooser.setFileFilter(new FileNameExtensionFilter("Extensible Markup Language", extensions));

            boolean ok = false;
            while (!ok) {
                int opcion = chooser.showSaveDialog(SingleExperiment_TraditionalRecommender_Window.this);
                if (opcion == JFileChooser.APPROVE_OPTION) {
                    File selected = chooser.getSelectedFile();
                    String nombre = selected.getAbsolutePath();
                    SwingGUIScope.getInstance().setCurrentDirectory(selected);

                    if (!nombre.toLowerCase().endsWith("." + CaseStudyXML.RESULT_EXTENSION)) {
                        // Add correct extension
                        nombre += "." + CaseStudyXML.RESULT_EXTENSION;
                    }
                    File tmp = new File(nombre);
                    if (!tmp.exists() || JOptionPane.showConfirmDialog(SingleExperiment_TraditionalRecommender_Window.this, "File " + nombre + " already exists. Do you want to replace it?",
                            "Confirm", JOptionPane.YES_NO_OPTION, 3) == JOptionPane.YES_OPTION) {
                        CaseStudyXML.caseStudyToXMLFile(caseStudy, tmp);
                        ok = true;
                    }
                } else {
                    ok = true;
                }
            }
        });

        return ret;
    }

    private Component panelEvaluationMeasures() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Evaluation measures"));

        this.evaluationMeasuresTableModel = new EvaluationMeasuresJTableModel();
        EvaluationMeasuresFactory.getInstance().getAllClasses().stream().forEach((em) -> {
            evaluationMeasuresTableModel.addEvaluationMeasure(em);
        });

        this.evaluationMeassuresTable = new EvaluationMeasuresTable(evaluationMeasuresTableModel);
        evaluationMeassuresTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mousePressed(e);
                int selectedColumn = evaluationMeassuresTable.getSelectedColumn();
                final int selectedRow = evaluationMeassuresTable.getSelectedRow();

                if (selectedColumn == 0 && selectedRow != -1) {
                    evaluationMeasuresTableModel.setSeleccionFila(selectedRow);
                }

            }
        });

        JScrollPane scroll = new JScrollPane(evaluationMeassuresTable);

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

    private Component panelRecommenderSystems() {

        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Recommender Systems"));

        this.SRSelector = new JComboBox<>(
                RecommenderSystemsFactory.getInstance().getRecommenderSystems().toArray(new RecommenderSystem[0]));
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
            dialogoParametrosSR = new EditParameterDialog(SingleExperiment_TraditionalRecommender_Window.this, ejecutando);
            RecommenderSystem rs = (RecommenderSystem) SRSelector.getSelectedItem();

            dialogoParametrosSR.setParameterTaker(rs);
            dialogoParametrosSR.pack();
            dialogoParametrosSR.setVisible(true);
        });
        ret.add(botonParametros, constraints);

        return ret;
    }

    public void executionFinished(boolean error) {
        ejecutando = false;
        ejecutar.setEnabled(true);
        guardarResultado.setEnabled(!error);
        SingleExperiment_TraditionalRecommender_Window.ejecutando = false;

        if (parallelExecution != null) {
            parallelExecution.cancel(true);
            parallelExecution = null;
        }

        if (!error) {
            executionProgressMessage.setText("Finished");
            ejecutar.setEnabled(true);

            //Guardo el resultado con nombre igual a la fecha
            CaseStudyXML.saveCaseResults(caseStudy);

            ResultsDialog.showResultsDialog(this, caseStudy);
        } else {
            executionProgressMessage.setText("Finished with errors");
        }
    }

    private Component panelValidationParameters() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Validation parameters"));

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(new JLabel("Validation technique"), constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        selectorValidacion = new JComboBox(ValidationTechniquesFactory.getInstance().getAllClasses().toArray(new ValidationTechnique[0]));
        ret.add(selectorValidacion, constraints);
        selectorValidacion.addActionListener((ActionEvent e) -> {
            ValidationTechnique selectedItem = (ValidationTechnique) selectorValidacion.getSelectedItem();
            botonParametrosTecnicaValidacion.setEnabled(selectedItem.hasParameters());
            botonParametrosTecnicaValidacion.setEnabled(selectedItem.getParameters().size() > 0);
        });

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.botonParametrosTecnicaValidacion = new JButton("Parameters");
        this.botonParametrosTecnicaValidacion.addActionListener((ActionEvent e) -> {
            ValidationTechnique vi = (ValidationTechnique) selectorValidacion.getSelectedItem();
            EditParameterDialog dialog = new EditParameterDialog(SingleExperiment_TraditionalRecommender_Window.this, ejecutando);
            dialog.setParameterTaker(vi);
            dialog.pack();
            dialog.setVisible(true);
        });
        ret.add(botonParametrosTecnicaValidacion, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(new JLabel("Prediction protocol"), constraints);

        this.comboPredictionValidationTechniques = new JComboBox(PredictionProtocolFactory.getInstance().getAllClasses().toArray());
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(this.comboPredictionValidationTechniques, constraints);
        comboPredictionValidationTechniques.addActionListener((ActionEvent e) -> {
            ParameterOwner selectedItem = (ParameterOwner) comboPredictionValidationTechniques.getSelectedItem();
            botonPredictionValidation.setEnabled(selectedItem.hasParameters());
            botonPredictionValidation.setEnabled(selectedItem.getParameters().size() > 0);
        });

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 2;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.botonPredictionValidation = new JButton("Parameters");
        this.botonPredictionValidation.addActionListener((ActionEvent e) -> {
            ParameterOwner selectedItem = (ParameterOwner) comboPredictionValidationTechniques.getSelectedItem();
            EditParameterDialog dialog = new EditParameterDialog(SingleExperiment_TraditionalRecommender_Window.this, ejecutando);
            dialog.setParameterTaker(selectedItem);
            dialog.pack();
            dialog.setVisible(true);
        });
        ret.add(botonPredictionValidation, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(new JLabel("N executions"), constraints);

        this.spinerN = new JSpinner(new SpinnerNumberModel(1, 1, 5000, 1));
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 2;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        ret.add(this.spinerN, constraints);

        return ret;
    }

    private Component panelDatasetSelector() {
        JPanel ret = new JPanel(new GridBagLayout());
        ret.setBorder(BorderFactory.createTitledBorder("Dataset"));

        GridBagConstraints constraints = new GridBagConstraints();

        Collection<DatasetLoader> allRatingsDatasetLoader = DatasetLoadersFactory.getInstance().getAllClasses();

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
            DatasetLoader<? extends Rating> cbdci = (DatasetLoader) datasetSelector.getSelectedItem();

            //Activa el boton si tiene dialogo de parametros y lo desactiva
            //en caso contrario
            botonParametros.setEnabled(cbdci.getParameters().size() > 0);
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
            EditParameterDialog dialog = new EditParameterDialog(SingleExperiment_TraditionalRecommender_Window.this, ejecutando);
            dialog.setParameterTaker((DatasetLoader) datasetSelector.getSelectedItem());
            dialog.pack();
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

    private Chronometer chrono;
    private int previousPercent;
    private String previousProceso;

    public void setProgress(String executionProgressMessage, int experimentProgressPercent, int executionProgressPercent) {
        this.experimentProgressBar.setValue(experimentProgressPercent);

        this.executionProgressMessage.setText(executionProgressMessage);
        this.executionProgressBar.setValue(executionProgressPercent);
    }

    @Override
    public void progressChanged(Experiment algorithmExperiment) {

        final String executionProgressTask = algorithmExperiment.getExecutionProgressTask();
        final int executionProgressPercent = algorithmExperiment.getExecutionProgressPercent();
        final long executionProgressRemainingTime = algorithmExperiment.getExecutionProgressRemainingTime();

        experimentProgressBar.setValue(algorithmExperiment.getExperimentProgressPercent());

        executionProgressMessage.setText(executionProgressTask);
        executionProgressBar.setValue(algorithmExperiment.getExecutionProgressPercent());
        if (chrono == null) {
            chrono = new Chronometer();
            chrono.reset();
            this.remainingTime.setText(DateCollapse.collapse(executionProgressRemainingTime));
            this.remainingTime.setVisible(true);
            this.previousPercent = executionProgressPercent;
            this.previousProceso = executionProgressTask;
        } else if (previousProceso.equals(executionProgressTask) && previousPercent == executionProgressPercent) {
            if (chrono.getPartialElapsed() > 5000) {
                this.remainingTime.setText(DateCollapse.collapse(executionProgressRemainingTime));
                this.remainingTime.setVisible(true);
                chrono.reset();
            }
        } else {
            this.previousPercent = executionProgressPercent;
            this.previousProceso = executionProgressTask;
            this.remainingTime.setVisible(false);
            this.remainingTime.setText(DateCollapse.collapse(executionProgressRemainingTime));
            this.remainingTime.setVisible(true);
            chrono.reset();
        }

        if (algorithmExperiment.isFinished()) {
            executionFinished(algorithmExperiment.hasErrors());
        }
    }

}
