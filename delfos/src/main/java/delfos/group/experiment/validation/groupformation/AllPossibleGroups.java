package delfos.group.experiment.validation.groupformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Devuelve todos los grupos posibles del dataset. Si el dataset tiene más de 10
 * usuarios, no se puede aplicar esta validación.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 24-May-2013
 */
public class AllPossibleGroups extends GroupFormationTechnique {

    @Override
    public Collection<GroupOfUsers> shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {

        Collection<Integer> allUsers = datasetLoader.getRatingsDataset().allUsers();
        if (allUsers.size() > 20) {
            throw new IllegalArgumentException("The number of users cannot be greater than 20.");
        }

        double numberOfDifferentGroups = Math.pow(2, allUsers.size());
        Set<GroupOfUsers> groupsSet = new HashSet<GroupOfUsers>((int) numberOfDifferentGroups);

        for (int idUser : allUsers) {
            groupsSet.add(new GroupOfUsers(idUser));
        }

        for (int idUser : allUsers) {
            Set<GroupOfUsers> gruposAAñadir = new TreeSet<GroupOfUsers>(groupsSet);

            for (GroupOfUsers groupOfUsers : gruposAAñadir) {
                Set<Integer> members = new TreeSet<Integer>(groupOfUsers.getGroupMembers());

                members.add(idUser);
                groupsSet.add(new GroupOfUsers(members.toArray(new Integer[0])));

            }
        }

        return new ArrayList<GroupOfUsers>(groupsSet);
    }
}
