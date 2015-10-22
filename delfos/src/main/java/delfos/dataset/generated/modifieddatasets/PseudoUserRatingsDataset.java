package delfos.dataset.generated.modifieddatasets;

import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.generated.modifieddatasets.userreductor.UserReductor_allowedUsers;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Dataset utilizado para añadir un usuario que no existe realmente.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 21-Enero-2014 Ahora también se permite eliminar usuarios, para dar
 * soporte a GRS.
 * @param <RatingType>
 */
public class PseudoUserRatingsDataset<RatingType extends Rating> extends RatingsDatasetAdapter<RatingType> {

    private final RatingsDataset<RatingType> constructionOriginalDataset;
    private RatingsDataset<RatingType> workingDataset;
    private RatingsDataset<RatingType> pseudoRatings = new BothIndexRatingsDataset<>();
    private int nextIdUser = -1;
    private Set<Integer> ratedItems = null;
    private boolean pseudoUserSetted = false;
    private int idPseudoUser;

    public PseudoUserRatingsDataset(RatingsDataset<? extends RatingType> ratingsDataset, Map<Integer, RatingType> userRatings, Collection<Integer> forbiddenUsers) {
        super();
        constructionOriginalDataset = (RatingsDataset<RatingType>) ratingsDataset;

        workingDataset = constructionOriginalDataset;

        if (pseudoUserSetted) {
            throw new IllegalStateException("The method setPseudoUserRatings can conly be called once per object instantiation.");
        }

        pseudoUserSetted = true;

        if (userRatings == null) {
            throw new IllegalArgumentException("No se puede crear un usuario sin valoraciones.");
        }

        //Obtengo el siguiente id de usuario.
        idPseudoUser = getNextIdUser();

        while (constructionOriginalDataset.allUsers().contains(idPseudoUser)) {
            idPseudoUser++;
        }

        Map<Integer, Map<Integer, RatingType>> ratingsToAdd = new TreeMap<>();
        ratingsToAdd.put(idPseudoUser, userRatings);

        Set<Integer> allowedUsers = new TreeSet<>(workingDataset.allUsers());

        for (int idUser : forbiddenUsers) {
            boolean removed = allowedUsers.remove(idUser);
            if (!removed) {
                throw new IllegalStateException("The original dataset did not contain the user.");
            }
        }

        workingDataset = new UserReductor_allowedUsers<>(workingDataset, allowedUsers);
        pseudoRatings = new BothIndexRatingsDataset<>(pseudoRatings, ratingsToAdd);

        ratedItems = null;

    }

    public PseudoUserRatingsDataset(RatingsDataset<? extends RatingType> ratingsDataset, Map<Integer, RatingType> userRatings) {
        this(ratingsDataset, userRatings, new TreeSet<>());
    }

    public int getIdPseudoUser() {
        return idPseudoUser;
    }

    @Override
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getRating(idUser, idItem);
        } else {
            return workingDataset.getRating(idUser, idItem);
        }
    }

    @Override
    public Set<Integer> allUsers() {
        Set<Integer> allUsers = new TreeSet<>(workingDataset.allUsers());
        allUsers.addAll(pseudoRatings.allUsers());
        return allUsers;
    }

    @Override
    public Set<Integer> allRatedItems() {
        if (ratedItems == null) {
            ratedItems = new TreeSet<>(workingDataset.allRatedItems());
            ratedItems.addAll(pseudoRatings.allRatedItems());
        }
        return Collections.unmodifiableSet(ratedItems);
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) throws UserNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getUserRated(idUser);
        } else {
            return workingDataset.getUserRated(idUser);
        }
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) {
        Set<Integer> ret = new TreeSet<>();
        try {
            ret.addAll(pseudoRatings.getItemRated(idItem));
        } catch (ItemNotFound ex) {
        }

        try {
            ret.addAll(workingDataset.getItemRated(idItem));
        } catch (ItemNotFound ex) {
        }

        return ret;
    }

    @Override
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound {
        if (pseudoRatings.allUsers().contains(idUser)) {
            return pseudoRatings.getUserRatingsRated(idUser);
        } else {
            return workingDataset.getUserRatingsRated(idUser);
        }
    }

    @Override
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        Map<Integer, RatingType> ret = new TreeMap<>();
        try {
            ret.putAll(pseudoRatings.getItemRatingsRated(idItem));
        } catch (ItemNotFound ex) {
        }

        try {
            ret.putAll(workingDataset.getItemRatingsRated(idItem));
        } catch (ItemNotFound ex) {
        }

        return ret;
    }

    @Override
    public Domain getRatingsDomain() {
        return workingDataset.getRatingsDomain();
    }

    private int getNextIdUser() {
        return nextIdUser--;
    }
}
