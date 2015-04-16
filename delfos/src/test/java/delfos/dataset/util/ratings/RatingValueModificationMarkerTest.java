package delfos.dataset.util.ratings;

import delfos.dataset.util.ratings.RatingValueModificationMarker;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import delfos.dataset.basic.rating.domain.DecimalDomain;

/**
 *
 * @author jcastro
 */
public class RatingValueModificationMarkerTest {

    public RatingValueModificationMarkerTest() {
    }

    /**
     * Test of getRatingModificationMarker method, of class
     * RatingValueModificationMarker.
     */
    @Test
    public void testGetRatingModificationMarkers() {
        System.out.println("getRatingModificationMarker");

        List<String> markers = RatingValueModificationMarker.generateDefaultMarkers("+", "==", "-", 4);
        RatingValueModificationMarker instance = new RatingValueModificationMarker(new DecimalDomain(1, 5), markers);

        Assert.assertEquals("El marcador es erroneo", "++++", instance.getRatingModificationMarker(1, 5));
        Assert.assertEquals("El marcador es erroneo", "+++", instance.getRatingModificationMarker(1, 4));
        Assert.assertEquals("El marcador es erroneo", "++", instance.getRatingModificationMarker(1, 3));
        Assert.assertEquals("El marcador es erroneo", "+", instance.getRatingModificationMarker(1, 2));
        Assert.assertEquals("El marcador es erroneo", "==", instance.getRatingModificationMarker(1, 1));
        Assert.assertEquals("El marcador es erroneo", "-", instance.getRatingModificationMarker(2, 1));
        Assert.assertEquals("El marcador es erroneo", "--", instance.getRatingModificationMarker(3, 1));
        Assert.assertEquals("El marcador es erroneo", "---", instance.getRatingModificationMarker(4, 1));
        Assert.assertEquals("El marcador es erroneo", "----", instance.getRatingModificationMarker(5, 1));
    }

}
