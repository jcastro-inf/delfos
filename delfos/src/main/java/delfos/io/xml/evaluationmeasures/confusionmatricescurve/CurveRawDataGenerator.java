package delfos.io.xml.evaluationmeasures.confusionmatricescurve;

import org.jdom2.Element;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;

/**
 *
 * Interfaz que define los métodos que una clase para integrar información en
 * bruto que represente los puntos de un espacio generado a partir de las
 * matrices de confusión.
 *
 * <p>El elemento a devolver tendrá la siguiente estructura:
 *
 * {@literal <RawData type="tipoDeLaCurva">\nCoordenada1\tCoordenada2\n
 * Coordenada1\tCoordenada2\n</RawData> }
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 (11-01-2013)
 *
 * @see PRCurve
 * @see AreaUnderROC
 */
public interface CurveRawDataGenerator {

    /**
     * Nombre del elemento que almacena la información en bruto.
     */
    public final static String RAW_DATA_ELEMENT = "RawData";
    /**
     * Nombre de la característica donde se almacena el tipo de información que se
     * representa en su contenido.
     */
    public final static String RAW_DATA_TYPE_ATTRIBUTE = "type";

    /**
     * Genera la información en bruto (separada por tabuladores) para
     * representar una curva generada a partir de las matrices de confusión.
     *
     * <p>La información en bruto se debe devolver usando una linea para cada
     * punto, y cada coordenada de los puntos separada por un tabulador.
     *
     * @param curve Curva que genera el espacio.
     * @return Elemento con la información en bruto.
     */
    public Element getRawDataElement(ConfusionMatricesCurve curve);
}
