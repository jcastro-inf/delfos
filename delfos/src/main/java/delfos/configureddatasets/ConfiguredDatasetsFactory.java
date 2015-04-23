package delfos.configureddatasets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.JFrame;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import delfos.ERROR_CODES;
import delfos.Constants;
import delfos.common.Global;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.TrustDatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.view.configureddatasets.NewConfiguredDatasetDialog;

/**
 * Factoría que devuelve datasets ya creados, es decir, datasets que funcionan
 * correctamente en la máquina local.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 13-mar-2014
 */
public final class ConfiguredDatasetsFactory {

    static {
        String runtimeMXBean_name = ManagementFactory.getRuntimeMXBean().getName();

        machineName = runtimeMXBean_name.substring(runtimeMXBean_name.indexOf("@") + 1);

        System.out.println("runtimeMXBean_name: " + runtimeMXBean_name);
    }
    private static ConfiguredDatasetsFactory instance;
    private static final String machineName;

    private static final File fileOfConfiguredDatasets
            = new File(Constants.CONFIGURATION_DIRECTORY.getPath() + File.separator + "configuredDatasets@" + machineName + ".xml");

    public static final String CONFIGURED_DATASETS_ROOT_ELEMENT_NAME = "ConfiguredDatasets";

    public static final String CONFIGURED_DATASET_ELEMENT_DESCRIPTION_ATTRIBUTE = "description";
    public static final String CONFIGURED_DATASET_ELEMENT_NAME_ATTRIBUTE = "name";
    public static final String CONFIGURED_DATASET_ELEMENT_NAME = "ConfiguredDataset";

    private final Map<String, ConfiguredDataset> datasetLoaders;

    private final List<ConfiguredDatasetsListener> listeners = new ArrayList<>();

    public void addConfiguredDatasetsListener(ConfiguredDatasetsListener listener) {
        listeners.add(listener);
        listener.configuredDatasetsChanged();
    }

    public void removeConfiguredDatasetsListener(ConfiguredDatasetsListener listener) {
        listeners.remove(listener);
    }

    private void notifyChange() {
        listeners.stream().forEach((listener) -> {
            listener.configuredDatasetsChanged();
        });
    }

    private ConfiguredDatasetsFactory() {
        datasetLoaders = new TreeMap<>();
        loadDatasetLoaders();
    }

    public static ConfiguredDatasetsFactory getInstance() {
        if (instance == null) {
            instance = new ConfiguredDatasetsFactory();
        }
        return instance;
    }

    public void showCreateConfiguredDatasetDialog(JFrame frame) {
        NewConfiguredDatasetDialog configuredDatasetDialog = new NewConfiguredDatasetDialog(frame, true);
        configuredDatasetDialog.setVisible(true);
    }

    public synchronized void saveDatasetLoaders() {

        Document doc = new Document();
        Element root = new Element(CONFIGURED_DATASETS_ROOT_ELEMENT_NAME);

        for (String idDatasetLoader : datasetLoaders.keySet()) {
            Element thisDatasetLoader = new Element(CONFIGURED_DATASET_ELEMENT_NAME);
            thisDatasetLoader.setAttribute(CONFIGURED_DATASET_ELEMENT_NAME_ATTRIBUTE, datasetLoaders.get(idDatasetLoader).getName());
            thisDatasetLoader.setAttribute(CONFIGURED_DATASET_ELEMENT_DESCRIPTION_ATTRIBUTE, datasetLoaders.get(idDatasetLoader).getDescription());
            Element datasetLoaderElement = DatasetLoaderXML.getElement(datasetLoaders.get(idDatasetLoader).getDatasetLoader());
            thisDatasetLoader.addContent(datasetLoaderElement);
            root.addContent(thisDatasetLoader);
        }

        doc.addContent(root);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        try (FileWriter fileWriter = new FileWriter(fileOfConfiguredDatasets)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_CONFIGURED_DATASETS_FILE.exit(ex);
        }
    }

