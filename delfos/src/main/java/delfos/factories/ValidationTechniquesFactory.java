package delfos.factories;

import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Items;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Users;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.experiment.validation.validationtechnique.HoldOut_Users;
import delfos.experiment.validation.validationtechnique.NoPartitions;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.common.Global;

/**
 * Clase que implementa el patrón factoría para las técnicas de validación.
 * Permite ver las técnicas que hay implementadas, obtener una técnica concreta.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date.
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 */
public class ValidationTechniquesFactory extends Factory<ValidationTechnique> {

    private static final ValidationTechniquesFactory instance;

    public static ValidationTechniquesFactory getInstance() {
        return instance;
    }

    static {
        Global.showInfoMessage("Validation techniques loaded\n");
        instance = new ValidationTechniquesFactory();
        instance.addClass(NoPartitions.class);
        instance.addClass(HoldOut_Ratings.class);
        instance.addClass(HoldOut_Users.class);
        instance.addClass(CrossFoldValidation_Ratings.class);
        instance.addClass(CrossFoldValidation_Users.class);
        instance.addClass(CrossFoldValidation_Items.class);
    }

    private ValidationTechniquesFactory() {
    }
}
