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
package delfos.rs.contentbased.vsm.booleanvsm.basic;

import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.contentbased.ContentBasedRecommender;
import static delfos.rs.contentbased.vsm.ContentBasedVSMRS.SIMILARITY_MEASURE;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

/**
 * El sistema de recomendación basado en contenido con modelado booleano
 * (BasicBooleanCBRS) realiza una transformación de las características
 * (numéricas o categóricas) de los productos a características booleanas. De
 * esta manera, el perfil de cada producto consiste en un vector de ceros y unos
 * que representa sus características. Una vez realizado esto se calcula el
 * perfil de usuario, que es un vector cuyos valores son la suma de los vectores
 * asociados a los productos que ha indicado que son de su agrado. En la fase de
 * recomendación se calcula la similitud entre el perfil de usuario y el perfil
 * de cada producto no valorado.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 (19 Octubre 2011)
 * @version 2.0 (28 de Febrero de 2013) Refactorización de las clases asociadas
 * a los perfiles de usuario.
 * @version 2.1 9-Octubre-2013 Incorporación del método makeUserModel
 */
public class BasicBooleanCBRS extends ContentBasedRecommender<BasicBooleanCBRSModel, SparseVector> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto del sistema de recomendación, que añade sus
     * parámetros.
     */
    public BasicBooleanCBRS() {
        super();
        addParameter(SIMILARITY_MEASURE);
    }

    /**
     * Constructor del sistema de recomendación que establece la medida de
     * similitud indicada por parámetro.
     *
     * @param similarityMeasure Medida de similitud que utiliza el sistema.
     */
    public BasicBooleanCBRS(BasicSimilarityMeasure similarityMeasure) {
        this();
        setParameterValue(SIMILARITY_MEASURE, similarityMeasure);
    }

    @Override
    public BasicBooleanCBRSModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        BooleanFeaturesTransformation booleanFeaturesTransformation = new BooleanFeaturesTransformation(contentDataset);

        BasicBooleanCBRSModel model = new BasicBooleanCBRSModel(booleanFeaturesTransformation);
        fireBuildingProgressChangedEvent("Model creation", 0, -1);
        int i = 1;
        for (Item item : contentDataset) {

            MutableSparseVector itemProfile = booleanFeaturesTransformation.newProfile();

            for (Feature f : item.getFeatures()) {
                Object value = item.getFeatureValue(f);

                long indexFeature = booleanFeaturesTransformation.getFeatureIndex(f, value);
                itemProfile.set(indexFeature, 1);
            }

            model.put(item.getId(), itemProfile);

            fireBuildingProgressChangedEvent("Profile creation", (int) ((float) i * 100 / contentDataset.size()), -1);
            i++;
        }

        fireBuildingProgressChangedEvent("Profile creation", 100, -1);
        return model;
    }

    @Override
    protected Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader,
            BasicBooleanCBRSModel model,
            SparseVector userProfile,
            Collection<Integer> candidateItems)
            throws UserNotFound, ItemNotFound,
            CannotLoadRatingsDataset, CannotLoadContentDataset {
        if (model == null) {
            throw new IllegalArgumentException("Recommendation model is null");
        }

        final BasicSimilarityMeasure similarity = (BasicSimilarityMeasure) getParameterValue(SIMILARITY_MEASURE);
        Collection<Recommendation> recomendaciones = new ArrayList<>();

        List<Float> userVectorProfile = model.booleanFeaturesTransformation.getFloatVector(userProfile);
        for (int idItem : candidateItems) {
            List<Float> itemVectorProfile = model.getBooleanFeaturesTransformation().getFloatVector(model.get(idItem));
            float sim;
            try {
                sim = similarity.similarity(itemVectorProfile, userVectorProfile);
                recomendaciones.add(new Recommendation(idItem, sim));
            } catch (CouldNotComputeSimilarity ex) {
//                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            }
        }

        return recomendaciones;
    }

    @Override
    protected SparseVector makeUserProfile(int idUser, DatasetLoader<? extends Rating> datasetLoader, BasicBooleanCBRSModel model) throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound {

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        MutableSparseVector userProfile = model.getBooleanFeaturesTransformation().newProfile();

        Map<Integer, ? extends Rating> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);

        //Calculo del perfil
        for (Map.Entry<Integer, ? extends Rating> entry : userRatingsRated.entrySet()) {
            int idItem = entry.getKey();
            Rating rating = entry.getValue();
            SparseVector itemProfile = model.get(idItem);
            if (datasetLoader.getDefaultRelevanceCriteria().isRelevant(rating)) {
                userProfile.add(itemProfile);
            }
        }

        double norm = userProfile.norm();
        for (VectorEntry vectorEntry : userProfile.fast()) {
            long key = vectorEntry.getKey();
            double value = vectorEntry.getValue();
            double normalisedValue = value / norm;
            userProfile.set(key, normalisedValue);
        }

        return userProfile;
    }
}
