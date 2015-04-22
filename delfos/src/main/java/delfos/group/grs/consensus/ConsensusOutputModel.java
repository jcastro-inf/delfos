package delfos.group.grs.consensus;

import delfos.rs.recommendation.Recommendation;
import java.util.Collection;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class ConsensusOutputModel {

    public final double consensusDegree;

    public final int round;

    public final Collection<Recommendation> consensusRecommendations;

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
