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
package delfos.group.grs.filtered.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.util.DatasetPrinterDeprecated;

/**
 * Implementa un filtro de ratings que elimina los ratings que son demasiado
 * distintos al resto de ratings del grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 03-May-2013
 */
public class OutliersRatingsFilter extends GroupRatingsFilter {

    private static final long serialVersionUID = 1L;
    /**
     * Umbral para eliminar ratings. Se elimina el rating si la diferencia es
     * mayor que este valor.
     */
    public static final Parameter DIFFERENCE_THRESHOLD
            = new Parameter("DIFFERENCE_THRESHOLD", new FloatParameter(0, Float.MAX_VALUE, 0.5f));
    /**
     * Se eliminan como máximo esta proporción de ratings.
     */
    public static final Parameter PERCENTAGE_MAX_FILTERED_OUT
            = new Parameter("PERCENTAGE_MAX_FILTERED_THRESHOLD", new FloatParameter(0, 1f, 0.2f));
    /**
     * Protege las valoraciones del grupo para que al menos exista una
     * valoración de cada producto. Por defecto vale false.
     */
    public static final Parameter KEEP_AT_LEAST_ONE_RATING = new Parameter(
            "KEEP_AT_LEAST_ONE_RATING", new BooleanParameter(false),
            "Protege las valoraciones del grupo para que al menos exista una \n"
            + "valoración de cada producto. Por defecto vale false.");

    private double oldPERCENTAGE_MAX_FILTERED_OUT = 0.2;
    private double oldDIFFERENCE_THRESHOLD = 0.5;
    private boolean oldKEEP_AT_LEAST_ONE_RATING = false;
    private String oldAlias = "";

    public OutliersRatingsFilter() {
        super();
        addParameter(DIFFERENCE_THRESHOLD);
        addParameter(PERCENTAGE_MAX_FILTERED_OUT);
        addParameter(KEEP_AT_LEAST_ONE_RATING);

        addParammeterListener(() -> {
            double newDIFFERENCE_THRESHOLD = ((Number) getParameterValue(DIFFERENCE_THRESHOLD)).doubleValue();
            double newPERCENTAGE_MAX_FILTERED_OUT = ((Number) getParameterValue(PERCENTAGE_MAX_FILTERED_OUT)).doubleValue();
            boolean newKEEP_AT_LEAST_ONE_RATING = (Boolean) getParameterValue(KEEP_AT_LEAST_ONE_RATING);
            String newAlias = getAlias();

            newDIFFERENCE_THRESHOLD = NumberRounder.round(newDIFFERENCE_THRESHOLD, 2);
            newPERCENTAGE_MAX_FILTERED_OUT = NumberRounder.round(newPERCENTAGE_MAX_FILTERED_OUT, 2);

            String oldAliasOldParameters
                    = OutliersRatingsFilter.class.getSimpleName()
                    + "(u=" + oldDIFFERENCE_THRESHOLD
                    + "_KR=" + oldKEEP_AT_LEAST_ONE_RATING
                    + "_MPD=" + oldPERCENTAGE_MAX_FILTERED_OUT + ")";

            String newAliasNewParameters
                    = OutliersRatingsFilter.class.getSimpleName()
                    + "(u=" + newDIFFERENCE_THRESHOLD
                    + "_KR=" + newKEEP_AT_LEAST_ONE_RATING
                    + "_MPD=" + newPERCENTAGE_MAX_FILTERED_OUT + ")";

            if (!oldAliasOldParameters.equals(newAliasNewParameters)) {
                oldDIFFERENCE_THRESHOLD = newDIFFERENCE_THRESHOLD;
                oldKEEP_AT_LEAST_ONE_RATING = newKEEP_AT_LEAST_ONE_RATING;
                oldPERCENTAGE_MAX_FILTERED_OUT = newPERCENTAGE_MAX_FILTERED_OUT;
                oldAlias = newAliasNewParameters;
                setAlias(newAliasNewParameters);
            }
        });
    }

    public OutliersRatingsFilter(double differenceThreshold, double percentageMaxFilteredOut) {
        this();
        setDiffThreshold(differenceThreshold);
        setPercentageMaxFilteredOut(percentageMaxFilteredOut);
    }

    public OutliersRatingsFilter(double differenceThreshold, double percentageMaxFilteredOut, boolean keepAtLeastOneRating) {
        this(differenceThreshold, percentageMaxFilteredOut);
        setKeepAtLeastOneRating(keepAtLeastOneRating);
    }

    /**
     * @return the diffThreshold
     */
    public double getDiffThreshold() {
        return ((Number) getParameterValue(DIFFERENCE_THRESHOLD)).doubleValue();
    }

    /**
     * @param diffThreshold the diffThreshold to set
     */
    public final void setDiffThreshold(double diffThreshold) {
        setParameterValue(DIFFERENCE_THRESHOLD, diffThreshold);
    }

    /**
     * @return the percentageThreshold
     */
    public double getPercentageMaxFilteredOut() {
        return ((Number) getParameterValue(PERCENTAGE_MAX_FILTERED_OUT)).doubleValue();
    }

    /**
     * @param percentage the percentageThreshold to set
     */
    public final void setPercentageMaxFilteredOut(double percentage) {
        setParameterValue(PERCENTAGE_MAX_FILTERED_OUT, percentage);
    }

    private void setKeepAtLeastOneRating(boolean keepAtLeastOneRating) {
        setParameterValue(KEEP_AT_LEAST_ONE_RATING, keepAtLeastOneRating);
    }

    private boolean getKeepAtLeastOneRating() {
        return (Boolean) getParameterValue(KEEP_AT_LEAST_ONE_RATING);
    }

