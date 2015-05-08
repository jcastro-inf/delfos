package delfos.common.parameters.view;

import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.DatasetLoaderParameterRestriction;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.LongParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.PasswordParameter;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.common.parameters.restriction.StringParameter;
import delfos.configuration.scopes.SwingGUIScope;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

/**
 * Clase que muestra los parámetros de un {@link ParameterOwner} y permite su
 * modificación mediante una interfaz swing
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date.
 * @version 1.1 (18-02-2013)
 */
public class EditParameterDialog extends JDialog {

    public static final long serialVersionUID = 1L;
    /**
     * Mapa que almacena los nuevos valores de los parámetros que se desean
     * modificar.
     */
    private Map<Parameter, Object> parametrosModificados;
    private JPanel panel = null;
    private ParameterOwner parameterOwner;
    private final boolean onlyRead;

    /**
     * Muestra un cuadro de diálogo en Swing para visualizar/editar los
     * parámetros de un objeto de tipo {@link ParameterOwner}
     *
     * @param owner Ventana que contiene el diálogo.
     * @param onlyRead True si se desea bloquear el cuadro de diálogo para
     * hacerlo de solo lectura.
     */
    public EditParameterDialog(Frame owner, boolean onlyRead) {
        super(owner, "", true);
        setLayout(new GridBagLayout());
        this.onlyRead = onlyRead;
    }

    /**
     * Muestra un cuadro de diálogo en Swing para visualizar/editar los
     * parámetros de un objeto de tipo {@link ParameterOwner}
     *
     * @param owner Ventana que contiene el diálogo.
     * @param onlyRead True si se desea bloquear el cuadro de diálogo para
     * hacerlo de solo lectura.
     */
    public EditParameterDialog(JDialog owner, boolean onlyRead) {
        super(owner, "", true);
        setLayout(new GridBagLayout());
        this.onlyRead = onlyRead;
    }

