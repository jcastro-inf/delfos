package delfos.similaritymeasures;

import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;

/**
 * Implementación por defecto de una medida de similitud.
 *
 * <p>
 * <p>
 * La similitud es un valor entre 0 y 1, 0 cuando los vectores son completamente
 * distintos y 1 cuando son completamente iguales.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 2.0 (Unknow date) Se cambia el funcionamiento de las medidas de
 * similitud, ahora se hace mediante interfaces y adaptadores.
 * @version 1.0 (Unknow date)
 */
public abstract class SimilarityMeasureAdapter extends ParameterOwnerAdapter implements SimilarityMeasure {

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SimilarityMeasure) {
            SimilarityMeasure similarityMeasure = (SimilarityMeasure) obj;
            return getName().equals(similarityMeasure.getName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = getName().hashCode();
        return hash;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.SIMILARITY_MEASURE;
    }

}
