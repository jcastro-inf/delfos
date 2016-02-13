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
package delfos.group.experiment.validation.predictionvalidation;

import java.util.Collection;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.experiment.SeedHolder;

/**
 * Clase utilizada para establecer los métodos que debe implementar un protocolo
 * de predicción para sistemas de recomendación a grupos. Para utilizar el
 * protocolo, se solicita que se calcule qué productos se deben predecir a
 * partir de las valoraciones conocidas de los miembros del mismo.
 *
 * Por ejemplo, tenemos un grupo G = {u1,u2,u3} cuyos miembros tienen las
 * siguientes valoraciones: u1 = {i1:5 i2:3 i3:- i4:3}
 * <p>
 * u2 = {i1:4 i2:- i3:5 i4:4}
 * <p>
 * u3 = {i1:3 i2:4 i3:5 i4:-}
 * <p>
 *
 * Una posible técnica de validación podría determinar que se predigan los
 * productos i1 e i2 a partir de i3 e i4
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (10/12/2012)
 */
public abstract class GroupPredictionProtocol extends ParameterOwnerAdapter implements SeedHolder {

    /**
     * Establece los parámetros comunes a todos los protocolos de predicción
     * para recomendación a grupos de usuarios.
     */
    public GroupPredictionProtocol() {
        super();

        init();
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {

        return (Long) getParameterValue(SEED);
    }

    /**
     * Realiza las inicializaciones pertinentes: Inicializa la secuencia
     * aleatoria y añade un listener que la resetee cuando cambie la semilla.
     */
    private void init() {
        addParameter(SEED);

        addParammeterListener(new ParameterListener() {
            private long valorAnterior
                    = (Long) getParameterValue(SEED);

            @Override
            public void parameterChanged() {
                long newValue = (Long) getParameterValue(SEED);
                if (valorAnterior != newValue) {
                    if (Global.isVerboseAnnoying()) {
                        Global.showWarning("Reset " + getName() + " to seed = " + newValue + "\n");
                    }
                    valorAnterior = newValue;
                    setSeedValue(newValue);
                }
            }
        });
    }

    /**
     * Solicita a la técnica de validación que calcule los productos que se
     * solicitarán al sistema de recomendación a grupos que prediga. Para ello,
     * calcula la lista que define cuántas peticiones de recomendación se
     * realizarán al sistema de recomendación y qué items debe predecir en cada
     * una de las mismas.
     *
     * La lista devuelta se interpreta de la siguiente manera. La lista externa
     * representa las solicitudes de recomendación que se debe hacer y la lista
     * interna almacena los items que se pueden recomendar en cada solicitud.
     * Por ejemplo, si el valor devuelto es: <code>{{1,2},{1},{2}}</code> se
     * deberán realizar las llamadas: <code>recommend(group,{1,2});</code>
     * <code>recommend(group,{1});</code> <code>recommend(group,{2});</code>
     * Este mecanismo se utiliza para la validación {@link GivenN} y
     * {@link AllButOne}.
     *
     * NOTA: Antes de calcular las recomendaciones hay que eliminar las
     * valoraciones que se van a predecir del dataset. De esta manera se asegura
     * que no se utilizan en el proceso de predicción.
     *
     * @param trainDatasetLoader
     * @param testDatasetLoader
     *
     * @param group Grupo para el que se calcula la lista de peticiones.
     * @return Lista que define cuántas peticiones y con qué items se debe
     * solicitar al sistema de recomendación colaborativo que realice
     * recomendaciones para su validación.
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     *
     * @see RemoveRatingsDatasets
     */
    public abstract Collection<GroupRecommendationRequest> getGroupRecommendationRequests(
            DatasetLoader<? extends Rating> trainDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader, GroupOfUsers group)
            throws CannotLoadRatingsDataset, UserNotFound;

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_PREDICTION_PROTOCOL;
    }

}
