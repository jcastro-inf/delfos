/*
 * Copyright (C) 2017 jcastro
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
package delfos.dataset.basic.tags;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.user.User;
import java.io.Serializable;
import java.util.Comparator;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class TagOverItem implements Serializable, Comparable<TagOverItem> {

    public static final Comparator<TagOverItem> DEFAULT = (o1, o2) -> {
        int compare = Item.BY_ID.compare(o1.getItem(), o2.item);
        if (compare != 0) {
            return compare;
        } else {
            return o1.tag.compareTo(o2.tag);
        }
    };

    private String tag;
    private Item item;
    private User user;

    public TagOverItem() {
    }

    public TagOverItem(String tag, Item item) {
        this.tag = tag;
        this.item = item;
        this.user = User.ANONYMOUS_USER;
    }

    public TagOverItem(String tag, Item item, User user) {
        this.tag = tag;
        this.item = item;
        this.user = user;
    }

    public Item getItem() {
        return item;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public int compareTo(TagOverItem o) {
        return DEFAULT.compare(this, o);
    }
}
