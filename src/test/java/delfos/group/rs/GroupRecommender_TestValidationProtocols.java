package delfos.group.rs;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommendationModel;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Sistema de recomendación a grupos que comprueba que se aplican correctamente
 * los protocolos de validación a la hora de realizar la experimentación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 13-Feb-2013
 * @version 2.0 06-Mar-2013 Mejorada la evaluación de la corrección.
 */
public class GroupRecommender_TestValidationProtocols<RatingType extends Rating> extends GroupRecommenderSystemAdapter<Object, Object> {

    private static final long serialVersionUID = 45L;
    /**
     * Almacena el dataset usado en la fase de construcción del modelo general.
     */
    protected RatingsDataset<RatingType> datasetEnBuild;
    /**
     * Almacena el dataset usado en la fase de construcción del modelo del
     * grupo. Se almacenan varios porque puede cambiar.
     */
    protected Map<GroupOfUsers, RatingsDataset> datasetsEnConstruccionModeloGrupo = new TreeMap<GroupOfUsers, RatingsDataset>();
    /**
     * Sistema de recomendación aleatorio que se utiliza para devolver algún
     * tipo de recomendaciones.
     */
    protected RandomGroupRecommender randomGroupRecommender = new RandomGroupRecommender();

    public GroupRecommender_TestValidationProtocols() {
        super();
    }

    @Override
    public <RatingType2 extends Rating> Object buildRecommendationModel(DatasetLoader<RatingType2> datasetLoader) throws CannotLoadRatingsDataset {
        datasetEnBuild = (RatingsDataset<RatingType>) datasetLoader.getRatingsDataset();
        Global.showInfoMessage("Built.\n");
        return null;
    }

    @Override
    public <RatingType extends Rating> Object buildGroupModel(DatasetLoader<RatingType> datasetLoader, Object RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {
        datasetsEnConstruccionModeloGrupo.put(
                groupOfUsers,
                datasetLoader.getRatingsDataset());
        Global.showInfoMessage("BuiltGroupModel for group " + groupOfUsers.toString() + ".\n");

        return null;
    }

    @Override
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, Object RecommendationModel, Object groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        /*
         * Compruebo si alguna vez me piden recomendaciones que se conocian en
         * la construcción del modelo general
         */
        Global.showInfoMessage("Recommended for group " + groupOfUsers.toString() + ".\n");

        //Chequeo si el dataset es el mismo
        if (datasetEnBuild == datasetLoader.getRatingsDataset()) {
            Global.showInfoMessage("El dataset en build general y en recomendación es el mismo!\n");
        }

        //Compruebo si en el dataset actual se tienen las valoraciones a predecir
        for (long idUser : groupOfUsers.getIdMembers()) {
            boolean error = false;
            Collection<Long> userRated = datasetLoader.getRatingsDataset().getUserRated(idUser);
            for (Item item : candidateItems) {
                if (userRated.contains(item.getId())) {
                    Global.showWarning("El producto a predecir está en el dataset!!!!");
                    error = true;
                }
                if (error) {
                    break;
                }
            }
            if (error) {
                break;
            }
        }

        //Compruebo si en el dataset de construcción del modelo del grupo estaba la valoración
        for (long idUser : groupOfUsers.getIdMembers()) {
            boolean error = false;
            Collection<Long> userRated = datasetsEnConstruccionModeloGrupo.get(groupOfUsers).getUserRated(idUser);
            for (Item item : candidateItems) {
                if (userRated.contains(item.getId())) {
                    Global.showWarning("El dataset de construcción del modelo de este grupo contiene el rating a predecir!!!!");
                    error = true;
                }
                if (error) {
                    break;
                }
            }
            if (error) {
                break;
            }
        }

        //Compruebo si en el dataset de construcción del modelo general estaba la valoración
        for (long idUser : groupOfUsers.getIdMembers()) {
            boolean error = false;
            Collection<Long> userRated = datasetEnBuild.getUserRated(idUser);
            for (Item item : candidateItems) {
                if (userRated.contains(item.getId())) {
                    Global.showWarning("El dataset de construcción del modelo general contiene el rating a predecir!!!!");
                    error = true;
                }
                if (error) {
                    break;
                }
            }
            if (error) {
                break;
            }
        }

        if (datasetsEnConstruccionModeloGrupo.get(groupOfUsers) == datasetLoader.getRatingsDataset()) {
            Global.showWarning("El dataset en build del grupo y en recomendación es el mismo!");
        }

        //Finalmente recomiendo
        datasetsEnConstruccionModeloGrupo.remove(groupOfUsers);

        if (RecommendationModel instanceof RandomRecommendationModel) {
            RandomRecommendationModel randomRecommendationModel = (RandomRecommendationModel) RecommendationModel;

            return randomGroupRecommender.recommendOnly(
                    datasetLoader,
                    randomRecommendationModel,
                    groupOfUsers,
                    groupOfUsers,
                    candidateItems);
        } else {
            throw new IllegalArgumentException("The model type is not the correct for this recommender.");
        }
    }

    @Override
    public boolean isRatingPredictorRS() {
        return false;
    }
}
