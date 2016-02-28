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
package delfos.group.grs.preferenceaggregation.order;

import delfos.group.grs.preferenceaggregation.CommonSequences;
import delfos.group.grs.preferenceaggregation.PreferenceOrder;


/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupTotalOrder implements Preff {

    private PreferenceOrder<Integer>[] orderMembers;

    public GroupTotalOrder(PreferenceOrder<Integer>[] orderMembers) {
        this.orderMembers = orderMembers;
    }

    @Override
    public double preff(Object e1, Object e2) {

        if (e1 == e2) {
            //Global.showWarning("Es un warning?");
            return 0;
        }

        //Secuencia que compruebo en cada uno de los miembros del grupo
        PreferenceOrder alpha = new PreferenceOrder(e1, e2);
        int gAB = 0;
        for (PreferenceOrder x : orderMembers) {

            //Esta sentencia suma el valor de ncm(alpha,x), es decir, el número de vecinos en común
            gAB += CommonSequences.getCommonSequencesEfficient(alpha, x).size();
        }


        int gB = 0;
        alpha = new PreferenceOrder(e2);
        for (PreferenceOrder x : orderMembers) {

            //Esta sentencia suma el valor de ncm(alpha,x), es decir, el número de vecinos en común
            gB += CommonSequences.getCommonSequencesEfficient(alpha, x).size();
        }

        double ret = ((double) gAB) / gB;

        if (ret <= 1 && ret >= 0) {
            throw new IllegalArgumentException("The value is not between 0 and 1: " + ret);
        }
        if (ret < 0) {
            throw new IllegalArgumentException("The value is negative: " + ret);
        }

        return ret;
    }
}
