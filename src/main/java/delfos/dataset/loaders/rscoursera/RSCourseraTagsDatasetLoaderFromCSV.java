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
package delfos.dataset.loaders.rscoursera;

import com.csvreader.CsvReader;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.tags.DefaultTagsDataset;
import delfos.dataset.basic.tags.TagOverItem;
import delfos.dataset.basic.tags.TagsDataset;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedList;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RSCourseraTagsDatasetLoaderFromCSV {

    private final int itemColumn = 0;
    private final int tagColumn = 1;

    public TagsDataset readTagsDataset(ContentDataset contentDataset, File tagsFile) throws FileNotFoundException {

        try {

            CsvReader reader = new CsvReader(
                    new FileInputStream(tagsFile.getAbsolutePath()),
                    Charset.forName("UTF-8"));
            reader.setRecordDelimiter('\n');
            reader.setDelimiter(',');

            Collection<TagOverItem> tags = new LinkedList<>();

            while (reader.readRecord()) {

                int idItem = Integer.parseInt(reader.get(itemColumn));
                String tag = reader.get(tagColumn);

                Item item = contentDataset.get(idItem);

                tags.add(new TagOverItem(tag, item));
            }
            reader.close();
            TagsDataset tagsDataset = new DefaultTagsDataset(tags);
            return tagsDataset;
        } catch (IOException | NumberFormatException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public void writeDataset(TagsDataset usersDataset, String fileName) throws IOException {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
