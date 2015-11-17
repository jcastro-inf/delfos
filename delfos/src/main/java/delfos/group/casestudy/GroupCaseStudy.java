package delfos.group.casestudy;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.ExperimentAdapter;
import delfos.experiment.casestudy.CaseStudyParameterChangedListener;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

/**
 * Clase que encapsula el funcionamiento general de un caso de estudio dedicado
 * a realizar una experimentación con sistemas de recomendación a grupos
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unkown date
 * @version 1.1 14-Feb-2013 Adición de las validaciones de sistemas de
 * recomendación a grupos.
 */
public abstract class GroupCaseStudy extends ExperimentAdapter {

    protected final LinkedList<CaseStudyParameterChangedListener> propertyListeners = new LinkedList<>();

    public GroupCaseStudy() {
        addParameter(SEED);
    }

    /**
     * Devuelve el tiempo de ejecución en milisegundos de una partición concreta
     * de una ejecución dada
     *
     * @param execution Ejecución para la que se quiere conocer el tiempo
     * @param split
     * @return tiempo de ejecución en milisegundos
     */
    public abstract long getBuildTime(int execution, int split);

    /**
     * Devuelve las medidas de evaluación que se aplican a los resultados de
     * ejecución del sistema de recomendación a grupos
     *
     * @return Medidas de evaluación aplicadas
     */
    public abstract Collection<GroupEvaluationMeasure> getEvaluationMeasures();

    /**
     * Devuelve el DatasetLoader<? extends Rating> que el experimento utiliza
     *
     * @return DatasetLoader<? extends Rating> que el experimento utiliza
     */
    public abstract DatasetLoader<? extends Rating> getDatasetLoader();

    /**
     * Obtiene el criterio de relevancia utilizado en el caso de estudio.
     *
     * @return Criterio de relevancia actual.
     */
    public abstract RelevanceCriteria getRelevanceCriteria();

    //TODO: Poner una función que
    /**
     * Método para asignar al caso de estudio el resultado calculado de una
     * medida de evaluación sobre una ejecución.
     *
     * @param ejecucion Ejecución a la que se refiere el resultado.
     * @param split
     * @param e Medida de evaluación
     * @param groupMeasureResult Resultado de la medida
     * @deprecated Está previsto eliminar este método.
     */
    public abstract void putResult(int ejecucion, int split, GroupEvaluationMeasure e, GroupMeasureResult groupMeasureResult);

    /**
     * Método que realiza la ejecución del caso de estudio, con la configuración
     * que se haya
     *
     * @throws CannotLoadContentDataset
     * @throws CannotLoadRatingsDataset
     * @throws UserNotFound
     * @throws ItemNotFound
     */
    public abstract void execute() throws CannotLoadContentDataset, CannotLoadRatingsDataset, UserNotFound, ItemNotFound;

    /**
     * Obtiene el sistema de recomendación a grupos que se está utilizando.
     *
     * @return Sistema de recomendación a grupos que se utiliza.
     */
    public abstract GroupRecommenderSystem<Object, Object> getGroupRecommenderSystem();

    /**
     * Devuelve el resultado agregado de la medida de evaluación de GRS que se
     * indica por parámetro.
     *
     * @param em Medida de evaluación que se consulta.
     * @return Objeto que encapsula los resultados de la aplicación de la medida
     * a las recomendaciones del sistema.
     */
    public abstract GroupMeasureResult getAggregateMeasureResult(GroupEvaluationMeasure em);

    /**
     * Devuelve el resultado de la medida de evaluación de GRS que se indica por
     * parámetro en la ejecución indicada.
     *
     * @param em Medida de evaluación que se consulta.
     * @param numExec Ejecución para la que se consulta el resultado de la
     * medida.
     * @param split
     * @return Objeto que encapsula los resultados de la aplicación de la medida
     * a las recomendaciones del sistema.
     */
    public abstract GroupMeasureResult getMeasureResult(GroupEvaluationMeasure em, int numExec, int split);

    /**
     * Devuelve la técnica utilizada para generar los grupos que se evaluarán en
     * el caso de estudio.
     *
     * @return Técnica de formación de grupos utilizada
     */
    public abstract GroupFormationTechnique getGroupFormationTechnique();

    /**
     * Devuelve la validación que se utiliza en este caso de estudio.
     *
     * @return
     */
    public abstract GroupValidationTechnique getGroupValidationTechnique();

    /**
     * Obtiene el número de ejecuciones que se realizan
     *
     * @return Número de ejecuciones.
     */
    public abstract int getNumExecutions();

    /**
     * Devuelve el tiempo de recomendación que el algoritmo demoró en la
     * ejecución indicada. Engloba el cálculo de las recomendaciones para todos
     * los datos del conjunto de test.
     *
     * @param ex Ejecución para la que se consulta el tiempo
     * @param split
     * @return Tiempo en milisegundos de la fase de recomendación
     * @throws IllegalArgumentException Si ex es mayor o igual que el número de
     * ejecuciones o es menor que cero.
     *
     */
    public abstract long getRecommendationTime(int ex, int split);

    /**
     * Devuelve el tiempo empleado en la construcción del modelo de los grupos
     * ya formados.
     *
     * @param ex
     * @param split
     * @return tiempo en milisegundos.
     */
    public abstract long getGroupBuildTime(int ex, int split);

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_CASE_STUDY;
    }

    public abstract GroupPredictionProtocol getGroupPredictionProtocol();

    /**
     * Devuelve el tiempo medio de construcción del modelo de recomendación en
     * milisegundos.
     *
     * @return
     */
    public abstract long getAggregateBuildTime();

    /**
     * Devuelve el tiempo medio de construcción de todos los perfiles de grupo
     * en milisegundos.
     *
     * @return
     */
    public abstract long getAggregateGroupBuildTime();

    /**
     * Devuelve el tiempo medio de cálculo de todas las recomendaciones en
     * milisegundos.
     *
     * @return
     */
    public abstract long getAggregateRecommendationTime();

    public int hashCodeWithoutGroupRecommenderSystem() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.getSeedValue());
        hash = 97 * hash + Objects.hashCode(this.getDatasetLoader());
        hash = 97 * hash + Objects.hashCode(this.getGroupFormationTechnique());
        hash = 97 * hash + Objects.hashCode(this.getRelevanceCriteria());
        hash = 97 * hash + Objects.hashCode(this.getGroupPredictionProtocol());
        hash = 97 * hash + Objects.hashCode(this.getGroupValidationTechnique());
        return hash;
    }

    public int hashCodeOfTheRecommenderSystem() {
        return this.getGroupRecommenderSystem().hashCode();
    }
}