    /**
     * Establece el objeto de tipo {@link ParameterOwner} para el que se
     * visualizan y editan sus parámetros.
     *
     * @param _parameterOwner Objeto para el que se muestran/editan sus
     * parámetros.
     */
    public final void setParameterTaker(final ParameterOwner _parameterOwner) {
        this.parameterOwner = _parameterOwner;
        this.setTitle("Parameters of '" + _parameterOwner.getName() + "'");
        if (panel != null) {
            this.remove(panel);
        }

        parametrosModificados = new LinkedHashMap<>();
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        int y = 0;
        for (final Parameter p : this.parameterOwner.getParameters()) {
            constraints.fill = GridBagConstraints.HORIZONTAL;
            constraints.weightx = 0.0;
            constraints.weighty = 0.0;
            constraints.gridx = 0;
            constraints.gridy = y;
            constraints.gridwidth = 1;
            constraints.gridheight = 1;
            constraints.insets = new Insets(3, 4, 3, 4);
            panel.add(new JLabel(p.getName()), constraints);

            Object restriction = p.getRestriction();
            boolean widgetCreado = false;
            if (restriction instanceof FloatParameter) {
                FloatParameter npr = (FloatParameter) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                float value = ((Number) _parameterOwner.getParameterValue(p)).floatValue();
                float min = npr.getMin();
                float max = npr.getMax();
                float step = 0.01f;

                final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, step));
                spinner.setSize(50, spinner.getSize().height);
                spinner.setMaximumSize(new Dimension(500, spinner.getSize().height));
                spinner.addChangeListener((ChangeEvent e) -> {
                    EditParameterDialog.this.parametrosModificados.put(p, spinner.getValue());
                });
                panel.add(spinner, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof IntegerParameter) {
                IntegerParameter ipr = (IntegerParameter) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                int value = (Integer) _parameterOwner.getParameterValue(p);
                int min = ipr.getMin();
                int max = ipr.getMax();

                final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
                spinner.setSize(50, spinner.getSize().height);
                spinner.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        EditParameterDialog.this.parametrosModificados.put(p, spinner.getValue());
                    }
                });
                panel.add(spinner, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof LongParameter) {
                LongParameter ipr = (LongParameter) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                long value = (Long) _parameterOwner.getParameterValue(p);
                long min = ipr.getMin();
                long max = ipr.getMax();

                final JSpinner spinner = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
                spinner.setSize(50, spinner.getSize().height);
                spinner.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        long value1 = ((Number) spinner.getValue()).longValue();

                        EditParameterDialog.this.parametrosModificados.put(p, value1);
                    }
                });
                panel.add(spinner, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof FileParameter) {
                final FileParameter fpr = (FileParameter) p.getRestriction();
                File file = (File) _parameterOwner.getParameterValue(p);

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                final JButton botonElegirArchivo = new JButton(file.getName());
                botonElegirArchivo.setToolTipText(file.getAbsolutePath());
                botonElegirArchivo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        chooser.setDialogTitle("Select " + p.getName() + " file");

                        File currentDirectory = SwingGUIScope.getInstance().getCurrentDirectory();

                        chooser.setCurrentDirectory(currentDirectory);

                        chooser.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return fpr.getFileFilter().accept(f);
                            }

                            @Override
                            public String getDescription() {
                                return "Filtro del parámetro " + fpr.getName();
                            }
                        });

                        int opcion = chooser.showOpenDialog(EditParameterDialog.this);

                        if (opcion == JFileChooser.APPROVE_OPTION) {
                            SwingGUIScope.getInstance().setCurrentDirectory(chooser.getSelectedFile());
                            botonElegirArchivo.setText(chooser.getSelectedFile().getName());
                            botonElegirArchivo.setToolTipText(chooser.getSelectedFile().getAbsolutePath());
                            EditParameterDialog.this.parametrosModificados.put(p, chooser.getSelectedFile());
                        }
                    }
                });
                panel.add(botonElegirArchivo, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof ObjectParameter) {
                ObjectParameter opr = (ObjectParameter) p.getRestriction();
                Object[] allowed = opr.getAllowed();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                final JComboBox combo = new JComboBox(allowed);
                combo.addActionListener((ActionEvent e) -> {
                    EditParameterDialog.this.parametrosModificados.put(p, combo.getSelectedItem());
                });

                Object value = _parameterOwner.getParameterValue(p);
                int index = -1;
                for (int i = 0; i < allowed.length; i++) {
                    if (allowed[i].equals(value)) {
                        index = i;
                    }
                }
                combo.setSelectedIndex(index);
                panel.add(combo, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof BooleanParameter) {
//                BooleanParameter bpr = (BooleanParameter) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                final JCheckBox check = new JCheckBox();
                Boolean selected = (Boolean) _parameterOwner.getParameterValue(p);
                check.setSelected(selected);
                check.addActionListener((e) -> {
                    EditParameterDialog.this.parametrosModificados.put(p, check.isSelected());
                });

                panel.add(check, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof RecommenderSystemParameterRestriction) {
                RecommenderSystemParameterRestriction popr = (RecommenderSystemParameterRestriction) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                constraints.weighty = 0.0;
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);

                JPanel innerPanel = new JPanel(new GridBagLayout());
                Object[] allowed = popr.getAllowed();
                Object value = _parameterOwner.getParameterValue(p);
                int index = -1;
                for (int i = 0; i < allowed.length; i++) {
                    if (allowed[i].getClass().equals(value.getClass())) {
                        index = i;
                        allowed[i] = value;
                    }
                }

                final JComboBox combo = new JComboBox(allowed);
                combo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        EditParameterDialog.this.parametrosModificados.put(p, combo.getSelectedItem());
                    }
                });

                combo.setSelectedIndex(index);
                innerPanel.add(combo, constraints);

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = 0;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);

                JButton parametros = new JButton("Parameters");
                parametros.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ParameterOwner parameterOwner;
                        if (EditParameterDialog.this.parametrosModificados.containsKey(p)) {
                            parameterOwner = (ParameterOwner) EditParameterDialog.this.parametrosModificados.get(p);
                        } else {
                            parameterOwner = (ParameterOwner) _parameterOwner.getParameterValue(p);
                        }
                        EditParameterDialog editParameterDialog = new EditParameterDialog(EditParameterDialog.this, onlyRead);
                        editParameterDialog.setParameterTaker(parameterOwner);
                        editParameterDialog.setVisible(true);

                    }
                });
                innerPanel.add(parametros, constraints);

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                panel.add(innerPanel, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof DatasetLoaderParameterRestriction) {
                DatasetLoaderParameterRestriction dlpr = (DatasetLoaderParameterRestriction) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                constraints.weighty = 0.0;
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);

                JPanel innerPanel = new JPanel(new GridBagLayout());
                Object[] allowed = dlpr.getAllowed();
                Object value = _parameterOwner.getParameterValue(p);
                int index = -1;
                for (int i = 0; i < allowed.length; i++) {
                    if (allowed[i].getClass().equals(value.getClass())) {
                        index = i;
                        allowed[i] = value;
                    }
                }
                final JComboBox combo = new JComboBox(allowed);
                combo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        EditParameterDialog.this.parametrosModificados.put(p, combo.getSelectedItem());
                    }
                });
                combo.setSelectedIndex(index);
                innerPanel.add(combo, constraints);

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = 0;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);

                JButton parametros = new JButton("Parameters");
                parametros.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ParameterOwner parameterOwner;
                        if (EditParameterDialog.this.parametrosModificados.containsKey(p)) {
                            parameterOwner = (ParameterOwner) EditParameterDialog.this.parametrosModificados.get(p);
                        } else {
                            parameterOwner = (ParameterOwner) _parameterOwner.getParameterValue(p);
                        }
                        EditParameterDialog editParameterDialog = new EditParameterDialog(EditParameterDialog.this, onlyRead);
                        editParameterDialog.setParameterTaker(parameterOwner);
                        editParameterDialog.setVisible(true);

                    }
                });
                innerPanel.add(parametros, constraints);

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                panel.add(innerPanel, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof PasswordParameter) {
                PasswordParameter npr = (PasswordParameter) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);

                final String pass = (String) _parameterOwner.getParameterValue(p);
                final JPasswordField passField = new JPasswordField(pass);
                passField.setSize(50, passField.getSize().height);
                passField.setMaximumSize(new Dimension(500, passField.getSize().height));
                passField.addComponentListener(null);
                passField.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        String newPass = "";
                        char[] arrayPass = passField.getPassword();
                        for (char c : arrayPass) {
                            newPass += c;
                        }
                        if (!pass.equals(newPass)) {
                            EditParameterDialog.this.parametrosModificados.put(p, newPass);
                        }
                        passField.selectAll();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        String newPass = "";
                        char[] arrayPass = passField.getPassword();
                        for (char c : arrayPass) {
                            newPass += c;
                        }
                        if (!pass.equals(newPass)) {
                            EditParameterDialog.this.parametrosModificados.put(p, newPass);
                        }
                    }
                });

                panel.add(passField, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof StringParameter) {
                StringParameter npr = (StringParameter) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);

                final String stringValue = (String) _parameterOwner.getParameterValue(p);
                final JTextField textField = new JTextField(stringValue);
                textField.setSize(50, textField.getSize().height);
                textField.setMaximumSize(new Dimension(500, textField.getSize().height));
                textField.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        String newPass = textField.getText();
                        if (!stringValue.equals(newPass)) {
                            EditParameterDialog.this.parametrosModificados.put(p, newPass);
                        }
                        textField.selectAll();
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        String newPass = textField.getText();
                        if (!stringValue.equals(newPass)) {
                            EditParameterDialog.this.parametrosModificados.put(p, newPass);
                        }
                    }
                });

                panel.add(textField, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof ParameterOwnerRestriction) {
                ParameterOwnerRestriction parameterOwnerRestriction = (ParameterOwnerRestriction) p.getRestriction();

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 1.0;
                constraints.weighty = 0.0;
                constraints.gridx = 0;
                constraints.gridy = 0;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);

                JPanel innerPanel = new JPanel(new GridBagLayout());
                Object[] allowed = parameterOwnerRestriction.getAllowed();
                Object value = _parameterOwner.getParameterValue(p);
                int index = -1;
                for (int i = 0; i < allowed.length; i++) {
                    if (allowed[i].getClass().equals(value.getClass())) {
                        index = i;
                        allowed[i] = value;
                    }
                }

                final JComboBox combo = new JComboBox(allowed);
                combo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        EditParameterDialog.this.parametrosModificados.put(p, combo.getSelectedItem());
                    }
                });

                combo.setSelectedIndex(index);
                innerPanel.add(combo, constraints);

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = 0;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);

                JButton parametros = new JButton("Parameters");
                parametros.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ParameterOwner parameterOwner;
                        if (EditParameterDialog.this.parametrosModificados.containsKey(p)) {
                            parameterOwner = (ParameterOwner) EditParameterDialog.this.parametrosModificados.get(p);
                        } else {
                            parameterOwner = (ParameterOwner) _parameterOwner.getParameterValue(p);
                        }
                        EditParameterDialog editParameterDialog = new EditParameterDialog(EditParameterDialog.this, onlyRead);
                        editParameterDialog.setParameterTaker(parameterOwner);
                        editParameterDialog.setVisible(true);

                    }
                });
                innerPanel.add(parametros, constraints);

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                panel.add(innerPanel, constraints);
                widgetCreado = true;
            }

            if (restriction instanceof DirectoryParameter) {
                final DirectoryParameter fpr = (DirectoryParameter) p.getRestriction();
                File file = (File) _parameterOwner.getParameterValue(p);

                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.weightx = 0.0;
                constraints.weighty = 0.0;
                constraints.gridx = 1;
                constraints.gridy = y;
                constraints.gridwidth = 1;
                constraints.gridheight = 1;
                constraints.insets = new Insets(3, 4, 3, 4);
                final JButton botonElegirArchivo = new JButton(file.getName());
                botonElegirArchivo.setToolTipText(file.getAbsolutePath());
                botonElegirArchivo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();

                        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        chooser.setDialogTitle("Select directory as value of parameter " + p.getName() + " ");
                        chooser.setCurrentDirectory(SwingGUIScope.getInstance().getCurrentDirectory());

                        chooser.setFileFilter(new FileFilter() {
                            @Override
                            public boolean accept(File f) {
                                return f.isDirectory();
                            }

                            @Override
                            public String getDescription() {
                                return "Filtro del parámetro " + fpr.getName() + " (Solo directorios)";
                            }
                        });

                        int opcion = chooser.showOpenDialog(EditParameterDialog.this);

                        if (opcion == JFileChooser.APPROVE_OPTION) {
                            SwingGUIScope.getInstance().setCurrentDirectory(chooser.getSelectedFile());
                            botonElegirArchivo.setText(chooser.getSelectedFile().getName());
                            botonElegirArchivo.setToolTipText(chooser.getSelectedFile().getAbsolutePath());
                            EditParameterDialog.this.parametrosModificados.put(p, chooser.getSelectedFile());
                        }
                    }
                });
                panel.add(botonElegirArchivo, constraints);
                widgetCreado = true;
            }

            if (!widgetCreado) {
                Global.showWarning("No se ha creado widget para '" + p.getName() + "' con restriccion de tipo '" + p.getRestriction() + "'\n");
                Global.showError(new IllegalStateException("No se ha creado widget para '" + p.getName() + "' con restriccion de tipo '" + p.getRestriction() + "'\n"));
            }

            y++;
        }
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 3;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        this.add(panel, constraints);

        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        JButton cancelar = new JButton("Cancel");
        cancelar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!onlyRead) {
                    if (!parametrosModificados.isEmpty()) {
                        int n = JOptionPane.showConfirmDialog(
                                EditParameterDialog.this,
                                "Parameter modification will be discarded. Are you sure?",
                                "Parameter modification",
                                JOptionPane.YES_NO_OPTION);
                        if (n == JOptionPane.YES_OPTION) {
                            boolean correcto = true;
                            for (Parameter p : parametrosModificados.keySet()) {
                                Object newValue = parametrosModificados.get(p);
                                if (EditParameterDialog.this.parameterOwner.setParameterValue(p, newValue) == null) {
                                    Global.showWarning("No se pudo asignar el parámetro " + p.getName() + "=" + newValue + "\n");
                                    correcto = false;
                                }
                            }
                            if (correcto) {
                                EditParameterDialog.this.setVisible(false);
                                EditParameterDialog.this.dispose();
                            }
                        }
                    }
                }
                EditParameterDialog.this.setVisible(false);
                EditParameterDialog.this.dispose();
            }
        });
        this.add(cancelar, constraints);
        constraints.fill = GridBagConstraints.NONE;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        JButton aceptar = new JButton("Done");
        aceptar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!onlyRead) {
                    if (!parametrosModificados.isEmpty()) {
                        String s = "";
                        for (Parameter p : parametrosModificados.keySet()) {
                            s += p.getName() + ",";
                        }
                        s = s.substring(0, s.length() - 1);

                        int n = JOptionPane.showConfirmDialog(
                                EditParameterDialog.this,
                                "The following parameters will be modified: \n" + s,
                                "Parameter modification",
                                JOptionPane.YES_NO_OPTION);

                        if (n == JOptionPane.YES_OPTION) {
                            boolean correcto = true;
                            for (Parameter p : parametrosModificados.keySet()) {
                                Object newValue = parametrosModificados.get(p);
                                if (EditParameterDialog.this.parameterOwner.setParameterValue(p, newValue) == null) {
                                    Global.showWarning("Cannot make the parameter assignment: " + p.getName() + "=" + newValue + "\n");
                                    Global.showError(new IllegalStateException("Cannot make the parameter assignment: " + p.getName() + "=" + newValue + "\n"));
                                    correcto = false;
                                }
                            }
                            if (correcto) {
                                EditParameterDialog.this.setVisible(false);
                                EditParameterDialog.this.dispose();
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(EditParameterDialog.this, "Changes will be discarded due to the experiment is running.", "Runing experiment", JOptionPane.INFORMATION_MESSAGE);
                }
                EditParameterDialog.this.setVisible(false);
                EditParameterDialog.this.dispose();
            }
        });
        this.add(aceptar, constraints);
        this.pack();

        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((d.width - this.getWidth()) / 2, (d.height - this.getHeight()) / 2);

    }
}
