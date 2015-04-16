package delfos.rs.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import delfos.ERROR_CODES;
import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.StringParameter;
import delfos.io.xml.recommendations.RecommendationsToXML;
import delfos.rs.recommendation.Recommendations;

/**
 * Escribe las recomendaciones a un fichero XML. El parámetro
 * {@link XMLFile#FILE_NAME} indica el principio del nombre del fichero, por lo
 * que si su valor es 'userUserRecommendation', el nombre del fichero de
 * recomendaciones para el usuario 54 es 'userUserRecommendation.user_54.xml'.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 28-oct-2013
 */
public class RecommendationsOutputFileXML extends RecommendationsOutputMethod {

    private static final long serialVersionUID = 1L;
    public static final Parameter FILE_NAME = new Parameter("FILE_NAME", new StringParameter("recommendations"));

    /**
     * Constructor por defecto, que añade los parámetros de este método de
     * salida de recomendaciones.
     */
    public RecommendationsOutputFileXML() {
        super();
        addParameter(FILE_NAME);
    }

    /**
     * Constructor que establece el nombre del archivo de salida.
     *
     * @param filePartialName
     */
    public RecommendationsOutputFileXML(String filePartialName) {
        this();
        setParameterValue(FILE_NAME, filePartialName);
    }

    /**
     * Constructor que establece el nombre del archivo de salida.
     *
     * @param filePartialName
     * @param numberOfRecommendations
     */
    public RecommendationsOutputFileXML(String filePartialName, int numberOfRecommendations) {
        this();
        setParameterValue(FILE_NAME, filePartialName);
        setParameterValue(NUMBER_OF_RECOMMENDATIONS, numberOfRecommendations);
    }

    @Override
    public void writeRecommendations(Recommendations recommendationsToUser) {

        Element recommendationsElement = RecommendationsToXML.getRecommendationsElement(recommendationsToUser);

        String idTarget = recommendationsToUser.getTargetIdentifier();

        Document doc = new Document(recommendationsElement);
        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        File file = getCompleteFile(idTarget);
        FileUtilities.createDirectoriesForFile(file);

        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RECOMMENDATIONS.exit(ex);
        }
    }

    public Recommendations readRecommendations(String idTarget) throws JDOMException {

        SAXBuilder builder = new SAXBuilder();
        Document doc = null;

        try {
            doc = builder.build(getCompleteFile(idTarget));
        } catch (IOException ex) {
            Global.showError(ex);
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
            throw new IllegalStateException(ex);
        }

        Element recommendationsElement = doc.getRootElement();
        Recommendations recommendations
                = RecommendationsToXML.getRecommendations(recommendationsElement);
        return recommendations;
    }

    /**
     * Devuelve la parte inicial del nombre del fichero en que se escriben las
     * recomendaciones.
     *
     * @return Parte inicial del nombre del fichero en que se escriben las
     * recomendaciones.
     */
    public String getFileName() {
        return (String) getParameterValue(FILE_NAME);
    }

    /**
     * Nombre completo del fichero en que se escriben las recomendaciones.
     *
     * @param idTarget para el que se almacenan sus recomendaciones.
     * @return
     */
    public File getCompleteFile(String idTarget) {
        String fileName = getFileName() + "_target_" + idTarget + ".xml";
        return new File(fileName);
    }
}
