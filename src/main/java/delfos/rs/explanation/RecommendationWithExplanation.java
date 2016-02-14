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
package delfos.rs.explanation;

import delfos.rs.recommendation.Recommendation;
import java.util.Collection;

/**
 *
 * @version 09-sep-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <ExplanationType>
 */
public class RecommendationWithExplanation<ExplanationType> {

    private final Collection<Recommendation> recommendations;
    private final ExplanationType explanation;

    public RecommendationWithExplanation(Collection<Recommendation> recommendations, ExplanationType explanation) {
        this.recommendations = recommendations;
        this.explanation = explanation;
    }
}
