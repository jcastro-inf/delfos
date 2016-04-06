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

import delfos.rs.RecommenderSystemAdapter;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.similaritymeasures.CosineCoefficient;
import delfos.similaritymeasures.Distance3Degree;
import delfos.similaritymeasures.EntropyOfDifferences;
import delfos.similaritymeasures.EuclideanDistance;
import delfos.similaritymeasures.HammingDistance;
import delfos.similaritymeasures.MSD;
import delfos.similaritymeasures.Manhattan;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.ProximityImpactPopularity;
import delfos.similaritymeasures.RatingRangeBased;
import delfos.similaritymeasures.RefinedHammingDistance;
import delfos.similaritymeasures.SimilarityMeasure;
import delfos.similaritymeasures.Tanimoto;
import delfos.similaritymeasures.WeightedSimilarityMeasure;
import delfos.similaritymeasures.useruser.ConditionalProbability;
import delfos.similaritymeasures.useruser.CosineAsymmetric;
import delfos.similaritymeasures.useruser.demographic.DemographicSimilarity;
import delfos.similaritymeasures.useruser.Jaccard;
import delfos.similaritymeasures.useruser.MSDAsymmetric;
import delfos.similaritymeasures.useruser.RelevanceFactor;
import delfos.similaritymeasures.useruser.SorensenIndex;
import delfos.similaritymeasures.useruser.SorensenIndex_improved;
import delfos.similaritymeasures.useruser.UserUserMultipleCorrelationCoefficient;
import delfos.similaritymeasures.useruser.UserUserSimilarity;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper;
import delfos.similaritymeasures.useruser.UserUserSimilarityWrapper_relevanceFactor;
import delfos.similaritymeasures.useruser.UserUserSimilarity_buffered;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Factoría que conoce todas las medidas de similitud que la biblioteca de
 * recomendación incorppora implementadas. Permite obtenerlas según qué tipo de
 * medida se necesite en cada momento, por ejemplo medidas que soporten la
 * ponderación de valores ({@link WeightedSimilarityMeasure}) o que estén
 * diseñadas para funcionar en sistemas de recomendación colaborativos
 * ({@link CollaborativeSimilarityMeasure}).
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 * @version 2.0 9-Mayo-2013 Ahora la clase hereda de {@link Factory}.
 *
 * @see SimilarityMeasure
 */
public class SimilarityMeasuresFactory extends Factory<SimilarityMeasure> {

    private static final SimilarityMeasuresFactory instance;

    public static SimilarityMeasuresFactory getInstance() {
        return instance;
    }

    static {
        instance = new SimilarityMeasuresFactory();
        instance.addClass(CosineCoefficient.class);
        instance.addClass(ProximityImpactPopularity.class);
        instance.addClass(EuclideanDistance.class);
        instance.addClass(PearsonCorrelationCoefficient.class);
        instance.addClass(Tanimoto.class);
        instance.addClass(HammingDistance.class);
        instance.addClass(RefinedHammingDistance.class);
        instance.addClass(Manhattan.class);
        instance.addClass(RatingRangeBased.class);
        instance.addClass(Distance3Degree.class);

        //UserUser similarities.
        instance.addClass(UserUserSimilarityWrapper.class);
        instance.addClass(UserUserMultipleCorrelationCoefficient.class);
        instance.addClass(UserUserSimilarityWrapper_relevanceFactor.class);
        instance.addClass(MSD.class);

        //Asymmetric similarities for users
        instance.addClass(ConditionalProbability.class);
        instance.addClass(SorensenIndex_improved.class);
        instance.addClass(CosineAsymmetric.class);
        instance.addClass(MSDAsymmetric.class);

        //Taking into account only the rated items and disregarding their values.
        instance.addClass(SorensenIndex.class);
        instance.addClass(Jaccard.class);

        //Similarity penalties
        instance.addClass(RelevanceFactor.class);
        instance.addClass(EntropyOfDifferences.class);

        //Buffered
        instance.addClass(UserUserSimilarity_buffered.class);

        //Demographic similarities
        instance.addClass(DemographicSimilarity.class);

    }

    /**
     * Devuelve todas las medidas de similitud colaborativas.
     *
     * @param rs Sistema de recomendación para el que se utilizará la medida.
     * @return Lista de medidas de similitud colaborativas que cumplen la
     * condición.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<CollaborativeSimilarityMeasure> getCollaborativeSimilarityMeasures(Class<? extends RecommenderSystemAdapter> rs) {
        ArrayList<CollaborativeSimilarityMeasure> ret = new ArrayList<>();
        allClasses.values().stream().forEach((c) -> {
            try {
                if (CollaborativeSimilarityMeasure.class.isAssignableFrom(c)) {
                    CollaborativeSimilarityMeasure recommender = (CollaborativeSimilarityMeasure) c.newInstance();
                    ret.add(recommender);
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        });
        return ret;
    }

    public Collection<BasicSimilarityMeasure> getBasicSimilarityMeasures() {
        ArrayList<BasicSimilarityMeasure> ret = new ArrayList<>();
        allClasses.values().stream().forEach((c) -> {
            try {
                if (BasicSimilarityMeasure.class.isAssignableFrom(c)) {
                    BasicSimilarityMeasure basicSimilarityMeasure = (BasicSimilarityMeasure) c.newInstance();
                    ret.add(basicSimilarityMeasure);
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        });
        return ret;
    }

    public Collection<WeightedSimilarityMeasure> getWeightedSimilarityMeasures() {
        ArrayList<WeightedSimilarityMeasure> ret = new ArrayList<>();
        allClasses.values().stream().forEach((c) -> {
            try {
                if (WeightedSimilarityMeasure.class.isAssignableFrom(c)) {
                    WeightedSimilarityMeasure weightedSimilarityMeasure = (WeightedSimilarityMeasure) c.newInstance();
                    ret.add(weightedSimilarityMeasure);
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        });
        return ret;
    }

    public Collection<UserUserSimilarity> getUserUserSimilarityMeasures() {
        ArrayList<UserUserSimilarity> ret = new ArrayList<>();
        allClasses.values().stream().forEach((c) -> {
            try {
                if (UserUserSimilarity.class.isAssignableFrom(c)) {
                    UserUserSimilarity userUserSimilarity = (UserUserSimilarity) c.newInstance();
                    ret.add(userUserSimilarity);
                }
            } catch (IllegalAccessException | InstantiationException ex) {
                exceptionInCreation(c, ex);
            }
        });
        return ret;
    }

    public Collection<Class<UserUserSimilarity>> getUserUserSimilarityMeasuresClasses() {
        ArrayList<Class<UserUserSimilarity>> ret = new ArrayList<>();
        allClasses.values().stream().filter((c) -> (UserUserSimilarity.class.isAssignableFrom(c))).map((c) -> (Class<UserUserSimilarity>) c).forEach((userUserSimilarityClass) -> {
            ret.add(userUserSimilarityClass);
        });
        return ret;
    }
}
