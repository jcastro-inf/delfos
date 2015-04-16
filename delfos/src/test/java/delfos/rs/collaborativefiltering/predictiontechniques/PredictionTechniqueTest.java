package delfos.rs.collaborativefiltering.predictiontechniques;

import org.junit.Test;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.constants.DelfosTest;

/**
 * Clase abstracta para probar técnicas de predicción de sistemas de
 * recomendación colaborativos.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 21-Jan-2013
 */
public class PredictionTechniqueTest extends DelfosTest {

    /**
     * Evalúa que la técnica predice correctamente.
     */
    @Test
    public void testPrediction() {
        checkTestOverride();
    }

    /**
     * Evalúa que la técnica lanza una excepción cuando no hay valores
     * disponibles para predecir.
     *
     * @throws delfos.common.exceptions.CouldNotPredictRating
     */
    @Test(expected = CouldNotPredictRating.class)
    public void testNotEnoughValues() throws CouldNotPredictRating {
        checkTestOverride();
        throw new CouldNotPredictRating("");
    }

    @Test(expected = CouldNotPredictRating.class)
    public void testNaNRating() throws CouldNotPredictRating {
        checkTestOverride();
        throw new CouldNotPredictRating("");
    }

    @Test(expected = CouldNotPredictRating.class)
    public void testInfinityRating() throws CouldNotPredictRating {
        checkTestOverride();
        throw new CouldNotPredictRating("");
    }

    private void checkTestOverride() {
        if (!this.getClass().equals(PredictionTechniqueTest.class)) {
            throw new UnsupportedOperationException(this.getClass() + ": Child classes must override this method.");
        }
    }
}
