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
package delfos.factories;

import delfos.rs.output.RecommendationsOutputDatabase;
import delfos.rs.output.RecommendationsOutputFileXML;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.RecommendationsOutputStandardXML;

/**
 * Factoría que conoce los métodos de escritura de recomendaciones
 * ({@link RecommendationsOutputMethod}).
 *
* @author Jorge Castro Gallardo
 *
 * @version 28-oct-2013
 */
public class RecommendationsOutputMethodFactory extends Factory<RecommendationsOutputMethod> {

    private static final RecommendationsOutputMethodFactory instance;

    static {
        instance = new RecommendationsOutputMethodFactory();
        instance.addClass(RecommendationsOutputDatabase.class);
        instance.addClass(RecommendationsOutputFileXML.class);
        instance.addClass(RecommendationsOutputStandardRaw.class);
        instance.addClass(RecommendationsOutputStandardXML.class);
    }

    private RecommendationsOutputMethodFactory() {
    }

    public static RecommendationsOutputMethodFactory getInstance() {
        return instance;
    }

}
