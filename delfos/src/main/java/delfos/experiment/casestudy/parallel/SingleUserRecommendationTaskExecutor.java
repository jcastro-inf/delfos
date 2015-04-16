package delfos.experiment.casestudy.parallel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.RecommenderSystem;
import delfos.rs.recommendation.Recommendation;

/**
 * Realiza la ejecución de una recomendación de un usuario y la almacena. Al
 * terminar la tarea, libera los recursos que ya no son necesarios.
 *
* @author Jorge Castro Gallardo
 */
public class SingleUserRecommendationTaskExecutor implements SingleTaskExecute<SingleUserRecommendationTask> {

    @Override
    public void executeSingleTask(SingleUserRecommendationTask task) {
        RecommenderSystem recommenderSystem = task.getRecommenderSystem();
        DatasetLoader<? extends Rating> datasetLoader = task.getDatasetLoader();
        int idUser = task.getIdUser();
        Collection<Integer> idItemList = task.getIdItemList();
        Object model = task.getRecommenderSystemModel();
        try {
            List<Recommendation> recommend = recommenderSystem.recommendOnly(datasetLoader, model, idUser, idItemList);
            task.setRecommendationList(recommend);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        } catch (NotEnoughtUserInformation ex) {
            //Como estamos en una clase que pertenece a los casos de estudio, se captura esta excepción para asignar una lista de recomendaciones vacía al usuario, para solucionar el fallo de lista nula en la composición de resultados.
            task.setRecommendationList(Collections.EMPTY_LIST);
        }
    }
}
