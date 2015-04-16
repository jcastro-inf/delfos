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
public class RandomRecommenderModel<Key> implements Serializable {

    private static final long serialVersionUID = 107L;
    private final long seed;
    private final float minRating;
    private final float maxRating;
    private final Map<Key, Random> generadores;

    private RandomRecommenderModel() {
        seed = 0;
        this.minRating = 1;
        this.maxRating = 5;
        generadores = Collections.synchronizedMap(new TreeMap<Key, Random>());
    }

    public RandomRecommenderModel(long seed, Number minRating, Number maxRating) {
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
