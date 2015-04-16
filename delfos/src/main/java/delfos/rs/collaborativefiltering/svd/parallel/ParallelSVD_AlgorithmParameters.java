package delfos.rs.collaborativefiltering.svd.parallel;

/**
 *
 * @version 24-jul-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class ParallelSVD_AlgorithmParameters {

    protected double lrate;
    protected Integer numFeatures;
    protected Integer numIterationsPerFeature;
    protected Boolean smartInit;
    protected float maxInitialisation;
    protected float minInitialisation;
    protected double kVvalue;

    public ParallelSVD_AlgorithmParameters() {
    }

}
