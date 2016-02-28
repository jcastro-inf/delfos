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
package delfos.dataset.generated.random;

import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.rating.domain.IntegerDomain;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.experiment.SeedHolder;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Dataset loader aleatorio. Esta clase se usa para testear los sistemas de
 * recomendación y comprobar la corrección del código.
 *
 * <p>
 * <p>
 * No usar en sistemas reales.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 31-Jan-2013
 */
public class RandomDatasetLoader extends DatasetLoaderAbstract<RatingWithTimestamp> implements ContentDatasetLoader, UsersDatasetLoader, SeedHolder {

    private static final long serialVersionUID = 1L;
    private RatingsDataset<RatingWithTimestamp> rd;
    private RandomContentDataset cd;
    /**
     * Parámetro para definir el número de usuarios que el dataset tendrá. Por
     * defecto son 50 usuarios.
     */
    public final static Parameter ratings_numUsers = new Parameter("ratings_numUsers", new IntegerParameter(1, 1000000, 50));
    /**
     * Parámetro para definir el número de productos que el dataset tendrá. Por
     * defecto son 50 productos.
     */
    public final static Parameter ratings_numItems = new Parameter("ratings_numItems", new IntegerParameter(1, 1000000, 50));
    /**
     * Porcentaje de valoraciones que están definidas en el dataset. Por defecto
     * es el 50%.
     */
    public final static Parameter ratings_loadFactor = new Parameter("ratings_loadFactor", new DoubleParameter(0, 1, 0.5f));
    /**
     * Valoración mas baja que un usuario puede dar. Por defecto es 1.
     */
    public final static Parameter ratings_minRating = new Parameter("ratings_minRating", new IntegerParameter(1, 1000000, 1));
    /**
     * Valoración más alta que un usuario puede dar. Por defecto es 5.
     */
    public final static Parameter ratings_maxRating = new Parameter("ratings_maxRating", new IntegerParameter(1, 1000000, 5));
    /**
     * Valoración más alta que un usuario puede dar. Por defecto es 5.
     */
    public final static Parameter ratings_domain_type = new Parameter("ratings_domain_type", new ObjectParameter(Arrays.asList("Integer", "Decimal"), "Decimal"));
    /**
     * Número de características numéricas que los productos tendrán. Por
     * defecto es 2.
     */
    public final static Parameter content_numNumericFeatures = new Parameter("content_numNumericFeatures", new IntegerParameter(0, 1000000, 2));
    /**
     * Número de características categóricas que los productos tendrán. Por
     * defecto es 2.
     */
    public final static Parameter content_numNominalFeatures = new Parameter("content_numNominalFeatures", new IntegerParameter(0, 1000000, 2));
    /**
     * Número de valores distintos de las características numéricas que los
     * productos tendrán. Por defecto es 2.
     */
    public final static Parameter content_numNumericalDifferentValues = new Parameter("content_numNumericalDifferentValues", new IntegerParameter(1, 1000000, 2));
    private UsersDatasetAdapter ud;

    /**
     * Constructor por defecto del dataset aleatorio, que añade los parámetros y
     * deja su valor al valor por defecto.
     */
    public RandomDatasetLoader() {
        super();
        addParameter(SeedHolder.SEED);
        addParameter(ratings_numUsers);
        addParameter(ratings_numItems);
        addParameter(ratings_loadFactor);
        addParameter(ratings_minRating);
        addParameter(ratings_maxRating);
        addParameter(ratings_domain_type);
        addParameter(content_numNumericFeatures);
        addParameter(content_numNominalFeatures);
        addParameter(content_numNumericalDifferentValues);
        addParammeterListener(() -> {
            rd = null;
            cd = null;
        });
    }

    /**
     * Constructor que establece el número de usuarios, de productos y el factor
     * de carga. Deja el resto de parámetros al valor por defecto.
     *
     * @param numUsers Número de usuarios.
     * @param numItems Número de productos.
     * @param loadFactor Factor de carga.
     */
    public RandomDatasetLoader(int numUsers, int numItems, double loadFactor) {
        this();

        setParameterValue(ratings_numUsers, numUsers);
        setParameterValue(ratings_numItems, numItems);
        setParameterValue(ratings_loadFactor, loadFactor);
    }

    @Override
    public synchronized RatingsDataset<RatingWithTimestamp> getRatingsDataset() {
        if (rd == null) {
            long seedValue = (Long) getParameterValue(SeedHolder.SEED);
            int numUsersValue = (Integer) getParameterValue(ratings_numUsers);
            int numItemsValue = (Integer) getParameterValue(ratings_numItems);
            double loadFactorValue = ((Number) getParameterValue(ratings_loadFactor)).doubleValue();
            int minRatingValue = (Integer) getParameterValue(ratings_minRating);
            int maxRatingValue = (Integer) getParameterValue(ratings_maxRating);

            int numNumericFeaturesValue = (Integer) getParameterValue(content_numNumericFeatures);
            int numNominalFeaturesValue = (Integer) getParameterValue(content_numNominalFeatures);
            int numNumericalDifferentValuesValue = (Integer) getParameterValue(content_numNumericalDifferentValues);

            String domainType = getParameterValue(ratings_domain_type).toString();

            Domain ratingDomain;

            switch (domainType) {
                case "Decimal":
                    ratingDomain = new DecimalDomain(minRatingValue, maxRatingValue);
                    break;
                case "Integer":
                    ratingDomain = new IntegerDomain((long) minRatingValue, (long) maxRatingValue);
                    break;
                default:
                    Global.showError(new IllegalArgumentException("Cannot generate the ratings domain, '" + domainType + "' not recognised."));
                    ratingDomain = new IntegerDomain((long) minRatingValue, (long) maxRatingValue);
            }

            rd = RandomRatingsDatasetFactory.createRatingsDatasetWithLoadFactor(
                    numUsersValue, numItemsValue,
                    loadFactorValue,
                    ratingDomain,
                    seedValue);

            cd = new RandomContentDataset(rd, numNumericFeaturesValue, numNominalFeaturesValue, numNumericalDifferentValuesValue, seedValue);

            ud = new UsersDatasetAdapter(rd
                    .allUsers().stream()
                    .map((idUser -> new User(idUser)))
                    .collect(Collectors.toSet()));

        }
        return rd;
    }

    @Override
    public ContentDataset getContentDataset() {

        getRatingsDataset();

        return cd;
    }

    @Override
    public UsersDatasetAdapter getUsersDataset() {
        getRatingsDataset();
        return ud;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {

        int minRatingValue = (Integer) getParameterValue(ratings_minRating);
        int maxRatingValue = (Integer) getParameterValue(ratings_maxRating);
        if (minRatingValue == 1 && maxRatingValue == 5) {
            return new RelevanceCriteria(4);
        } else {
            return new RelevanceCriteria(maxRatingValue - 0.8 * (maxRatingValue - minRatingValue));
        }
    }

    /**
     * Devuelve el valor de la semilla.
     *
     * @return Semilla.
     */
    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }

    @Override
    public void setSeedValue(long seedValue
    ) {
        setParameterValue(SEED, seedValue);

        rd = null;
        cd = null;
    }
}
