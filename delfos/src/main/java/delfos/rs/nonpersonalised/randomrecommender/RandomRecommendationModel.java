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
package delfos.rs.nonpersonalised.randomrecommender;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

/**
 * Modelo para el sistema de recomendación aleatorio. Almacena un generador
 * aleatorio para cada usuario, combinando la semilla indicada al algoritmo y su
 * id de usuario.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2).
 *
 * @version 1.0 30-Julio-2013
 * @param <Key> Tipo de clave para cada usuario.
 */
public class RandomRecommendationModel<Key> implements Serializable {

    private static final long serialVersionUID = 107L;
    private final long seed;
    private final float minRating;
    private final float maxRating;
    private final Map<Key, Random> generadores;

    private RandomRecommendationModel() {
        seed = 0;
        this.minRating = 1;
        this.maxRating = 5;
        generadores = Collections.synchronizedMap(new TreeMap<Key, Random>());
    }

    public RandomRecommendationModel(long seed, Number minRating, Number maxRating) {
        this.seed = seed;
        this.minRating = minRating.floatValue();
        this.maxRating = maxRating.floatValue();
        generadores = Collections.synchronizedMap(new TreeMap<Key, Random>());
    }

    public float predict(Key key, int idItem) {

        float prediction = getRandomFloat(key) * (maxRating - minRating) + minRating;
        return prediction;
    }

    public float getRandomFloat(Key key) {
        synchronized (generadores) {
            if (!generadores.containsKey(key)) {
                long thisRecommendationSeed = seed + key.hashCode();
                Random randomGenerator = new Random(thisRecommendationSeed);
                generadores.put(key, randomGenerator);
            }
        }
        return generadores.get(key).nextFloat();

    }

    public int getRandomInt(Key key, int n) {
        synchronized (generadores) {
            if (!generadores.containsKey(key)) {
                long thisRecommendationSeed = seed + key.hashCode();
                generadores.put(key, new Random(thisRecommendationSeed));
            }
        }
        return generadores.get(key).nextInt(n);
    }
}
