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
package delfos.main.managers.recommendation.nonpersonalised;

import delfos.main.managers.CaseUseModeWithSubManagers;
import delfos.main.managers.CaseUseSubManager;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @version 22-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class NonPersonalisedRecommendation extends CaseUseModeWithSubManagers {

    /**
     * Parámetro de la linea de comandos para usar el modo non-personalised.
     */
    public static final String NON_PERSONALISED_MODE = "--non-personalised";

    private static final NonPersonalisedRecommendation instance = new NonPersonalisedRecommendation();

    public static NonPersonalisedRecommendation getInstance() {
        return instance;
    }

    @Override
    public String getModeParameter() {
        return NON_PERSONALISED_MODE;
    }

    @Override
    public Collection<CaseUseSubManager> getAllCaseUseSubManagers() {
        ArrayList<CaseUseSubManager> allCaseUseModeSubManagers = new ArrayList<>();

        allCaseUseModeSubManagers.add(BuildRecommendationModel.getInstance());
        allCaseUseModeSubManagers.add(Recommend.getInstance());

        return allCaseUseModeSubManagers;
    }

}
