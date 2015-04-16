package delfos.view.resultanalysis;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

/**
 * Crea un CSV a partir de los resultados de varios casos de uso ejecutados.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 19-Noviembre-2013
 */
public class ResultAnalysisFrame extends JFrame {

    public static final long serialVersionUID = 1L;
    private FilesTableModel filesTableModel;
    private JTable filesTable;
    private JButton buttonAddFiles;
    private OutputTableModel outputTableModel;
    private JTable outputTable;
    private JButton buttonWriteCSV;

    private List<File> files = new LinkedList<File>();

    public ResultAnalysisFrame() throws HeadlessException {

        initComponents();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                System.exit(0);
            }
        });
        pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        setLocation((d.width - getWidth()) / 2, (d.height - getHeight()) / 2);
        toFront();

    }

    private void initComponents() {

        this.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelFiles(), constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panelOutput(), constraints);
    }

    private JComponent panelFiles() {

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.filesTableModel = new FilesTableModel();
        this.filesTable = new JTable(filesTableModel);
        JScrollPane scrollTable = new JScrollPane(filesTable);
        panel.add(scrollTable, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        buttonAddFiles = new JButton("Add files...");
        buttonAddFiles.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                chooser.setDialogTitle("Select caseStudy files");

                chooser.setMultiSelectionEnabled(true);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                int opcion = chooser.showOpenDialog(ResultAnalysisFrame.this);
                if (opcion == JFileChooser.APPROVE_OPTION) {

                    ResultAnalysisFrame.this.files.addAll(Arrays.asList(chooser.getSelectedFiles()));

                    filesTableModel.updateOutput(ResultAnalysisFrame.this.files.toArray(new File[0]));
                    outputTableModel.updateOutput(ResultAnalysisFrame.this.files.toArray(new File[0]));
                }
            }
        });
        panel.add(buttonAddFiles, constraints);

        return panel;
    }

    private JComponent panelOutput() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        this.outputTableModel = new OutputTableModel();
        this.outputTable = new JTable(outputTableModel);
        JScrollPane scrollTable = new JScrollPane(outputTable);
        panel.add(scrollTable, constraints);

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        buttonWriteCSV = new JButton("Write CSV");
        buttonWriteCSV.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    FileWriter fw = new FileWriter(System.currentTimeMillis() + "- ResultAnalyisis.csv");
                    String[][] matrix = outputTableModel.getCSVData();

                    StringBuilder s = new StringBuilder();

                    for (String[] row : matrix) {
                        for (String cell : row) {

                            try {
                                double value = Double.parseDouble(cell);
                                s.append(cell).append("\t");
                            } catch (Exception ex) {
                                s.append('"').append(cell).append('"').append("\t");
                            }
                        }
                        s.deleteCharAt(s.length() - 1);
                        s.append("\n");
                    }

                    fw.write(s.toString());
                    fw.flush();
                    fw.close();
                } catch (IOException ex) {
                    Logger.getLogger(ResultAnalysisFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        panel.add(buttonWriteCSV, constraints);

        return panel;
    }
}
