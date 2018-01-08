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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DefaultTagsDataset implements TagsDataset {

    private Collection<TagOverItem> tags;

    public DefaultTagsDataset() {
    }

    public DefaultTagsDataset(Collection<TagOverItem> tags) {
        this.tags = Collections.unmodifiableCollection(
                tags.parallelStream().collect(Collectors.toList()));
    }

    @Override
    public Collection<TagOverItem> getItemTags(Item item) {
        return tags.parallelStream()
                .filter(tagOverItem -> tagOverItem.getItem().equals(item))
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<TagOverItem> iterator() {
        return tags.iterator();
    }

    @Override
    public Stream<TagOverItem> stream() {
        return tags.stream();
    }

}
