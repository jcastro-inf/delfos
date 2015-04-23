package delfos;

import static delfos.Constants.CONFIGURATION_DIRECTORY;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 * Clase que almacena un directorio. Se utiliza para, una vez encontrado un
 * archivo mediante un cuadro de diálogo, que se abra el siguiente cuadro de
 * diálogo para elegir ficheros en el mismo directorio
 *
 * @author Jorge Castro Gallardo
 */
public class Path extends ParameterOwnerAdapter {

    /**
     * Nombre del archivo donde se guardan los valores de configuración de las
     * rutas por defecto de la biblioteca de recomendación.
     */
    public static final File CONFIGURATION_FILE_NAME = new File(CONFIGURATION_DIRECTORY.getPath() + File.separator + "path.config");
    private static final long serialVersionUID = 1L;
    private static Path instance;

    /**
     * Para inicializar los parámetros de esta clase estática.
     */
    private static void init() {

        if (!CONFIGURATION_DIRECTORY.exists()) {
            boolean mkdir = CONFIGURATION_DIRECTORY.mkdir();
            if (!mkdir) {
                IOException ex = new IOException("Cannot create '" + CONFIGURATION_DIRECTORY.getAbsolutePath() + "' directory");
                ERROR_CODES.CANNOT_WRITE_LIBRARY_CONFIG_FILE.exit(ex);
            }
        } else {
            Global.showMessage("Configuration directory exists. (" + CONFIGURATION_DIRECTORY.getAbsolutePath() + ")\n");
        }

        if (instance == null) {
            instance = new Path();
        }
    }
    private String lastValidPath = ".";
    private String datasetDirectory;

    /**
     * Constructor por defecto de esta clase. Busca el archivo de configuración
     * para los directorios y si no existe, lanza un cuadro de diálogo para
     * elegir los valores y lo crea.
     */
    private Path() {
        if (CONFIGURATION_FILE_NAME.exists()) {
            readFile();
            File datasetDirectoryFile = new File(datasetDirectory);
            if (!datasetDirectoryFile.exists() || !datasetDirectoryFile.isDirectory()) {
                do {
                    datasetDirectory = getConfigurationParameters();
                } while (!new File(datasetDirectory).exists());

                writeFile();
            }
        } else {

            do {
                datasetDirectory = getConfigurationParameters();
            } while (!new File(datasetDirectory).exists());

            writeFile();
        }
    }

    /**
     * Establece el nuevo directorio a utilizar por el siguiente
     * {@link JFileChooser}
     *
     * @param newPath Nuevo directorio
     */
    public void setPath(String newPath) {
        init();

        File f = new File(newPath);
        if (f.isDirectory()) {
            instance.lastValidPath = f.getPath();
        } else {
            instance.lastValidPath = f.getParentFile().getPath();
        }
    }

    /**
     * Establece el nuevo directorio a utilizar por el siguiente
     * {@link JFileChooser}
     *
     * @param newPath Nuevo directorio
     */
    public static void setPath(File newPath) {
        init();

        if (newPath.isDirectory()) {
            instance.lastValidPath = newPath.getPath();
        } else {
            instance.lastValidPath = newPath.getParentFile().getPath();
        }
    }

    /**
     * Devuelve el directorio inicial en que se deben abrir los
     * {@link JFileChooser}
     *
     * @return directorio en el que abrir el cuadro de diálogo
     */
    public static File getPath() {
        init();
        return new File(instance.lastValidPath);
    }

    /**
     * Devuelve el directorio que almacena el repositorio de datasets.
     *
     * @return Repositorio de datasets.
     */
    public static String getDatasetDirectory() {
        init();
        return instance.datasetDirectory;
    }

    /**
     * Asigna un nuevo valor para el repositorio de datasets.
     *
     * @param datasetDirectory Nuevo directorio donde se almacena el repositorio
     * de datasets.
     *
     * @throws IllegalArgumentException Si el nuevo valor no es un directorio o
     * si el directorio especificado no existe.
     */
    public static void setDatasetDirectory(String datasetDirectory) {
        init();
        File f = new File(datasetDirectory);
        if (!f.exists()) {
            throw new IllegalArgumentException("Specified file not exists '" + datasetDirectory + "'");
        } else if (!f.isDirectory()) {
            throw new IllegalArgumentException("Specified string is not a directory'" + datasetDirectory + "'");
        } else {
            instance.datasetDirectory = datasetDirectory;
        }

    }

