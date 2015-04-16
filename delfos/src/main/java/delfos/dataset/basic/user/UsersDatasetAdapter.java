package delfos.dataset.basic.user;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeaturesDefault;
import delfos.dataset.basic.features.Feature;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserAlreadyExists;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 24-jul-2013
 */
public class UsersDatasetAdapter extends CollectionOfEntitiesWithFeaturesDefault<User> implements UsersDataset {

    public UsersDatasetAdapter() {
    }

    public UsersDatasetAdapter(Iterable<User> userCollection) throws UserAlreadyExists {
        for (User user : userCollection) {
            add(user);
        }
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
        Set<String> _entitiesById = new TreeSet<String>();
        for (User user : this) {
            _entitiesById.add(user.getName() + " (User " + user.getId() + ")");
        }
        return _entitiesById.toString();
    }
}
