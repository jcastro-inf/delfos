package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.experiment.validation.predictionvalidation.AllButOne;
import delfos.group.experiment.validation.predictionvalidation.CrossFoldPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.HoldOutPrediction;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;

/**
 * Conoce las técnicas de generación de grupos que se utilizarán en los casos de
 * estudio para evaluar sistemas de recomendación a grupos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 16-Jan-2013
 * @version 9-Enero-2013 Adaptado a la nueva declaración de las factorías.
 */
public class GroupPredictionProtocolsFactory extends Factory<GroupPredictionProtocol> {

    protected static final GroupPredictionProtocolsFactory instance;

    public static GroupPredictionProtocolsFactory getInstance() {
        return instance;
    }

    static {
        instance = new GroupPredictionProtocolsFactory();

        instance.addClass(AllButOne.class);
        instance.addClass(CrossFoldPredictionProtocol.class);
        instance.addClass(HoldOutPrediction.class);
        instance.addClass(NoPredictionProtocol.class);
    }

    protected GroupPredictionProtocolsFactory() {
    }
}