    /**
     * Lee el archivo de configuración de directorios.
     */
    private void readFile() {
        boolean correcto = false;
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(CONFIGURATION_FILE_NAME);

            Element config = doc.getRootElement();

            lastValidPath = config.getAttributeValue("path");
            datasetDirectory = config.getAttributeValue("datasetDirectory");

            File filePath = new File(lastValidPath);
            File fileDatasetDirectory = new File(datasetDirectory);

            if (!filePath.exists()) {
                Global.showWarning("directory '" + lastValidPath + "' doesn't exist");
                correcto &= false;
                if (!filePath.isDirectory()) {
                    Global.showWarning("'" + lastValidPath + "' is not a directory");
                    correcto &= false;
                }
            }

            if (!fileDatasetDirectory.exists()) {
                Global.showWarning("directory '" + datasetDirectory + "' doesn't exist");
                correcto &= false;
                if (!fileDatasetDirectory.isDirectory()) {
                    Global.showWarning(datasetDirectory + "' is not a directory");
                    correcto &= false;
                }
            }
            correcto = true;
        } catch (JDOMException ex) {
            ERROR_CODES.CANNOT_READ_LIBRARY_CONFIG_FILE.exit(ex);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_READ_LIBRARY_CONFIG_FILE.exit(ex);
        }
        if (!correcto) {
            getConfigurationParameters();
        }
    }

    /**
     * Escribe el archivo de configuración de directorios.
     */
    private void writeFile() {
        Element config = new Element("pathConfig");

        config.setAttribute("path", lastValidPath);
        config.setAttribute("datasetDirectory", datasetDirectory);

        Document doc = new Document(config);
        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
        try {
            FileWriter fileWriter = new FileWriter(CONFIGURATION_FILE_NAME);
            outputter.output(doc, fileWriter);
            fileWriter.close();
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_LIBRARY_CONFIG_FILE.exit(ex);
        }
    }

    /**
     * Solicita al usuario el directorio en que se almacena el repositorio de
     * datasets.
     *
     * @return Nuevo repositorio de datasets.
     */
    private String getConfigurationParameters() {
        final Semaphore s = new Semaphore(0);

        //Obtener los datos mediante un cuadro de diálogo sencillo en swing.
        final JFrame dialogo = new JFrame("Select dataset directory");
        JLabel textoDescriptivo = new JLabel("Directory of datasets");
        final JTextField textoRuta = new JTextField("Set a directory");
        JButton botonBuscarDirectorio = new JButton("...");
        final JButton botonOK = new JButton("Done");
        botonOK.setEnabled(false);

        botonOK.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                s.release();

                dialogo.setVisible(false);
                dialogo.dispose();

            }
        });

        dialogo.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        dialogo.add(textoDescriptivo, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        dialogo.add(textoRuta, constraints);
        textoRuta.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                validateTextoRuta(textoRuta.getText(), botonOK);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                validateTextoRuta(textoRuta.getText(), botonOK);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                validateTextoRuta(textoRuta.getText(), botonOK);
            }
        });

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        dialogo.add(botonBuscarDirectorio, constraints);
        botonBuscarDirectorio.addActionListener(new ActionListener() {
            private void validateTextoRuta(String textoRuta, JButton botonOK) {
                String directorio = textoRuta;
                File f = new File(directorio);
                botonOK.setEnabled(f.exists() && f.isDirectory());
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                //Sacar el dialogo para elegir directorios.
                JFileChooser chooser = new JFileChooser(".");
                chooser.setDialogTitle("Select directory of datasets");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                boolean ok = false;
                while (!ok) {

                    int opcion = chooser.showOpenDialog(dialogo);
                    if (opcion == JFileChooser.APPROVE_OPTION) {
                        File selected = chooser.getSelectedFile();
                        if (selected.isDirectory()) {
                            ok = true;

                            textoRuta.setText(selected.getPath());
                            validateTextoRuta(selected.getPath(), botonOK);
                        }
                    } else {
                        ok = true;
                    }
                }
            }
        });

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        dialogo.add(botonOK, constraints);

        dialogo.pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        dialogo.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }

        });

        dialogo.setLocation((d.width - dialogo.getWidth()) / 2, (d.height - dialogo.getHeight()) / 2);
        dialogo.toFront();
        dialogo.setVisible(true);
        try {
            s.acquire();
        } catch (InterruptedException ex) {
            Global.showError(ex);
        }

        //RELATIVIZE PATH
        //String relativePath
        return relativizePath(new File(new File(".").getAbsolutePath()), new File(textoRuta.getText()));
    }

    /**
     * Comprueba si el texto es una ruta a un directorio que existe. Si es asi,
     * activa el botón para aceptar la entrada.
     *
     * @param textoRuta Texto de la ruta.
     * @param botonOK Botón que se activa si el parámetro es válido o desactiva
     * si no lo es.
     */
    private static void validateTextoRuta(String textoRuta, JButton botonOK) {
        String directorio = textoRuta;
        File f = new File(directorio);
        botonOK.setEnabled(f.exists() && f.isDirectory());

    }

    /**
     * Convierte una ruta en ruta relativa, a partir de un directorio base.
     *
     * @param base Directorio a partir del cual se relativiza.
     * @param file Ruta a relativizar.
     * @return Ruta relativa al fichero.
     */
    public static String relativizePath(File base, File file) {
        Global.showMessage("Relativize:\n");
        Global.showMessage("base -> " + base + "\n");
        Global.showMessage("file -> " + file + "\n");
        String relativized;
        if (File.separator.equals("" + File.separator + "")) {

            //Estoy en windows, problemas
            String driveBase = base.getAbsolutePath().substring(0, 2);
            String driveFile = file.getAbsolutePath().substring(0, 2);

            if (!driveBase.equals(driveFile)) {
                return file.getAbsolutePath();
            }

            String basePath = base.getAbsolutePath().substring(2);
            String filePath = file.getAbsolutePath().substring(2);

            if (basePath.endsWith(".")) {
                basePath = basePath.substring(0, basePath.length() - 1);
            }

            if (!basePath.endsWith(File.separator)) {
                basePath = basePath + File.separator;
            }
            if (!filePath.endsWith(File.separator)) {
                filePath = filePath + File.separator;
            }

            Global.showMessage("========\n");
            Global.showMessage(basePath + "\n");
            Global.showMessage(filePath + "\n");

            //Subo el directorio base para que sea común al del archivo
            if (filePath.contains(basePath)) {
                //Caso facil, el archivo está dentro.
                int indexOf = filePath.indexOf(basePath);
                indexOf = indexOf + basePath.length();

                relativized = filePath.substring(indexOf, filePath.length());
            } else {
                Global.showMessage(basePath + "\n");
                Global.showMessage(filePath + "\n");
                Global.showMessage("=====\n");
                Global.showMessage("\n");
                int indexCommon = 0;
                int maxIndex = Math.min(basePath.length(), filePath.length());
                //extraer la parte común
                for (int i = 0; i < maxIndex; i++) {
                    if (basePath.charAt(i) == filePath.charAt(i)) {
                        if (basePath.charAt(i) == File.separatorChar) {
                            indexCommon = i;
                        }
                    } else {
                        break;
                    }
                }

                //Al common le sumo uno porque es así y le sumo otro para quitar la barra del mas largo.
                basePath = basePath.substring(indexCommon + 1, basePath.length());
                filePath = filePath.substring(indexCommon + 1, filePath.length());

                //Una vez se ha extraido, añadir lo que quede el archivo a relativizar.
                Global.showMessage(basePath + "\n");
                Global.showMessage(filePath + "\n");

                String aux = basePath;
                relativized = "";
                while (aux != null) {
                    int index = aux.indexOf(File.separator);
                    if (index < 0) {
                        aux = null;
                    } else {
                        aux = aux.substring(index + 1);
                        relativized = relativized + ".." + File.separator;
                    }
                }
                if (filePath.endsWith(File.separator)) {
                    filePath = filePath.substring(0, filePath.length() - 1);
                }
                relativized = relativized + filePath;
            }
        } else {

            //Espero que en linux si funcione esto...
            relativized = base.toURI().relativize(file.toURI()).getPath();
        }
        return relativized;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.PATH;
    }
}
