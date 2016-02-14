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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
