package delfos.rs;

import java.io.Serializable;

/**
 *
 * @author jcastro
 */
public class EmptyRecommendationModel implements Serializable {

    private static final long serialVersionUID = 6556561L;

    private final RecommenderSystem recommenderSystem;

    public EmptyRecommendationModel(RecommenderSystem recommenderSystem) {
        this.recommenderSystem = recommenderSystem;
    }

    @Override
    public String toString() {

        return "The recommender '" + recommenderSystem.getAlias() + "' does not need any model";
    }

}
