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
package delfos.group.groupsofusers;

import delfos.common.Global;
import delfos.dataset.basic.user.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Clase abstracta que define los métodos que un objeto que representa a un grupo de usuarios tiene. Se define esta
 * "interfaz" para dar la posibilidad de que en un futuro los grupos de usuarios puedan ser más sofisticados o más
 * simples
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupOfUsers implements Comparable<GroupOfUsers>, Iterable<Long>, Serializable {

    private static final long serialVersionUID = 24352466L;
    public static Comparator<GroupOfUsers> BY_MEMBERS_ID = (GroupOfUsers o1, GroupOfUsers o2) -> {

        if (o1.size() == o2.size()) {
            List<Long> thisMembers = o1.getIdMembers().stream().sorted().collect(Collectors.toList());
            List<Long> compareMembers = o2.getIdMembers().stream().sorted().collect(Collectors.toList());

            for (int i = 0; i < thisMembers.size(); i++) {
                int compareTo = thisMembers.get(i).compareTo(compareMembers.get(i));
                if (compareTo != 0) {
                    //Son distintas.
                    return compareTo;
                }
            }
            //Son iguales
            return 0;
        } else if (o1.size() > o2.size()) {
            return 1;
        } else if (o1.size() < o2.size()) {
            return -1;
        } else {
            throw new IllegalStateException("This situation is impossible.");
        }
    };

    /**
     * Conjunto de usuarios que pertenecen al grupo
     */
    private final Set<Long> idMembers;

    private final Set<User> members;

    public GroupOfUsers() {
        idMembers = new TreeSet<>();
        members = new TreeSet<>();
    }

    @Deprecated
    public GroupOfUsers(Long... _users) {
        idMembers = new TreeSet<>(Arrays.asList(_users));
        members = idMembers.stream()
                .map(user -> new User(user))
                .collect(Collectors.toCollection(TreeSet::new));

        if (idMembers.size() > this.idMembers.size()) {
            Global.showWarning("There are repeated users in the origin collection of users");
        }
    }

    public GroupOfUsers(User... users) {
        this(Arrays.asList(users));
    }

    public GroupOfUsers(Collection<User> users) {
        members = new TreeSet<>(users);
        idMembers = users.stream().map(user -> user.getId()).collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Añade un usuario al grupo de usuarios
     *
     * @param idUser Usuario que se desea añadir al grupo
     * @return
     */
    public boolean addUser(Long idUser) {
        boolean add = idMembers.add(idUser);
        if (!add) {
            Global.showWarning("User was already a group member");
        }
        return add;
    }

    public boolean removeUser(Long idUser) {
        boolean remove = idMembers.remove((Long) idUser);
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
    public Collection<Long> getIdMembers() {
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
        return GroupOfUsers.BY_MEMBERS_ID.compare(this, o);
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
    public boolean contains(Long idUser) {
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
        Iterator<Long> i = getIdMembers().iterator();
        while (i.hasNext()) {
            Long obj = i.next();
            if (obj != null) {
                h += obj.hashCode();
            }
        }
        return h;
    }

    @Override
    public Iterator<Long> iterator() {
        return getIdMembers().iterator();
    }

    public String getTargetId() {
        return GROUP_ID_TARGET_PREFIX + groupMembersToString(getIdMembers());
    }

    public static final String GROUP_ID_TARGET_PREFIX = "Group_";

    public static GroupOfUsers parseIdTarget(String idTarget) {
        if (idTarget.startsWith(GROUP_ID_TARGET_PREFIX)) {
            String collectionToString = idTarget.substring(idTarget.indexOf(GROUP_ID_TARGET_PREFIX));
            Collection<Long> groupMembers = extractGroupMembers(collectionToString);
            return new GroupOfUsers(groupMembers.toArray(new Long[0]));
        } else {
            throw new IllegalArgumentException("Not a group idTarget '" + idTarget + "'");
        }
    }

    public static Collection<Long> extractGroupMembers(String collectionToString) {
        String collectionToStringWithoutBrackets = collectionToString
                .replaceAll("\\[", "")
                .replaceAll("\\]", "");

        if (collectionToStringWithoutBrackets.isEmpty()) {
            throw new IllegalStateException("Collection '" + collectionToString + "' cannot be empty, a group of users must have members");
        }

        String[] members = collectionToStringWithoutBrackets.split(",");

        LinkedList<Long> ret = new LinkedList<>();
        for (String member : members) {
            ret.add(Long.parseLong(member));
        }

        return ret;
    }

    public static String groupMembersToString(Collection<Long> members) {
        Iterator<Long> it = members.iterator();
        if (!it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (;;) {
            Long idUser = it.next();
            sb.append(Long.toString(idUser));
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}
