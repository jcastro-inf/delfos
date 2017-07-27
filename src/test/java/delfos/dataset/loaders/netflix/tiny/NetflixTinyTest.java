package delfos.dataset.loaders.netflix.tiny;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import org.junit.Test;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class NetflixTinyTest {

    @Test
    public void test(){

        NetflixTiny dataset = new NetflixTiny();

        RatingsDataset<Rating> ratingsDataset = dataset.getRatingsDataset();

        System.out.println("Ratings: "+ratingsDataset.getNumRatings());
        System.out.println("Items:   "+ratingsDataset.allRatedItems().size());
        System.out.println("Users:   "+ratingsDataset.allUsers().size());

        System.out.println("Items without ratings: ");

        Set<Long> allItems = dataset.getContentDataset().allIDs().stream().collect(Collectors.toSet());
        Set<Long> ratedItems = dataset.ratingsDataset.allRatedItems().stream().collect(Collectors.toSet());

        Set<Long> itemsWithoutRatings  = new TreeSet<>();
        itemsWithoutRatings.addAll(allItems);
        itemsWithoutRatings.removeAll(ratedItems);

        itemsWithoutRatings.forEach(idItem -> {
                Item item = dataset.getContentDataset().get(idItem);
                System.out.println("\tItem ["+item.getId()+"] '"+item.getName()+"' has no ratings");
        });


    }

}