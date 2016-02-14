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
package delfos.configuration;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class ConfigurationScope {

    private final String scopeName;

    private Document document = null;

    public ConfigurationScope(String scopeName) {
        this.scopeName = scopeName;
    }

    public String getScopeName() {
        return scopeName;
    }

    /**
     * The scopeName property is set with the given value.
     *
     * @param propertyName Name of the property, separated with dots to indicate
     * route, e.g., swing-gui.position.x indicates that x element of
     * swing-gui/position is set to the provided value.
     * @param value value of the property.
     */
    public void setProperty(String propertyName, String value) {
        loadConfigurationScope();

        List<String> path = Arrays.asList(propertyName.split("\\."));

        Element targetElement = null;

        for (String node : path) {
            Element targetElementChild;
            if (targetElement == null) {
                targetElementChild = document.getRootElement().getChild(node);
                if (targetElementChild == null) {
                    targetElementChild = new Element(node);
                    document.getRootElement().addContent(targetElementChild);
                }
            } else {
                targetElementChild = targetElement.getChild(node);
                if (targetElementChild == null) {
                    targetElementChild = new Element(node);
                    targetElement.addContent(targetElementChild);
                }
            }
            targetElement = targetElementChild;
        }
        targetElement.setAttribute("value", value);

        saveConfigurationScope();
    }

    public String getProperty(String propertyName) {
        loadConfigurationScope();

        List<String> path = new ArrayList<>(Arrays.asList(propertyName.split("\\.")));

        Element root = document.getRootElement();
        if (root == null) {
            throw new IllegalStateException("Root element cannot be null");
        }
        Element targetElement = root.getChild(path.remove(0));

        for (String node : path) {
            if (targetElement == null) {
                break;
            }
            targetElement = targetElement.getChild(node);
        }
        if (targetElement == null) {
            return null;
        } else {
            return targetElement.getAttributeValue("value");
        }
    }

    protected Document getXMLDocument() {
        return document;
    }

    protected void saveConfigurationScope() {

        File configurationFile = ConfigurationManager.getConfigurationFile(this);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        FileUtilities.createDirectoriesForFile(configurationFile);
        try (FileWriter fileWriter = new FileWriter(configurationFile)) {
            outputter.output(document, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }

    }

    protected void loadConfigurationScope() {
        if (document == null) {
            File configurationFile = ConfigurationManager.getConfigurationFile(this);
            SAXBuilder builder = new SAXBuilder();
            try {
                document = builder.build(configurationFile);
            } catch (JDOMException | IOException ex) {
                Global.showWarning("Cannot load xml from " + configurationFile.getAbsolutePath());

                if (configurationFile.exists()) {
                    File erroneousFile = new File(configurationFile.getParentFile().getAbsolutePath() + "cannot-parse.xml");
                    configurationFile.renameTo(erroneousFile);
                }

                document = new Document(new Element(scopeName));
            }
        }
    }

    public File getConfigurationFile() {
        return ConfigurationManager.getConfigurationFile(this);
    }
}
