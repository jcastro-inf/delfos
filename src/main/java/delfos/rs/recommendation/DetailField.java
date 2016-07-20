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
package delfos.rs.recommendation;

import java.io.Serializable;

/**
 *
 * @author jcastro
 */
public enum DetailField implements Serializable {

    TimeTaken;

    public static DetailField valueOfNoCase(String name) {
        for (DetailField detailField : values()) {
            if (detailField.name().equalsIgnoreCase(name)) {
                return detailField;
            }
        }

        throw new IllegalStateException("No DetailField with identifier '" + name + "'");
    }

    public Object parseValue(String detailFieldValueString) {
        switch (this) {
            case TimeTaken:
                return Long.parseLong(detailFieldValueString);
            default:
                throw new IllegalStateException("Unknown DetaildField '" + this + "'");
        }
    }
}
