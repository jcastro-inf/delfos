package delfos.view;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.Global;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Clase que encapsula el funcionamiento de un archivo de configuración para
 * almacenar las preferencias de la interfaz (tamaño ventanas, etc), así como
 * los directorios por defecto que se utilizan para buscar datasets
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class SwingGUIConfigurationFile {

    private static final File f = new File(Constants.CONFIGURATION_FOLDER.getPath() + File.separator + "swing-gui.config");
    private static Map<String, String> cache;

    static {
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException ex) {
                Global.showError(ex);
            }
        }
    }

    static boolean exists() {
        return f.exists();
    }

    /**
     * Este método es privado para que no pueda ser invocado en ningún sitio
     */
    private SwingGUIConfigurationFile() {
    }

    /**
     * Método para cargar el archivo de configuración de la interfaz en memoria
     */
    private static void loadFile() {
        if (cache == null) {
            cache = new TreeMap<>();
        }
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = br.readLine();
            while (line != null) {
                String variable;
                String valor;
                if (line.startsWith("@") && line.contains(":")) {
                    variable = line.substring(1, line.indexOf(":"));
                    valor = line.substring(line.indexOf(":") + 1);
                    cache.put(variable, valor);
                }
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
        }
    }

    /**
     * Devuelve el valor de la propiedad indicada.
     *
     * @param propertyName Propiedad para la que se busca el valor.
     * @return Valor de la propiedad.
     */
    public static String getPropertyValue(String propertyName) {
        if (cache == null) {
            loadFile();
        }

        return cache.get(propertyName);
    }

    /**
     * Establece el valor de la propiedad indicada.
     *
     * @param propertyName Propiedad para la que se establece el valor.
     * @param value Valor nuevo de la propiedad.
     */
    public synchronized static void setProperty(String propertyName, Object value) {
        if (cache == null) {
            loadFile();
        }

        cache.put(propertyName, value.toString());
    }

    /**
     *
     */
    public static void saveFile() {
        if (cache == null) {
            loadFile();
        }

        //el segundo parámetro es falso para que borre el archivo y lo escriba de nuevo
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(f, false));
            for (Entry<String, String> entry : cache.entrySet()) {
                bw.write("@" + entry.getKey() + ":" + entry.getValue() + "\n");
            }

            bw.flush();
            bw.close();
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_SAVE_CONFIG_FILE.exit(ex);
        }
    }
}
