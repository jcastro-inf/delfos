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
package delfos.experiment.validation.predictionprotocol;

import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.experiment.SeedHolder;
import delfos.rs.RecommenderSystemAdapter;
import java.util.List;
import java.util.Set;

/**
 * Interfaz que especifica los métodos que deberá tener una validación de predicciones. Esta clase se encarga de separar
 * los ratings que conoce el sistema de recomendación al realizar la predicción de los que no. Sólo se usan las
 * {@link PredictionProtocol} en algoritmos de filtrado colaborativo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (19 Octubre 2011)
 * @version 1.1 08-Mar-2013 Ahora implementa {@link SeedHolder}, para controlar la generación de datos aleatorios.
 */
public abstract class PredictionProtocol extends ParameterOwnerAdapter implements SeedHolder {

    /**
     * Crea un protocolo de predicción con los valores dados por defecto.
     */
    public PredictionProtocol() {
        super();

        addParameter(SEED);

        init();
    }

    /**
     * Solicita a la técnica de validación que prepare los datasets para realizar una solicitud de recomendaciones al
     * usuario <code>idUser</code> en la partición <code>split</code>
     *
     * La lista devuelta se interpreta de la siguiente manera. La lista externa representa las solicitudes de
     * recomendación que se debe hacer y la lista interna almacena los items que se pueden recomendar en cada solicitud.
     * Por ejemplo, si el valor devuelto es: <code>{{1,2},{1},{2}}</code> se deberán realizar las llamadas:
     * <code>recommendOnly(idUser,{1,2});</code> <code>recommendOnly(idUser,{1});</code>
     * <code>recommendOnly(idUser,{2});</code> Este mecanismo se utiliza para la validación {@link GivenN} y
     * {@link AllButOne}.
     *
     * NOTA: Antes de cada {@link RecommenderSystemAdapter#recommendOnly(java.lang.Integer, java.util.Collection) }
     * hay que eliminar las valoraciones que se van a predecir
     *
     * Calcula la lista que define cuántas peticiones de recomendación se realizarán al sistema de recomendación y qué
     * items debe predecir en cada una de las mismas. Para ello se devuelve una lista de listas de idItems
     *
     * @param testRatingsDataset
     * @param idUser Usuario para el que se calcula la lista de peticiones
     * @return Lista que define cuántas peticiones y con qué items se debe solicitar al sistema de recomendación
     * colaborativo que realice recomendaciones para su validación
     * @throws UserNotFound Si el usuario <code>idUser<\code> no se encuentra en el dataset original.
     */
    public abstract List<Set<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound;

    public List<Set<Integer>> getRatingsToHide(
            RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        return getRecommendationRequests(testRatingsDataset, idUser);
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
     * Realiza las inicializaciones de la instancia.
     */
    private void init() {
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

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.PREDICTION_PROTOCOL_TECHNIQUE;
    }

}
