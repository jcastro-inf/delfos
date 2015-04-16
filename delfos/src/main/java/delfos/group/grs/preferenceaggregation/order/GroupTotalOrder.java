package delfos.group.grs.preferenceaggregation.order;

import delfos.group.grs.preferenceaggregation.CommonSequences;
import delfos.group.grs.preferenceaggregation.PreferenceOrder;


/**
 *
* @author Jorge Castro Gallardo
 */
public class GroupTotalOrder implements Preff {

    private PreferenceOrder<Integer>[] orderMembers;

    public GroupTotalOrder(PreferenceOrder<Integer>[] orderMembers) {
        this.orderMembers = orderMembers;
    }

    @Override
    public float preff(Object e1, Object e2) {

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

        float ret = ((float) gAB) / gB;

        if (ret <= 1 && ret >= 0) {
            throw new IllegalArgumentException("The value is not between 0 and 1: " + ret);
        }
        if (ret < 0) {
            throw new IllegalArgumentException("The value is negative: " + ret);
        }

        return ret;
    }
}
