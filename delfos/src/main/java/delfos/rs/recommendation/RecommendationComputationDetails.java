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
