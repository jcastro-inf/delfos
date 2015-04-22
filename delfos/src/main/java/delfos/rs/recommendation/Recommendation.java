package delfos.rs.recommendation;

import delfos.common.decimalnumbers.NumberCompare;
import delfos.common.decimalnumbers.NumberRounder;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Almacena una recomendación devuelta por un sistema de recomendación.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Recommendation implements Comparable<Recommendation>, Serializable {

    private static final long serialVersionUID = 5468;

    /**
     * Si un item esta dos veces, lo devuelve una sola (devuelve un conjunto, no
     * una colección).
     *
     * @param recommendations
     * @return
     */
    public static Set<Integer> getSetOfItems(Collection<Recommendation> recommendations) {
        Set<Integer> ret = new TreeSet<>();
        for (Recommendation r : recommendations) {
            ret.add(r.getIdItem());
        }
        return ret;
    }

    public static Map<Integer, Number> convertToMapOfNumbers(Collection<Recommendation> recommendations) {

        Map<Integer, Number> map = new TreeMap<>();

        for (Recommendation recommendation : recommendations) {
            final int idItem = recommendation.idItem;
            final Number preference = recommendation.preference;

            if (map.containsKey(idItem)) {
                throw new IllegalStateException("Cannot add more than once an item, duplicated item '" + idItem + "' in recommendation list");
            }
            map.put(idItem, preference);
        }

        return map;

    }

    public static Map<Integer, Number> convertToMapOfNumbers_onlyRankPreference(Collection<Recommendation> recommendations) {
        Map<Integer, Number> map = new TreeMap<>();

        final double size = recommendations.size();

        int i = 0;
        for (Recommendation recommendation : recommendations) {

            final int idItem = recommendation.idItem;
            final Number preference = (size - i) / size;

            if (map.containsKey(idItem)) {
                throw new IllegalStateException("Cannot add more than once an item, duplicated item '" + idItem + "' in recommendation list");
            }
            map.put(idItem, preference);

            i++;
        }

        return map;
    }

    /**
     ***************************************************************************
     *********************** ATRIBUTOS DE INSTANCIA
     * *************************************************************************
     */
    private final int idItem;
    private final Number preference;

    /**
     * Constructor que asigna los valores del objeto
     *
     * @param idItem item sobre el que se realiza la recomendación
     * @param preference preferencia que el sistema de recomendación ha asignado
     * al item para el usuario al que se recomienda
     */
    public Recommendation(Integer idItem, Number preference) {
        this.idItem = idItem;
        this.preference = preference;
    }

    /**
     * id del item recomendado
     *
     * @return id del item que se recomienda
     */
    public int getIdItem() {
        return idItem;
    }

    /**
     * Valor asignado por el sistema de recomendación para indicar la relevancia
     * de la recomendación. Cuanto mayor es el valor de la relevancia, más
     * probable es que la recomendación sea relevante
     *
     * @return relevancia de la recomendación
     */
    public Number getPreference() {
        return preference;
    }

    @Override
    public String toString() {
        String preferenceRounded = NumberRounder.round_str(preference);
        return "item:" + idItem + "-->" + preferenceRounded;
    }

    @Override
    public int compareTo(Recommendation o) {
        Comparator<Recommendation> comparator = getRecommendationPreferenceComparator();
        return comparator.compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Recommendation) {
            Recommendation recommendation = (Recommendation) obj;
            if (this.idItem != recommendation.idItem) {
                return false;
            }
            return NumberCompare.equals(this.preference, recommendation.preference, 4);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.idItem;
        hash = 37 * hash + (this.preference != null ? this.preference.hashCode() : 0);
        return hash;
    }

    private static Comparator<Recommendation> getRecommendationPreferenceComparator() {
        Comparator<Recommendation> comparator = (Recommendation o1, Recommendation o2) -> {
            int compare = Double.compare(o1.preference.doubleValue(), o2.preference.doubleValue());
            if (compare != 0) {
                return compare;
            } else {
                return Integer.compare(o1.idItem, o2.idItem);
            }
        };

        comparator = comparator.reversed();

        return comparator;

    }

    /**
     * Compara esta valoración con otra, teniendo en cuenta solo 4 decimales.
     *
     * @param r Recomendación con la que se compara
     * @param numDecimals numero de decimales a tener en cuenta.
     * @return true si las recomendación es del mismo producto y la preferencia
     * es igual hasta el cuarto decimal, false en otro caso.
     */
    public boolean relaxedEquals(Recommendation r, int numDecimals) {

        if (this.idItem != r.idItem) {
            return false;
        }
        return NumberCompare.equals(this.preference, r.preference, numDecimals);
    }
}
