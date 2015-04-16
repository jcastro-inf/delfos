package delfos.group.grs.consensus;

import java.util.List;
import delfos.rs.recommendation.Recommendation;

/**
 *
* @author Jorge Castro Gallardo
 */
public class ConsensusOutputModel {

    public final double consensusDegree;

    public final int round;

    public final List<Recommendation> consensusRecommendations;

    public ConsensusOutputModel() {
        this.consensusDegree = 0;
        this.round = 0;
        this.consensusRecommendations = null;
    }

    public ConsensusOutputModel(double consensusDegree, int round, List<Recommendation> consensusRecommendations) {
        this.consensusDegree = consensusDegree;
        this.round = round;
        this.consensusRecommendations = consensusRecommendations;
    }

}
