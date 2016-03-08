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
package delfos.group.grs.consensus;

import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ConsensusOutputModel {

    public final double consensusDegree;

    public final int round;

    public final Collection<Recommendation> consensusRecommendations;
    GroupRecommendations groupRecommendation;

    public ConsensusOutputModel() {
        this.consensusDegree = 0;
        this.round = 0;
        this.consensusRecommendations = null;
    }

    public ConsensusOutputModel(double consensusDegree, int round, Collection<Recommendation> consensusRecommendations) {
        this.consensusDegree = consensusDegree;
        this.round = round;
        this.consensusRecommendations = consensusRecommendations;
    }

}
