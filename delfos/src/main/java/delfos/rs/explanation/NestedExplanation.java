package delfos.rs.explanation;

/**
 *
 * @version 09-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @param <ExplanationType>
 */
public class NestedExplanation<ExplanationType> {

    private final ExplanationType myExplanation;
    private final Object nestedExplanation;

    public NestedExplanation(ExplanationType myExplanation, Object nestedExplanation) {
        this.myExplanation = myExplanation;
        this.nestedExplanation = nestedExplanation;
    }

    public Object getNestedExplanation() {
        return nestedExplanation;
    }

    public ExplanationType getMyExplanation() {
        return myExplanation;
    }
}
