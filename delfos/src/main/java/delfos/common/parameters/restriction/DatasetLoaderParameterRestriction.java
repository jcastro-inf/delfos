package delfos.common.parameters.restriction;

import org.jdom2.Element;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.factories.DatasetLoadersFactory;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Restricción que permite valores de tipo {@link DatasetLoader}.
 *
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 (Unknow date)
 * @version 1.1 (18-Jan-2013)
 * @version 2.0 (04 de Marzo de 2013) Transformación del parámetro, acepta
 cualquier valor que sea un {@link DatasetLoader}.
 */
public class DatasetLoaderParameterRestriction extends ParameterRestriction {

    private final static long serialVersionUID = 1L;

    /**
     * Restricción por defecto que establece el dataset loader a un dataset
     * procedente de archivos CSV.
     */
    public DatasetLoaderParameterRestriction() {
        this(new CSVfileDatasetLoader());
    }

    /**
     * Restricción que establece el valor por defecto al valor indicado.
     *
     * @param defaultValue Valor por defecto.
     */
    public DatasetLoaderParameterRestriction(DatasetLoader<? extends Rating> defaultValue) {
        super(defaultValue);

        if (!isCorrect(defaultValue)) {
            throw new IllegalArgumentException("The default value isn't correct");
        }
    }

    /**
     * Restricción que establece el valor por defecto al valor indicado.
     *
     * @param defaultValue Valor por defecto.
     *
     * @throws IllegalArgumentException Si el valor por defecto no es una
     * instancia de {@link DatasetLoader}
     */
    public DatasetLoaderParameterRestriction(Object defaultValue) {
        super(defaultValue);

        if (!isCorrect(defaultValue)) {
            throw new IllegalArgumentException("The default value isn't correct");
        }
    }

    @Override
    public final boolean isCorrect(Object newValue) {
        return (newValue instanceof DatasetLoader);
    }

    /**
     *
     * @return
     *
     * @deprecated No se debe usar este método. Usar en su lugar {@link DatasetLoaderFactory#getAllDatasetLoader()
     * }.
     */
    public Object[] getAllowed() {
        return DatasetLoadersFactory.getInstance().getAllClasses().toArray();
    }

    @Override
    public Object parseString(String parameterValue) {
        return DatasetLoadersFactory.getInstance().getClassByName(parameterValue);
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return DatasetLoaderXML.getDatasetLoader(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return DatasetLoaderXML.getElement(parameterOwner, parameter);
    }
}
