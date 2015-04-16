package delfos.group.groupsofusers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import delfos.common.Global;

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
    private final ArrayList<Integer> users;

    public GroupOfUsers() {
        this.users = new ArrayList<>();
    }

    public GroupOfUsers(Integer... _users) {
        this();
        users.addAll(Arrays.asList(_users));

        if (users.size() > this.users.size()) {
            Global.showWarning("There are repeated users in the origin collection of users");
        }
    }

    public GroupOfUsers(Collection<Integer> users) {
        this.users = new ArrayList<>(users);
        Collections.sort(this.users);
        if (users.size() > this.users.size()) {
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
        boolean add = users.add(idUser);
        if (!add) {
            Global.showWarning("User was already a group member");
        }
        return add;
    }

    public boolean removeUser(int idUser) {
        boolean remove = users.remove((Integer) idUser);
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
    public Collection<Integer> getGroupMembers() {
        return new ArrayList<>(users);
    }

    public int size() {
        return users.size();
    }

    @Override
    public int compareTo(GroupOfUsers o) {
        if (this.size() > o.size()) {
            return 1;
        }

        if (this.size() < o.size()) {
            return -1;
        }

        List<Integer> thisMembers = new ArrayList<>(getGroupMembers());
        List<Integer> compareMembers = new ArrayList<>(o.getGroupMembers());
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
        return groupMembersToString(getGroupMembers());
    }

    /**
     * Función para comprobar si un usuario pertenece al grupo
     *
     * @param idUser id del usuario que se desea comprobar
     * @return Devuelve true si el usuario pertenece al grupo
     */
    public boolean contains(int idUser) {
        return getGroupMembers().contains(idUser);
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
        Iterator<Integer> i = getGroupMembers().iterator();
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
        return getGroupMembers().iterator();
    }

    public String getTargetId() {
        return GROUP_ID_TARGET_PREFIX + groupMembersToString(getGroupMembers());
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
