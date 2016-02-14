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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements the algorithm to compute common sequences of 2 total orders over a
 * set of objects. The total orders may not contain every possible object, so
 * Intersection of both total orders may be empty.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (01/12/2012)
 */
public class CommonSequences {

    /**
     * Obtiene las secuencias comunes tal y como se explica en el paper del
     * EUROFUSE2007 de Jun Liu, Zhiwei Li y Hui Wang
     *
     * @param alpha Orden total de alternativas 1.
     * @param beta Orden total de alternativas 2.
     * @return Colección de subsecuencias que son comunes a alpha y beta.
     */
    public static Collection<PreferenceOrder> getCommonSequencesEfficient(PreferenceOrder alpha, PreferenceOrder beta) {
        //Global.showMessage("COMPARING '"+alpha+"' and '"+beta+"'");

        //El algoritmo del paper está mal, implementarlo a mi manera
        List<PreferenceOrder> S;

        List<List<PreferenceOrder>> L = new LinkedList();

        for (int i = 0; i < Math.max(alpha.size(), beta.size()); i++) {
            L.add(new LinkedList<PreferenceOrder>());
        }

        //Global.showMessage("");
        //Global.showMessage("");
        //Global.showMessage("=================================================");
        int indexS = 0;
        int i = 0;

        //Global.showMessage("init");
        //Global.showMessage("\tS = "+S.toString());
        //Global.showMessage("\tind(S) = "+indexS);
        //Global.showMessage("\t"+L);


        for (Object x : alpha) {
            int j = beta.getIndexOf(x);
            if (j != -1) {
                List<PreferenceOrder> S_prima = new LinkedList<PreferenceOrder>();
                S_prima.add(new PreferenceOrder());
                for (int indZ = 0; indZ < L.size(); indZ++) {
                    List<PreferenceOrder> Z = L.get(indZ);
                    if (j >= indZ) {
                        S_prima.addAll(Z);
                    }
                }

                S = new LinkedList<PreferenceOrder>();
                for (PreferenceOrder y : S_prima) {
                    PreferenceOrder yx = new PreferenceOrder(y);
                    yx.addLastElement(x);
                    S.add(yx);
                }


                //indexS=j quiere decir que insertes S en la posición j
                L.set(j, S);

            }
            //Global.showMessage("");
            //Global.showMessage("===============================================");
            //Global.showMessage("loop "+i);
            //Global.showMessage("\tS = "+S);
            //Global.showMessage("\tind(S) = "+indexS);
            //Global.showMessage("\t"+L);
            i++;
        }

        List<PreferenceOrder> NalphaBeta = new LinkedList<PreferenceOrder>();
        NalphaBeta.add(new PreferenceOrder());

        for (List<PreferenceOrder> a : L) {
            for (PreferenceOrder b : a) {
                NalphaBeta.add(b);
            }
        }
        Collections.sort(NalphaBeta);
        //Global.showMessage("\tLista de ordenes comunes");
        //Global.showMessage(NalphaBeta.toString());
        return NalphaBeta;
    }

    /**
     * Obtiene las subsecuencias comunes de manera eficiente cuando existen
     * muchos elementos que no están en las dos secuencias
     *
     * @param alpha Orden total a comparar
     * @param beta Orden total a comparar
     * @return Devuelve las subsecuencias comunes a los dos ordenes totales
     */
    public static Collection<PreferenceOrder> getCommonSequences(PreferenceOrder alpha, PreferenceOrder beta) {
        //Global.showMessage("COMPARING '" + alpha + "' and '" + beta + "'");

        List<List<PreferenceOrder>> L = new LinkedList();
        //Lista vacía
        ArrayList<PreferenceOrder> list = new ArrayList(1);
        list.add(new PreferenceOrder());
        L.add(list);

        //El algoritmo del paper está mal, implementarlo a mi manera
        Set comunes = new TreeSet();
        comunes.addAll(alpha.getElements());
        comunes.retainAll(beta.getElements());

        list = new ArrayList<PreferenceOrder>(comunes.size());
        for (Object idItem : comunes) {
            list.add(new PreferenceOrder(idItem));
        }
        L.add(list);

        for (int i = 1; i < L.size(); i++) {
            list = new ArrayList<PreferenceOrder>();
            List<PreferenceOrder> ultimosAniadidos = L.get(i);

            for (PreferenceOrder e : ultimosAniadidos) {
                LinkedList<PreferenceOrder> addedThisLoop = new LinkedList<PreferenceOrder>();
                Object element = e.get(e.size() - 1);
                int indexAlpha = alpha.getIndexOf(element);
                int indexBeta = beta.getIndexOf(element);
                if (indexAlpha == alpha.size() - 1) {
                    //Es el último elemento de alpha
                    continue;
                }
                if (indexBeta == beta.size() - 1) {
                    //Es el último elemento de beta
                    continue;
                }
                List subListAlpha = alpha.getList().subList(indexAlpha + 1, alpha.size());
                List subListBeta = beta.getList().subList(indexBeta + 1, beta.size());
                Set elementosComunes = new TreeSet(subListAlpha);
                elementosComunes.retainAll(subListBeta);

                for (Object comun : elementosComunes) {
                    PreferenceOrder ex = new PreferenceOrder(e);
                    ex.addLastElement(comun);
                    addedThisLoop.add(ex);
                }

                if (!addedThisLoop.isEmpty()) {
                    //Global.showMessage("\t\tAdded " + addedThisLoop);
                    list.addAll(addedThisLoop);
                } else {
                    //Global.showMessage("no se añaden");
                }
            }
            if (!list.isEmpty()) {
                L.add(list);
            }
        }

        List<PreferenceOrder> NalphaBeta = new LinkedList<PreferenceOrder>();

        for (List<PreferenceOrder> a : L) {
            for (PreferenceOrder b : a) {
                NalphaBeta.add(b);
            }
        }
        Collections.sort(NalphaBeta);
//        Global.showMessage("\tLista de ordenes comunes");
//        Global.showMessage(NalphaBeta.toString());
        return NalphaBeta;
    }
}
