package delfos.dataset.mockdatasets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.entity.EntityAlreadyExists;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;

/**
 *
 * @author Jorge
 */
public class MockUsersDataset implements UsersDataset {

    private final Map<Integer, User> users = new TreeMap<>();
    private final FeatureGenerator featureGenerator = new FeatureGenerator();

    private void checkItemNotExists(int idUser) {
        if (users.containsKey(idUser)) {
            throw new IllegalArgumentException("The item " + idUser + " already exists.");
        }
    }

    private void checkItem(int idUser) throws UserNotFound {
        if (!users.containsKey(idUser)) {
            throw new UserNotFound(idUser);
        }
    }

    @Override
    public User getUser(int idUser) throws UserNotFound {
        try {
            return get(idUser);
        } catch (EntityNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }

    }

    @Override
    public void add(User entity) throws EntityAlreadyExists {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User get(int idUser) throws EntityNotFound {
        if (users.containsKey(idUser)) {
            return users.get(idUser);
        } else {
            throw new UserNotFound(idUser);
        }
    }

    @Override
    public Feature[] getFeatures() {
        return featureGenerator.getSortedFeatures().toArray(new Feature[0]);
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMinValue(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMaxValue(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature searchFeature(String featureName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature searchFeatureByExtendedName(String extendedName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Feature, Object> parseEntityFeatures(Map<String, String> features) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public int size() {
        return users.size();
    }

    @Override
    public Collection<Integer> getAllID() {
        return new TreeSet<>(users.keySet());
    }

    @Override
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(int idEntity, Map<String, String> features) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<User> iterator() {
        return new ArrayList<>(users.values()).iterator();
    }
}