    public synchronized void loadDatasetLoaders() {
        if (!fileOfConfiguredDatasets.exists()) {
            Global.showWarning("The configured datasets file for this machine does not exists-");
            Global.showWarning("\tFile '" + fileOfConfiguredDatasets.getAbsolutePath() + "': not found.");
            Global.showWarning("The configured datasets file for this machine does not exists.");
            saveDatasetLoaders();
        } else {
            if (Global.isVerboseAnnoying()) {
                Global.showMessage("Loading configured datasets from file '" + fileOfConfiguredDatasets.getAbsolutePath() + "'\n");
            }
            try {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(fileOfConfiguredDatasets);

                Element caseStudy = doc.getRootElement();
                if (!caseStudy.getName().equals(CONFIGURED_DATASETS_ROOT_ELEMENT_NAME)) {
                    IllegalArgumentException ex = new IllegalArgumentException("The XML does not contains the configured datasets.");
                    ERROR_CODES.CANNOT_READ_CONFIGURED_DATASETS_FILE.exit(ex);
                }

                for (Element configuredDataset : caseStudy.getChildren(CONFIGURED_DATASET_ELEMENT_NAME)) {
                    String name = configuredDataset.getAttributeValue(CONFIGURED_DATASET_ELEMENT_NAME_ATTRIBUTE);
                    String description = configuredDataset.getAttributeValue(CONFIGURED_DATASET_ELEMENT_DESCRIPTION_ATTRIBUTE);
                    Element datasetLoaderElement = configuredDataset.getChild(DatasetLoaderXML.ELEMENT_NAME);

                    if (datasetLoaderElement == null) {
                        IllegalStateException ex = new IllegalStateException("Cannot retrieve configured dataset loader '" + name + "'");
                        ERROR_CODES.CANNOT_READ_CONFIGURED_DATASETS_FILE.exit(ex);
                    }

                    DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(datasetLoaderElement);

                    if (Global.isVerboseAnnoying()) {
                        Global.showMessage("\tConfigured dataset '" + name + "' loaded.\n");
                    }

                    datasetLoaders.put(name, new ConfiguredDataset(name, description, datasetLoader));
                }
                if (datasetLoaders.isEmpty()) {
                    Global.showWarning("No configured datasets found, check configuration file.");
                }
                notifyChange();
            } catch (JDOMException | IOException ex) {
                ERROR_CODES.CANNOT_READ_CONFIGURED_DATASETS_FILE.exit(ex);
            }
        }
    }

    public void addDatasetLoader(String name, String description, DatasetLoader<? extends Rating> datasetLoader) {

        //Compruebo que el dataset funciona.
        datasetLoader.getRatingsDataset();
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDatasetLoader.getContentDataset();
        }

        if (datasetLoader instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) datasetLoader;
            usersDatasetLoader.getUsersDataset();
        }

        if (datasetLoader instanceof TrustDatasetLoader) {
            TrustDatasetLoader trustDatasetLoader = (TrustDatasetLoader) datasetLoader;
            trustDatasetLoader.getTrustDataset();
        }
        if (datasetLoaders.containsKey(name)) {
            throw new IllegalArgumentException("The identifier '" + name + "' for datasets is in use.");
        } else {
            datasetLoaders.put(name, new ConfiguredDataset(name, description, datasetLoader));
            saveDatasetLoaders();
            notifyChange();
        }
    }

    public DatasetLoader<? extends Rating> getDatasetLoader(String identifier) {
        return getDatasetLoader(identifier, DatasetLoader.class);
    }

    public <DatasetLoaderType> DatasetLoaderType getDatasetLoader(String identifier, Class<DatasetLoaderType> clase) {
        if (datasetLoaders.isEmpty()) {
            Global.showWarning("No configured datasets found, check configuration file.");
        }

        if (datasetLoaders.containsKey(identifier)) {
            ConfiguredDataset configuredDataset = datasetLoaders.get(identifier);

            DatasetLoader datasetLoader = configuredDataset.getDatasetLoader();
            if (clase.isInstance(datasetLoader)) {
                return (DatasetLoaderType) datasetLoader;
            } else {
                throw new IllegalArgumentException("The dataset loader does not matches type '" + clase + "'.");
            }
        } else {
            throw new IllegalArgumentException("Configured dataset with identifier '" + identifier + "' not defined.");
        }
    }

    /**
     * Devuelve los identificadores de los dataset configurados definidos.
     *
     * @return
     */
    public Set<String> keySet() {
        return datasetLoaders.keySet();
    }

    public Collection<DatasetLoader> getAllConfiguredDatasetLoaders() {
        Collection<DatasetLoader> ret = new ArrayList<>();
        for (ConfiguredDataset datasetLoader : datasetLoaders.values()) {
            ret.add(datasetLoader.getDatasetLoader());
        }
        return ret;
    }
}
