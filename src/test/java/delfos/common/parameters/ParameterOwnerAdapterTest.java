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
package delfos.common.parameters;

import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.RMSMean;
import delfos.common.parameters.chain.ParameterChain;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.similaritymeasures.Manhattan;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class ParameterOwnerAdapterTest {

    @Test
    public void testCloneGRS() {

        ParameterOwner grs = getTestingParameterOwner();

        ParameterOwner cloned = grs.clone();

        List<ParameterChain> chains = ParameterChain.obtainDifferentChains(Arrays.asList(grs, cloned));

        assertTrue(chains.isEmpty());
    }

    @Test
    public void testCloneGRS_parametersChanged() {
        AggregationOfIndividualRatings grs = getTestingParameterOwner();

        AggregationOfIndividualRatings cloned = (AggregationOfIndividualRatings) grs.clone();
        cloned.setParameterValue(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new Mean());

        List<ParameterChain> chains = ParameterChain.obtainDifferentChains(Arrays.asList(grs, cloned));

        assertFalse(chains.isEmpty());
    }

    public AggregationOfIndividualRatings getTestingParameterOwner() {
        AggregationOfIndividualRatings grs
                = new AggregationOfIndividualRatings();
        grs.setParameterValue(AggregationOfIndividualRatings.AGGREGATION_OPERATOR, new RMSMean());
        RecommenderSystem<? extends Object> singleRS;
        singleRS = new KnnMemoryBasedCFRS();
        singleRS.setParameterValue(KnnMemoryBasedCFRS.SIMILARITY_MEASURE, new Manhattan());
        singleRS.setParameterValue(KnnMemoryBasedCFRS.NEIGHBORHOOD_SIZE, 953);
        grs.setParameterValue(AggregationOfIndividualRatings.SINGLE_USER_RECOMMENDER, singleRS);
        return grs;
    }
}
