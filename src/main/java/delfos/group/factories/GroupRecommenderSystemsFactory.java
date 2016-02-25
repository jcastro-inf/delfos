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
import delfos.factories.RecommenderSystemsFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.group.grs.benchmark.polylens.PolyLens;
import delfos.group.grs.benchmark.polylens.PolyLens_FLINS2014;
import delfos.group.grs.consensus.ConsensusGRS;
import delfos.group.grs.cww.CentralityWeightedAggregationGRS;
import delfos.group.grs.filtered.GroupRecommenderSystemWithPostFilter;
import delfos.group.grs.filtered.GroupRecommenderSystemWithPreFilter;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import delfos.group.grs.mean.MeanRatingGRS;
import delfos.group.grs.persistence.GroupRecommenderSystem_fixedFilePersistence;
import delfos.group.grs.svd.SVDforGroup_ratingsAggregation;

/**
 * Factoría para los sistemas de recomendación a grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 09-May-2013
 */
public class GroupRecommenderSystemsFactory extends Factory<GroupRecommenderSystem> {

    private static final GroupRecommenderSystemsFactory instance;

    static {
        instance = new GroupRecommenderSystemsFactory();

        //Benchmark techniques.
        instance.addClass(PolyLens.class);
        instance.addClass(PolyLens_FLINS2014.class);

        //Generic techniques.
        instance.addClass(MeanRatingGRS.class);
        instance.addClass(RandomGroupRecommender.class);

        instance.addClass(AggregationOfIndividualRatings.class);
        instance.addClass(AggregationOfIndividualRecommendations.class);
        instance.addClass(GroupRecommenderSystemWithPreFilter.class);
        instance.addClass(GroupRecommenderSystemWithPostFilter.class);
        instance.addClass(GroupRecommenderSystem_fixedFilePersistence.class);
        instance.addClass(SVDforGroup_ratingsAggregation.class);

        instance.addClass(CentralityWeightedAggregationGRS.class);

        instance.addClass(ConsensusGRS.class);

        instance.addClass(HesitantKnnGroupUser.class);

    }

    public static GroupRecommenderSystemsFactory getInstance() {
        return instance;
    }

    private GroupRecommenderSystemsFactory() {
    }

    /**
     * Mete los sistemas de recomendación en la factoría de sistemas de
     * recomendación general.
     */
    public void copyInSingleUserRecommender() {
        for (Class<? extends GroupRecommenderSystem> clase : allClasses.values()) {
            RecommenderSystemsFactory.getInstance().addClass(clase);
        }
    }
}
