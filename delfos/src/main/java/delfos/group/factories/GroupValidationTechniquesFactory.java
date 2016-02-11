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
package delfos.group.factories;

import delfos.factories.Factory;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_Items;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_Ratings;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_groupRatedItems;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupMemberRatings;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.experiment.validation.validationtechniques.NoValidation;

/**
 * Conoce las técnicas de generación de grupos que se utilizarán en los casos de
 * estudio para evaluar sistemas de recomendación a grupos.
 *
 * @author Jorge Castro Gallardo
 */
public class GroupValidationTechniquesFactory extends Factory<GroupValidationTechnique> {

    protected static final GroupValidationTechniquesFactory instance;

    public static GroupValidationTechniquesFactory getInstance() {
        return instance;
    }

    static {
        instance = new GroupValidationTechniquesFactory();

        instance.addClass(CrossFoldValidation_Items.class);
        instance.addClass(CrossFoldValidation_Ratings.class);
        instance.addClass(HoldOutGroupRatedItems.class);
        instance.addClass(HoldOutGroupMemberRatings.class);
        instance.addClass(NoValidation.class);

        instance.addClass(CrossFoldValidation_groupRatedItems.class);
    }

    protected GroupValidationTechniquesFactory() {
    }
}
