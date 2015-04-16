package delfos.factories;

import delfos.experiment.validation.predictionprotocol.AllButOne;
import delfos.experiment.validation.predictionprotocol.GivenN;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.predictionprotocol.PredictN;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;

/**
 * Clase que implementa el patrón factoría para las validaciones de predicciones
 * para algoritmos colaborativos . Permite ver las técnicas que hay
 * implementadas, obtener una técnica concreta.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 Unknow date
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class PredictionProtocolFactory extends Factory<PredictionProtocol> {

    private static final PredictionProtocolFactory instance;

    public static PredictionProtocolFactory getInstance() {
        return instance;
    }

    static {
        instance = new PredictionProtocolFactory();
        instance.addClass(NoPredictionProtocol.class);
        instance.addClass(GivenN.class);
        instance.addClass(PredictN.class);
        instance.addClass(AllButOne.class);
    }

    private PredictionProtocolFactory() {
    }
}
