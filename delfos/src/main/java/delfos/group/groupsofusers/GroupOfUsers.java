package delfos.group.groupsofusers;

import delfos.common.Global;
import delfos.dataset.basic.user.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Clase abstracta que define los métodos que un objeto que representa a un
 * grupo de usuarios tiene. Se define esta "interfaz" para dar la posibilidad de
 * que en un futuro los grupos de usuarios puedan ser más sofisticados o más
 * simples
 *
 * @author Jorge Castro Gallardo
 */
public class GroupOfUsers implements Comparable<GroupOfUsers>, Iterable<Integer> {

    /**
     * Conjunto de usuarios que pertenecen al grupo
     */
    private final Set<Integer> idMembers;

    private final Set<User> members;

    public GroupOfUsers() {
        this.idMembers = new TreeSet<>();
        this.members = new TreeSet<>();
    }

    @Deprecated
    public GroupOfUsers(Integer... _users) {
        this();
        idMembers.addAll(Arrays.asList(_users));

        if (idMembers.size() > this.idMembers.size()) {
            Global.showWarning("There are repeated users in the origin collection of users");
        }
    }

    public GroupOfUsers(Set<User> users) {
        this.members = new TreeSet<>(users);
        this.idMembers = users.stream().map(user -> user.getId()).collect(Collectors.toCollection(TreeSet::new));
    }

    public GroupOfUsers(Collection<Integer> users) {
        this.idMembers = new TreeSet<>(users);
        this.members = users.stream().map(user -> new User(user)).collect(Collectors.toSet());

        if (users.size() > this.idMembers.size()) {
            Global.showWarning("There are repeated users in the origin collection of users");
        }
    }

    /**
     * Añade un usuario al grupo de usuarios
     *
     * @param idUser Usuario que se desea añadir al grupo
     * @return
     */
    public boolean addUser(int idUser) {
        boolean add = idMembers.add(idUser);
        if (!add) {
            Global.showWarning("User was already a group member");
        }
        return add;
    }

    public boolean removeUser(int idUser) {
        boolean remove = idMembers.remove((Integer) idUser);
        if (!remove) {
            Global.showWarning("User not in group");
        }
        return remove;
    }

    /**
     * Devuelve el conjunto de usuarios que son miembros de este grupo
     *
     * @return conjunto de miembros
     */
    public Collection<Integer> getIdMembers() {
        return new ArrayList<>(idMembers);
    }

    public Set<User> getMembers() {
        return members;
    }

    public int size() {
        return idMembers.size();
    }

    @Override
    public int compareTo(GroupOfUsers o) {
        if (this.size() > o.size()) {
            return 1;
        }

        if (this.size() < o.size()) {
            return -1;
        }

        List<Integer> thisMembers = new ArrayList<>(getIdMembers());
        List<Integer> compareMembers = new ArrayList<>(o.getIdMembers());
        Collections.sort(thisMembers);
        Collections.sort(compareMembers);

        for (int i = 0; i < thisMembers.size(); i++) {
            int compareTo = thisMembers.get(i).compareTo(compareMembers.get(i));

            if (compareTo != 0) {
                //Son distintas.
                return compareTo;
            }
        }

        //Son iguales
        return 0;
    }

    @Override
    public String toString() {
        return groupMembersToString(getIdMembers());
    }

    /**
     * Función para comprobar si un usuario pertenece al grupo
     *
     * @param idUser id del usuario que se desea comprobar
     * @return Devuelve true si el usuario pertenece al grupo
     */
    public boolean contains(int idUser) {
        return getIdMembers().contains(idUser);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof GroupOfUsers) {
            GroupOfUsers groupOfUsers = (GroupOfUsers) obj;
            return this.compareTo(groupOfUsers) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        Iterator<Integer> i = getIdMembers().iterator();
        while (i.hasNext()) {
            Integer obj = i.next();
            if (obj != null) {
                h += obj.hashCode();
            }
        }
        return h;
    }

    @Override
    public Iterator<Integer> iterator() {
        return getIdMembers().iterator();
    }

    public String getTargetId() {
        return GROUP_ID_TARGET_PREFIX + groupMembersToString(getIdMembers());
    }

    public static final String GROUP_ID_TARGET_PREFIX = "Group_";

    public static GroupOfUsers parseIdTarget(String idTarget) {
        if (idTarget.startsWith(GROUP_ID_TARGET_PREFIX)) {
            String collectionToString = idTarget.substring(idTarget.indexOf(GROUP_ID_TARGET_PREFIX));
            Collection<Integer> groupMembers = extractGroupMembers(collectionToString);
            return new GroupOfUsers(groupMembers);
        } else {
            throw new IllegalArgumentException("Not a group idTarget '" + idTarget + "'");
        }
    }

    public static Collection<Integer> extractGroupMembers(String collectionToString) {
        String collectionToStringWithoutBrackets = collectionToString
                .replaceAll("\\[", "")
                .replaceAll("\\]", "");

        if (collectionToStringWithoutBrackets.isEmpty()) {
            throw new IllegalStateException("Collection '" + collectionToString + "' cannot be empty, a group of users must have members");
        }

        String[] members = collectionToStringWithoutBrackets.split(",");

        LinkedList<Integer> ret = new LinkedList<>();
        for (String member : members) {
            ret.add(Integer.parseInt(member));
        }

        return ret;
    }

    public static String groupMembersToString(Collection<Integer> members) {
        Iterator<Integer> it = members.iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            Integer idUser = it.next();
            sb.append(Integer.toString(idUser));
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }
}
