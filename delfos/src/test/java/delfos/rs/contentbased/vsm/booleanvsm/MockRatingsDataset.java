package delfos.rs.contentbased.vsm.booleanvsm;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Set;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 09-oct-2013
 */
class MockRatingsDataset implements RatingsDataset<Rating> {

    @Override
    public Rating getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Integer> allUsers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Integer> allRatedItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Rating> getUserRatingsRated(Integer idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Integer, Rating> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getMeanRatingItem(int idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getMeanRatingUser(int idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Domain getRatingsDomain() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getNumRatings() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int sizeOfUserRatings(int idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int sizeOfItemRatings(int idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isRatedUser(int idUser) throws UserNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isRatedItem(int idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public float getMeanRating() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<Rating> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
