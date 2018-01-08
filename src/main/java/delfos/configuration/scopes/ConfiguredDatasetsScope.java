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
package delfos.configuration.scopes;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwnerNotFoundException;
import delfos.configuration.ConfigurationManager;
import delfos.configuration.ConfigurationScope;
import delfos.configureddatasets.ConfiguredDataset;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.io.xml.dataset.DatasetLoaderXML;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ConfiguredDatasetsScope extends ConfigurationScope {

    private static final ConfiguredDatasetsScope instance;

    public static final String CONFIGURED_DATASETS_ROOT_ELEMENT_NAME = "ConfiguredDatasets";

    public static final String CONFIGURED_DATASET_ELEMENT_DESCRIPTION_ATTRIBUTE = "description";
    public static final String CONFIGURED_DATASET_ELEMENT_NAME_ATTRIBUTE = "name";
    public static final String CONFIGURED_DATASET_ELEMENT_NAME = "ConfiguredDataset";

    static {
        instance = new ConfiguredDatasetsScope();
    }

    public static ConfiguredDatasetsScope getInstance() {
        return instance;
    }

    public ConfiguredDatasetsScope() {
        super("configured-datasets");
    }

    @Override
    protected void saveConfigurationScope() {
        saveConfiguredDatasets();
    }

    @Override
    public void loadConfigurationScope() {
        Collection<ConfiguredDataset> configuredDatasets = loadConfiguredDatasets();
        ConfiguredDatasetsFactory.getInstance().setAllConfiguredDatasets(configuredDatasets);
    }

    public synchronized void saveConfiguredDatasets() {

        Document doc = new Document();
        Element root = new Element(CONFIGURED_DATASETS_ROOT_ELEMENT_NAME);

        for (ConfiguredDataset configuredDataset : ConfiguredDatasetsFactory.getInstance().getAllConfiguredDatasets()) {
            Element thisDatasetLoader = new Element(CONFIGURED_DATASET_ELEMENT_NAME);

            thisDatasetLoader.setAttribute(CONFIGURED_DATASET_ELEMENT_NAME_ATTRIBUTE,
                    configuredDataset.getName());

            thisDatasetLoader.setAttribute(CONFIGURED_DATASET_ELEMENT_DESCRIPTION_ATTRIBUTE,
                    configuredDataset.getDescription());

            Element datasetLoaderElement = DatasetLoaderXML.getElement(
                    configuredDataset.getDatasetLoader());
            thisDatasetLoader.addContent(datasetLoaderElement);
            root.addContent(thisDatasetLoader);
        }

        doc.addContent(root);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        File fileOfConfiguredDatasets = ConfigurationManager.getConfigurationFile(this);

        try (FileWriter fileWriter = new FileWriter(fileOfConfiguredDatasets)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_CONFIGURED_DATASETS_FILE.exit(ex);
        }
    }

    private synchronized Collection<ConfiguredDataset> loadConfiguredDatasets() {
        File fileOfConfiguredDatasets = ConfigurationManager.getConfigurationFile(this);

        Collection<ConfiguredDataset> configuredDatasets = new ArrayList<>();

        if (!fileOfConfiguredDatasets.exists()) {
            Global.showWarning("The configured datasets file for this machine does not exists-");
            Global.showWarning("\tFile '" + fileOfConfiguredDatasets.getAbsolutePath() + "': not found.");
            Global.showWarning("The configured datasets file for this machine does not exists.");
            saveConfigurationScope();
        } else {
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Loading configured datasets from file '" + fileOfConfiguredDatasets.getAbsolutePath() + "'\n");
            }
            try {
                SAXBuilder builder = new SAXBuilder();
                Document doc = builder.build(fileOfConfiguredDatasets);

                Element rootElement = doc.getRootElement();
                if (!rootElement.getName().equals(CONFIGURED_DATASETS_ROOT_ELEMENT_NAME)) {
                    IllegalArgumentException ex = new IllegalArgumentException("The XML does not contains the configured datasets.");
                    ERROR_CODES.CANNOT_READ_CONFIGURED_DATASETS_FILE.exit(ex);
                }

                for (Element configuredDataset : rootElement.getChildren(CONFIGURED_DATASET_ELEMENT_NAME)) {
                    String name = configuredDataset.getAttributeValue(CONFIGURED_DATASET_ELEMENT_NAME_ATTRIBUTE);
                    String description = configuredDataset.getAttributeValue(CONFIGURED_DATASET_ELEMENT_DESCRIPTION_ATTRIBUTE);
                    Element datasetLoaderElement = configuredDataset.getChild(DatasetLoaderXML.ELEMENT_NAME);

                    if (datasetLoaderElement == null) {
                        IllegalStateException ex = new IllegalStateException("Cannot retrieve configured dataset loader '" + name + "'");
                        ERROR_CODES.CANNOT_READ_CONFIGURED_DATASETS_FILE.exit(ex);
                    }

                    try {
                        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(datasetLoaderElement);

                        if (Global.isVerboseAnnoying()) {
                            Global.showInfoMessage("\tConfigured dataset '" + name + "' loaded.\n");
                        }

                        configuredDatasets.add(new ConfiguredDataset(name, description, datasetLoader));

                    }catch (ParameterOwnerNotFoundException ex){
                        Global.showWarning(ex.getMessage());
                    }
                }
                if (configuredDatasets.isEmpty()) {
                    Global.showWarning("No configured datasets found, check configuration file.");
                }
            } catch (JDOMException | IOException ex) {
                ERROR_CODES.CANNOT_READ_CONFIGURED_DATASETS_FILE.exit(ex);
            }
        }

        return configuredDatasets;
    }

}
