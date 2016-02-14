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
package delfos.group.grs.preferenceaggregation;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Clase que encapsula la transformación de perfil basado en preferencias
 * numéricas a perfil basado en relaciones de preferencia. 
 * 
 * Por ejemplo, si un usuario tiene las siguientes valoraciones: 
 * 
 * u1 = [i1->5, i2->3 i3->4] => P_u1 = [i1 i2,i1 i3,i3 i1]
 * 
 * Tiene dos modos de funcionamiento, con 'bipolaridad' y sin ella. Con 
 * bipolaridad, si dos productos tienen la misma valoración se introducen las 
 * dos preferencias (u1=[i1->5, i2->3
 * 
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PartialOrder_userProfile {
    
    /**
     * Almacena las preferencias parciales del usuario
     */
    private Set<PreferenceOrder<Integer>> partialOrderPreferences = new TreeSet<PreferenceOrder<Integer>>();

    /**
     * Genera un perfil de usuario basado en el orden parcial a partir de las
     * valoraciones numéricas que el usuario ha dado sobre los productos
     * @param numericalPreferences Mapa cuya clave es el id del producto y el 
     * valor es la valoración numérica que el usuario le ha otorgado
     */
    public PartialOrder_userProfile(Map<Integer,Number> numericalPreferences, boolean bipolar) {
        
        Entry<Integer, Number>[] array = numericalPreferences.keySet().toArray(new Entry[0]);
        
        for(int i=0;i<array.length;i++){
            for(int j=i+1;j<array.length;j++){
                int idItem1 = array[i].getKey();
                double rating1 = array[i].getValue().doubleValue();
                int idItem2 = array[j].getKey();
                double rating2 = array[j].getValue().doubleValue();
                
                if(rating1 > rating2){
                    partialOrderPreferences.add(new PreferenceOrder<Integer>(idItem1,idItem2));
                }else{
                    if(rating1 < rating2){
                        partialOrderPreferences.add(new PreferenceOrder<Integer>(idItem2,idItem1));
                    }else{
                        if(bipolar){
                            partialOrderPreferences.add(new PreferenceOrder<Integer>(idItem1,idItem2));
                            partialOrderPreferences.add(new PreferenceOrder<Integer>(idItem2,idItem1));
                        }
                    }
                }
            }
        }
    }
    
    public PreferenceOrder<Integer> getTotalOrder(){
        
        
        
        
        
        return null;
    }
}
