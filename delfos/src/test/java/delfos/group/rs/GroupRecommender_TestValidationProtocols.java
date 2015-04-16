package delfos.group.rs;

import delfos.group.grs.RandomGroupRecommender;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommenderModel;
import delfos.rs.recommendation.Recommendation;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.Global;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Sistema de recomendación a grupos que comprueba que se aplican correctamente
 * los protocolos de validación a la hora de realizar la experimentación.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 13-Feb-2013
 * @version 2.0 06-Mar-2013 Mejorada la evaluación de la corrección.
 */
public class GroupRecommender_TestValidationProtocols extends GroupRecommenderSystemAdapter<Object, Object> {

    private static final long serialVersionUID = 45L;
    /**
     * Almacena el dataset usado en la fase de construcción del modelo general.
     */
    protected RatingsDataset<? extends Rating> datasetEnBuild;
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
    public Object build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {
        datasetEnBuild = datasetLoader.getRatingsDataset();
        Global.showMessage("Built.\n");
        return null;
    }

    @Override
    public Object buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, Object recommenderSystemModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset {
        datasetsEnConstruccionModeloGrupo.put(
                groupOfUsers,
                datasetLoader.getRatingsDataset());
        Global.showMessage("BuiltGroupModel for group " + groupOfUsers.toString() + ".\n");

        return null;
    }

    @Override
    public List<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, Object recommenderSystemModel, Object groupModel, GroupOfUsers groupOfUsers, Collection<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        /*
         * Compruebo si alguna vez me piden recomendaciones que se conocian en
         * la construcción del modelo general
         */
        Global.showMessage("Recommended for group " + groupOfUsers.toString() + ".\n");

        //Chequeo si el dataset es el mismo
        if (datasetEnBuild == datasetLoader.getRatingsDataset()) {
            Global.showMessage("El dataset en build general y en recomendación es el mismo!\n");
        }

        //Compruebo si en el dataset actual se tienen las valoraciones a predecir
        for (int idUser : groupOfUsers.getGroupMembers()) {
            boolean error = false;
            Collection<Integer> userRated = datasetLoader.getRatingsDataset().getUserRated(idUser);
            for (int idItem : idItemList) {
                if (userRated.contains(idItem)) {
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
        for (int idUser : groupOfUsers.getGroupMembers()) {
            boolean error = false;
            Collection<Integer> userRated = datasetsEnConstruccionModeloGrupo.get(groupOfUsers).getUserRated(idUser);
            for (int idItem : idItemList) {
                if (userRated.contains(idItem)) {
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
        for (int idUser : groupOfUsers.getGroupMembers()) {
            boolean error = false;
            Collection<Integer> userRated = datasetEnBuild.getUserRated(idUser);
            for (int idItem : idItemList) {
                if (userRated.contains(idItem)) {
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

        if (recommenderSystemModel instanceof RandomRecommenderModel) {
            RandomRecommenderModel randomRecommenderModel = (RandomRecommenderModel) recommenderSystemModel;

            return randomGroupRecommender.recommendOnly(datasetLoader, randomRecommenderModel, groupOfUsers, groupOfUsers, idItemList);
        } else {
            throw new IllegalArgumentException("The model type is not the correct for this recommender.");
        }
    }

    @Override
    public boolean isRatingPredictorRS() {
        return false;
    }
}
