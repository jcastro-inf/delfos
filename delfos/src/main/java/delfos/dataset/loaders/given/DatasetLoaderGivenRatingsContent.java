package delfos.dataset.loaders.given;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoaderAbstract;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 26-nov-2013
 * @param <RatingType>
 */
public class DatasetLoaderGivenRatingsContent<RatingType extends Rating> extends DatasetLoaderAbstract<RatingType> implements ContentDatasetLoader {

    /**
     * Dataset de contenido generado aleatoriamente;
     */
    private final ContentDataset contentDataset;
    /**
     * Dataset aleatorio que se utiliza en los test.
     */
    private final RatingsDataset<RatingType> ratingsDataset;
    private static final long serialVersionUID = 1L;

    public DatasetLoaderGivenRatingsContent(RatingsDataset<RatingType> ratingsDataset, ContentDataset contentDataset) {
        this.ratingsDataset = ratingsDataset;
        this.contentDataset = contentDataset;
    }

    @Override
    public RatingsDataset<RatingType> getRatingsDataset() throws CannotLoadRatingsDataset {
        return ratingsDataset;
    }

    @Override
    public ContentDataset getContentDataset() throws CannotLoadContentDataset {
        return contentDataset;
    }

    @Override
    public RelevanceCriteria getDefaultRelevanceCriteria() {
        return new RelevanceCriteria(4);
    }
}