    @Override
    public Map<Integer, Map<Integer, Number>> getFilteredRatings(Map<Integer, Map<Integer, Number>> originalSet) {
        int totalRatingsGrupo = 0;

        ArrayList<DifferenceRatings> ratingsToErase = new ArrayList<>();

        for (int idUser : originalSet.keySet()) {
            Collection<Integer> otherUsers = new TreeSet<>(originalSet.keySet());
            otherUsers.remove(idUser);

            for (int idItem : originalSet.get(idUser).keySet()) {

                Number userRating = originalSet.get(idUser).get(idItem);
                if (userRating == null) {
                    throw new IllegalStateException("The user has null as a rating for item " + idItem + ".");
                }

                MeanIterative mean = new MeanIterative();
                for (int idMember : otherUsers) {
                    if (originalSet.get(idMember).containsKey(idItem)) {
                        Number rating = originalSet.get(idMember).get(idItem);
                        if (rating == null) {
                            throw new IllegalStateException("The user has null as a rating for item " + idItem + ".");
                        }

                        mean.addValue(rating.doubleValue());
                    }
                }
                ratingsToErase.add(new DifferenceRatings(idUser, idItem, Math.abs(mean.getMean() - userRating.doubleValue()), userRating));
            }
        }
        Collections.sort(ratingsToErase);

        {
            if (isKeepingAtLeastOneRating()) {
                Set<Integer> itemAppeared = new TreeSet<>();
                for (ListIterator<DifferenceRatings> it = ratingsToErase.listIterator(ratingsToErase.size()); it.hasPrevious();) {
                    DifferenceRatings differenceRatings = it.previous();

                    if (itemAppeared.contains(differenceRatings.idItem)) {
                        differenceRatings.deletable = true;
                    } else {
                        differenceRatings.deletable = false;
                        itemAppeared.add(differenceRatings.idItem);
                    }
                }
            } else {
                ratingsToErase.stream().forEach((dif) -> {
                    dif.deletable = true;
                });
            }
        }

        int borrarMax = (int) (ratingsToErase.size() * getPercentageMaxFilteredOut());
        int eliminados = 0;

        if (Global.isVerboseAnnoying()) {
            //Calculo la matriz de prioridad en la eliminación de ratings.

            Map<Integer, Map<Integer, Number>> deletePriority = new TreeMap<>();
            originalSet.keySet().stream().forEach((idUser) -> {
                deletePriority.put(idUser, new TreeMap<>());
            });
            for (DifferenceRatings difference : ratingsToErase) {

                int idUser = difference.idUser;
                int idItem = difference.idItem;
                deletePriority.get(idUser).put(idItem, difference.diff);
            }

            Global.showInfoMessage("Rating elimination priority table.\n");
            DatasetPrinterDeprecated.printCompactRatingTable(deletePriority);
        }

        Map<Integer, Map<Integer, Number>> ratingsToReturn = new TreeMap<>();
        originalSet.keySet().stream().forEach((idUser) -> {
            ratingsToReturn.put(idUser, new TreeMap<>());
        });

        while (!ratingsToErase.isEmpty()) {
            DifferenceRatings difference = ratingsToErase.remove(ratingsToErase.size() - 1);

            int idUser = difference.idUser;
            int idItem = difference.idItem;

            if (difference.deletable && !Double.isNaN(difference.diff)) {
                //Es posible borrarlo, ver el resto de condiciones.
                if (difference.diff > getDiffThreshold() && eliminados < borrarMax) {
                    //Este rating no se devuelve, porque es muy distinto de la media.
                    if (Global.isVerboseAnnoying()) {
                        Global.showInfoMessage("Eliminado ==> " + difference + "\n");
                    }
                    eliminados++;
                } else {
                    //Este rating se devuelve.
                    if (Global.isVerboseAnnoying()) {
                        Global.showInfoMessage("Devuelto ---> " + difference + "\n");
                    }
                    ratingsToReturn.get(idUser).put(idItem, difference.originalRating);
                }
            } else {
                //No es posible borrarlo, devolverlo.
                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage("Devuelto ---> " + difference + "\n");
                }
                ratingsToReturn.get(idUser).put(idItem, difference.originalRating);
            }
        }

        float partialPercent = (eliminados * 100.0f) / borrarMax;
        float totalPercent = (eliminados * 100.0f) / totalRatingsGrupo;

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage(
                    "Ratings del grupo " + originalSet.keySet() + " eliminados: "
                    + eliminados + " de " + totalRatingsGrupo
                    + " (P: " + NumberRounder.round(partialPercent, 2)
                    + "% T: " + NumberRounder.round(totalPercent, 2) + "%)\n");
        }
        return ratingsToReturn;
    }

    private class DifferenceRatings implements Comparable<DifferenceRatings> {

        public final int idUser;
        public final int idItem;
        public final double diff;
        public final Number originalRating;
        /**
         * Dice si esta valoración se puede eliminar. Implementarlo. Hay que
         * hacer que este atributo se calcule despues de tener la lista
         * completa.
         */
        public Boolean deletable = null;

        public DifferenceRatings(int idUser, int idItem, double diff, Number originalRating) {
            this.idUser = idUser;
            this.idItem = idItem;
            this.diff = diff;
            this.originalRating = originalRating;
        }

        @Override
        public int compareTo(DifferenceRatings o) {
            return Double.compare(this.diff, o.diff);
        }

        @Override
        public String toString() {
            return Double.toString(diff);
        }
    }

    private boolean isKeepingAtLeastOneRating() {
        return (Boolean) getParameterValue(KEEP_AT_LEAST_ONE_RATING);
    }
}
