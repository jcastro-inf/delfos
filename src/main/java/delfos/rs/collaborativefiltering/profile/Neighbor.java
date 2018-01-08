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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Neighbor implements Comparable<Neighbor>, Serializable {

    private static final long serialVersionUID = 106L;

    public static final Comparator<Neighbor> BY_ID = (Neighbor o1, Neighbor o2) -> Long.compare(o1.getIdNeighbor(), o2.getIdNeighbor());
    public static final Comparator<Neighbor> BY_SIMILARITY_ASC = (Neighbor o1, Neighbor o2) -> {
        if (Double.isNaN(o1.similarity) && Double.isNaN(o2.similarity)) {
            return BY_ID.compare(o1, o2);
        } else if (Double.isNaN(o1.similarity)) {
            return 1;
        } else if (Double.isNaN(o2.similarity)) {
            return -1;
        } else if (Double.compare(o1.getSimilarity(), o2.getSimilarity()) != 0) {
            return Double.compare(o1.getSimilarity(), o2.getSimilarity());
        } else {
            return BY_ID.compare(o1, o2);
        }
    };
    public static final Comparator<Neighbor> BY_SIMILARITY_DESC = (Neighbor o1, Neighbor o2) -> {
        if (Double.isNaN(o1.similarity) && Double.isNaN(o2.similarity)) {
            return BY_ID.compare(o1, o2);
        } else if (Double.isNaN(o1.similarity)) {
            return 1;
        } else if (Double.isNaN(o2.similarity)) {
            return -1;
        } else if (-Double.compare(o1.getSimilarity(), o2.getSimilarity()) != 0) {
            return -Double.compare(o1.getSimilarity(), o2.getSimilarity());
        } else {
            return BY_ID.compare(o1, o2);
        }
    };

    public static Map<Long, Double> getNeighborsMap(List<Neighbor> neighbors) {
        Map<Long, Double> ret = new TreeMap<>();

        neighbors.stream().forEach((n) -> {
            Double similarity = (double) n.similarity;
            Long idNeighbor = n.idNeighbor;
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
    private final long idNeighbor;
    private EntityWithFeatures neighbor;
    /**
     * Similitud con el objeto del que es vecino.
     */
    private final double similarity;

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
    public Neighbor(
            RecommendationEntity entity,
            long idNeighbor,
            double similarity) {
        this.recommendationEntity = entity;
        this.idNeighbor = idNeighbor;
        this.similarity = (double) similarity;
    }

    public Neighbor(
            RecommendationEntity recommendationEntity,
            EntityWithFeatures neighbor,
            double similarity) {
        this.recommendationEntity = recommendationEntity;
        this.idNeighbor = neighbor.getId();
        this.neighbor = neighbor;
        this.similarity = (double) similarity;
    }

    /**
     * Devuelve el id de este vecino.
     *
     * @return Id de este vecino.
     */
    public long getIdNeighbor() {
        return idNeighbor;
    }

    /**
     * Similitud con el objeto del que es vecino.
     *
     * @return Similitud.
     */
    public double getSimilarity() {
        return similarity;
    }

    @Override
    public int compareTo(Neighbor neighbor) {
        return Neighbor.BY_SIMILARITY_DESC.compare(this, neighbor);
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
        hash = 97 * hash + Long.hashCode(this.idNeighbor);
        hash = 97 * hash + Double.hashCode(this.similarity);
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
