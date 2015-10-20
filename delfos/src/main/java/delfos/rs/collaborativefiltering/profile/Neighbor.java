package delfos.rs.collaborativefiltering.profile;

import delfos.common.decimalnumbers.NumberCompare;
import delfos.dataset.basic.features.EntityWithFeatures;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Clase que representa un vecino en un algoritmo de recomendación colaborativo
 * basado en vecinos cercanos.
 *
 * @version 1.0 (Unknow Date)
 * @version 1.1 (28 de Febrero de 2013)
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class Neighbor implements Comparable<Neighbor>, Serializable {

    private static final long serialVersionUID = 106L;

    public static final Comparator<Neighbor> BY_ID = (Neighbor o1, Neighbor o2) -> Integer.compare(o1.getIdNeighbor(), o2.getIdNeighbor());
    public static final Comparator<Neighbor> BY_SIMILARITY_ASC = (Neighbor o1, Neighbor o2) -> {
        if (Float.isNaN(o1.similarity) && Float.isNaN(o2.similarity)) {
            return BY_ID.compare(o1, o2);
        } else if (Float.isNaN(o1.similarity)) {
            return 1;
        } else if (Float.isNaN(o2.similarity)) {
            return -1;
        } else if (Float.compare(o1.getSimilarity(), o2.getSimilarity()) != 0) {
            return Float.compare(o1.getSimilarity(), o2.getSimilarity());
        } else {
            return BY_ID.compare(o1, o2);
        }
    };
    public static final Comparator<Neighbor> BY_SIMILARITY_DESC = (Neighbor o1, Neighbor o2) -> {
        if (Float.isNaN(o1.similarity) && Float.isNaN(o2.similarity)) {
            return BY_ID.compare(o1, o2);
        } else if (Float.isNaN(o1.similarity)) {
            return 1;
        } else if (Float.isNaN(o2.similarity)) {
            return -1;
        } else if (-Float.compare(o1.getSimilarity(), o2.getSimilarity()) != 0) {
            return -Float.compare(o1.getSimilarity(), o2.getSimilarity());
        } else {
            return BY_ID.compare(o1, o2);
        }
    };

    public static Map<Integer, Double> getNeighborsMap(List<Neighbor> neighbors) {
        Map<Integer, Double> ret = new TreeMap<>();

        neighbors.stream().forEach((n) -> {
            Double similarity = (double) n.similarity;
            Integer idNeighbor = n.idNeighbor;
            ret.put(idNeighbor, similarity);
        });

        return ret;
    }

    /**
     * Entidad que es este vecino (Usuario o producto).
     */
    private final RecommendationEntity recommendationEntity;
    /**
     * ID del vecino.
     */
    private final int idNeighbor;
    private EntityWithFeatures neighbor;
    /**
     * Similitud con el objeto del que es vecino.
     */
    private final float similarity;

    public Neighbor() {
        this.recommendationEntity = null;
        this.idNeighbor = -1;
        this.similarity = -1;
    }

    /**
     * Constructor por defecto que asigna el tipo de entidad, el id del vecino y
     * la similaridad (ponderación) de entre todos los vecinos considerados
     *
     * @param entity tipo de entidad vecina
     * @param idNeighbor id de la entidad vecina
     * @param similarity peso del vecino
     */
    @Deprecated
    public Neighbor(RecommendationEntity entity, int idNeighbor, double similarity) {
        this.recommendationEntity = entity;
        this.idNeighbor = idNeighbor;
        this.similarity = (float) similarity;
    }

    public Neighbor(RecommendationEntity recommendationEntity, EntityWithFeatures neighbor, double similarity) {
        this.recommendationEntity = recommendationEntity;
        this.idNeighbor = neighbor.getId();
        this.neighbor = neighbor;
        this.similarity = (float) similarity;
    }

    /**
     * Devuelve el id de este vecino.
     *
     * @return Id de este vecino.
     */
    public int getIdNeighbor() {
        return idNeighbor;
    }

    /**
     * Similitud con el objeto del que es vecino.
     *
     * @return Similitud.
     */
    public float getSimilarity() {
        return similarity;
    }

    @Override
    public int compareTo(Neighbor t) {
        if (this.recommendationEntity.compareTo(t.getRecommendationEntity()) == 0) {
            if (BY_ID.compare(this, t) == 0) {
                return BY_SIMILARITY_DESC.compare(this, t);
            } else {
                return BY_ID.compare(this, t);
            }
        }
        return this.recommendationEntity.compareTo(t.getRecommendationEntity());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Neighbor)) {
            return false;
        }

        Neighbor otherNeighbor = (Neighbor) obj;
        if (recommendationEntity != otherNeighbor.recommendationEntity) {
            return false;
        } else if (idNeighbor != otherNeighbor.idNeighbor) {
            return false;
        } else {
            return NumberCompare.equals(this.similarity, otherNeighbor.similarity);
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.recommendationEntity != null ? this.recommendationEntity.hashCode() : 0);
        hash = 97 * hash + this.idNeighbor;
        hash = 97 * hash + Float.floatToIntBits(this.similarity);
        return hash;
    }

    /**
     * Devuelve el tipo de objeto al que representa el id devuelto por el método
     * {@link Neighbor#getIdNeighbor()}
     *
     * @return tipo de entidad vecina
     */
    public RecommendationEntity getRecommendationEntity() {
        return recommendationEntity;
    }

    public EntityWithFeatures getNeighbor() {
        return neighbor;
    }

    @Override
    public String toString() {
        String ret = "unknow";
        switch (recommendationEntity) {
            case ITEM:
                ret = "idItem:" + getIdNeighbor() + " -> " + getSimilarity();
                break;
            case USER:
                ret = "idUser:" + getIdNeighbor() + " -> " + getSimilarity();
                break;
        }
        return ret;
    }
}
