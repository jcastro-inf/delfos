package delfos.rs.recommendation;

import delfos.common.decimalnumbers.NumberCompare;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.dataset.basic.item.Item;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Almacena una recomendación devuelta por un sistema de recomendación.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Recommendation implements Comparable<Recommendation>, Serializable {

    private static void validateComparatorParameters(Recommendation o1, Recommendation o2) {
        if (o1 == null) {
            throw new IllegalStateException("recommendation 1 is null");
        } else if (o2 == null) {
            throw new IllegalStateException("recommendation 2 is null");
        } else if (o1.getPreference() == null) {
            throw new IllegalStateException("Preference value of recommendation 1 is null");
        } else if (o2.getPreference() == null) {
            throw new IllegalStateException("Preference value of recommendation 2 is null");
        }
    }

    public static final Comparator<Recommendation> BY_ID = (Recommendation o1, Recommendation o2) -> {
        validateComparatorParameters(o1, o2);
        return Integer.compare(o1.getIdItem(), o2.getIdItem());
    };
    public static final Comparator<Recommendation> BY_PREFERENCE_ASC = (Recommendation o1, Recommendation o2) -> {
        validateComparatorParameters(o1, o2);
        return Double.compare(o1.getPreference().doubleValue(), o2.getPreference().doubleValue());
    };
    public static final Comparator<Recommendation> BY_PREFERENCE_DESC = (Recommendation o1, Recommendation o2) -> {
        validateComparatorParameters(o1, o2);
        return -Double.compare(o1.getPreference().doubleValue(), o2.getPreference().doubleValue());
    };

    private static final long serialVersionUID = 5468;

    /**
     * Si un item esta dos veces, lo devuelve una sola (devuelve un conjunto, no
     * una colección).
     *
     * @param recommendations
     * @return
     */
    public static Set<Integer> getSetOfItems(Collection<Recommendation> recommendations) {
        return recommendations.parallelStream()
                .map((recommendation) -> recommendation.getIdItem())
                .collect(Collectors.toSet());
    }

    public static Map<Integer, Number> convertToMapOfNumbers(Collection<Recommendation> recommendations) {

        Map<Integer, Number> map = new TreeMap<>();

        for (Recommendation recommendation : recommendations) {
            final int idItem = recommendation.getIdItem();
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

            final int idItem = recommendation.getIdItem();
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
    private final Number preference;
    protected final Item item;

    /**
     * Constructor que asigna los valores del objeto
     *
     * @param idItem item sobre el que se realiza la recomendación
     * @param preference preferencia que el sistema de recomendación ha asignado
     * al item para el usuario al que se recomienda
     */
    @Deprecated
    public Recommendation(Integer idItem, Number preference) {
        if (idItem == null) {
            throw new IllegalArgumentException("Item cannot be null");
        } else if (preference == null) {
            throw new IllegalArgumentException("Preference cannot be null");
        }

        this.preference = preference;
        this.item = new Item(idItem);
    }

    public Recommendation(Item item, Number preference) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        } else if (preference == null) {
            throw new IllegalArgumentException("Preference cannot be null");
        }

        this.preference = preference;
        this.item = item;
    }

    /**
     * id del item recomendado
     *
     * @return id del item que se recomienda
     */
    @Deprecated
    public int getIdItem() {
        return item.getId();
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
        return "item:" + item.getId() + "-->" + preferenceRounded;
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
            if (this.getIdItem() != recommendation.getIdItem()) {
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
        hash = 37 * hash + this.getIdItem();
        hash = 37 * hash + (this.preference != null ? this.preference.hashCode() : 0);
        return hash;
    }

    public static Comparator<Recommendation> getRecommendationPreferenceComparator() {
        Comparator<Recommendation> comparator = (Recommendation o1, Recommendation o2) -> {
            int compare = Double.compare(o1.preference.doubleValue(), o2.preference.doubleValue());
            if (compare != 0) {
                return compare;
            } else {
                return Integer.compare(o1.getIdItem(), o2.getIdItem());
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

        if (this.getIdItem() != r.getIdItem()) {
            return false;
        }
        return NumberCompare.equals(this.preference, r.preference, numDecimals);
    }

    public Item getItem() {
        return item;
    }

}
