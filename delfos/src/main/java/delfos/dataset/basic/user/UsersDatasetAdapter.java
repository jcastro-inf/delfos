package delfos.dataset.basic.user;

import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeaturesDefault;
import delfos.dataset.basic.features.Feature;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 24-jul-2013
 */
public class UsersDatasetAdapter extends CollectionOfEntitiesWithFeaturesDefault<User> implements UsersDataset {

    public UsersDatasetAdapter() {
    }

    public UsersDatasetAdapter(Set<User> userCollection) {
        userCollection.stream().forEach((user) -> add(user));
    }

    @Override
    public User getUser(int idUser) throws UserNotFound {
        if (entitiesById.containsKey(idUser)) {
            return entitiesById.get(idUser);
        } else {
            throw new UserNotFound(idUser);
        }
    }

    @Override
    public Feature[] getFeatures() {
        return featureGenerator.getSortedFeatures().toArray(new Feature[0]);
    }

    @Override
    public Iterator<User> iterator() {
        return entitiesById.values().iterator();
    }

    @Override
    public User get(int idUser) throws EntityNotFound {
        if (entitiesById.containsKey(idUser)) {
            return entitiesById.get(idUser);
        } else {
            throw new EntityNotFound(User.class, idUser);
        }
    }

    @Override
    public String toString() {
        Set<String> _entitiesById = new TreeSet<>();
        for (User user : this) {
            _entitiesById.add(user.getName() + " (User " + user.getId() + ")");
        }
        return _entitiesById.toString();
    }
}
