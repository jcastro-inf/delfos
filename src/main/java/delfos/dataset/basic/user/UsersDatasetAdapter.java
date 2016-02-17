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
package delfos.dataset.basic.user;

import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeaturesDefault;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.rating.Rating;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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

    private Integer hashCodeBuffer = null;

    @Override
    public int hashCode() {
        if (hashCodeBuffer == null) {
            hashCodeBuffer = hashCode(this);
        }
        return hashCodeBuffer;
    }

    public static <RatingType extends Rating> int hashCode(UsersDataset usersDataset) {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(37, 11);

        List<Integer> usersSorted = usersDataset.allIDs().stream().collect(Collectors.toList());
        usersSorted.sort((i1, i2) -> Integer.compare(i1, i2));

        Feature[] features = usersDataset.getFeatures();

        for (int idUser : usersSorted) {
            User user = usersDataset.get(idUser);

            hashCodeBuilder.append(idUser);
            hashCodeBuilder.append(user.getName());
            for (Feature feature : features) {
                if (user.getFeatures().contains(feature)) {
                    Object featureValue = user.getFeatureValue(feature);
                    hashCodeBuilder.append(feature.getName());
                    hashCodeBuilder.append(feature.getType());
                    hashCodeBuilder.append(featureValue);
                }
            }
        }

        return hashCodeBuilder.hashCode();
    }

}
