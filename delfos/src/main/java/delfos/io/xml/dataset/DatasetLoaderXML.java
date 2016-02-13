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
package delfos.io.xml.dataset;

import org.jdom2.Element;
import delfos.ERROR_CODES;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.ParameterOwnerType;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.factories.DatasetLoadersFactory;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.io.xml.parameterowner.parameter.ParameterXML;

/**
 * Clase para efectuar la entrada/salida en XML de los dataset loader.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (06/12/2012)
 * @version 2.0 15-Noviembre-2013 Ya no están obsoletos los métodos, se han
 *
 * reimplementado a partir de {@link ParameterOwnerXML}.
 */
public class DatasetLoaderXML {

    public static final String ELEMENT_NAME = "DatasetLoader";

    public static DatasetLoader<? extends Rating> getDatasetLoader(Element datasetLoaderElement) {
        if (!datasetLoaderElement.getName().equals(ELEMENT_NAME)) {
            throw new IllegalArgumentException("The element does not contains a dataset loader. [Element name '" + datasetLoaderElement.getName() + "']");
        }

        String name = datasetLoaderElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_NAME);
        String parameterOwnerType = datasetLoaderElement.getAttributeValue(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE);
        if (parameterOwnerType == null) {
            datasetLoaderElement.setAttribute(ParameterOwnerXML.PARAMETER_OWNER_ATTRIBUTE_TYPE, ParameterOwnerType.DATASET_LOADER.name());
        }

        if (datasetLoaderElement.getAttribute(CONFIGURED_DATASET_ATTRIBUTE) != null && "true".equals(datasetLoaderElement.getAttributeValue(CONFIGURED_DATASET_ATTRIBUTE))) {
            //Es un dataset configurado, hay que recuperarlo de la factoría de dataset configurados.
            DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader(name);
            return datasetLoader;
        } else {
            //Un dataset de toda la vida.
            ParameterOwner parameterOwner = ParameterOwnerXML.getParameterOwner(datasetLoaderElement);
            if (parameterOwner instanceof DatasetLoader) {
                DatasetLoader<? extends Rating> datasetLoader = (DatasetLoader) parameterOwner;
                return datasetLoader;
            } else {
                IllegalStateException ex = new IllegalStateException("The XML does not have the expected structure: The loaded parameter owner is not a dataset loader [" + parameterOwner + "]");
                ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
                throw ex;
            }
        }
    }
    public static final String CONFIGURED_DATASET_ATTRIBUTE = "configuredDataset";

    public static Element getElement(DatasetLoader<? extends Rating> datasetLoader) {
        Element element = ParameterOwnerXML.getElement(datasetLoader);
        element.setName(ELEMENT_NAME);
        return element;
    }

    public static Object getDatasetLoader(ParameterOwner parameterOwner, Element element) {
        String datasetLoaderName = element.getAttributeValue("name");

        DatasetLoader<? extends Rating> datasetLoader = DatasetLoadersFactory.getInstance().getClassByName(datasetLoaderName);

        for (Object o : element.getChildren()) {
            Element parameterElement = (Element) o;
            ParameterXML.getParameterValue(datasetLoader, parameterElement);
        }
        return datasetLoader;
    }

    public static Element getElement(ParameterOwner parameterOwner, Parameter parameter) {
        DatasetLoader<? extends Rating> dl = (DatasetLoader) parameterOwner.getParameterValue(parameter);
        Element datasetLoaderElement = new Element(ELEMENT_NAME);
        datasetLoaderElement.setAttribute("name", dl.getName());

        for (Parameter p : dl.getParameters()) {
            Element elementParameter = ParameterXML.getElement(dl, p);
            datasetLoaderElement.addContent(elementParameter);
        }
        return datasetLoaderElement;
    }
}
