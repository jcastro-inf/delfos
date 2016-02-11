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

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author jcastro
 */
public class RecommendationComputationDetails {

    private final Map<DetailField, Object> details;

    public enum DetailField {

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

    public static final RecommendationComputationDetails EMPTY_DETAILS = new RecommendationComputationDetails();

    public RecommendationComputationDetails() {
        details = new TreeMap<>();
    }

    public RecommendationComputationDetails(Map<DetailField, Object> details) {
        this.details = details;
    }

    public RecommendationComputationDetails addDetail(DetailField detailField, Object detailValue) {
        this.details.put(detailField, detailValue);
        return this;
    }

    public Set<DetailField> detailFieldSet() {
        return details.keySet();
    }

    public Object getDetailFieldValue(DetailField detailField) {
        if (details.containsKey(detailField)) {
            return details.get(detailField);
        } else {
            return "N/A";
        }
    }

}
