package delfos.group.grs.preferenceaggregation.order;

import delfos.common.Global;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
* @author Jorge Castro Gallardo
 */
public class LearningToOrderThings {

    public static List greedyOrderSimplificado(Set idItemsRated, Preff preff) {
        List<Object> ret = new ArrayList<Object>(idItemsRated.size());
        List<Object> V = new ArrayList<Object>(idItemsRated);
        
        List<Double> heuristic = new ArrayList<Double>();
        for(double d:heuristic(V,preff)){
            heuristic.add(d);
        }
        
        int numOrdenados=0;
        while(!V.isEmpty()){
            Object t;
            
            int index = 0;
            double maxValue=-Double.MAX_VALUE;
            for(int i=0;i<heuristic.size();i++){
                if(heuristic.get(i) > maxValue){
                    index=i;
                    maxValue = heuristic.get(i);
                }
            }
            
            if(index > V.size()-1 || index < 0){
                Global.showWarning("indexNotInRange");
            }
            t = V.get(index);
            ret.add(t);
            
            
            
            V.remove(t);
            if(!V.isEmpty()){
                
                numOrdenados++;
                
                int percent = (numOrdenados*100) / V.size();
                if( percent > 10 ){
                    heuristic = new ArrayList<Double>();
                    for(double d:heuristic(V,preff)){
                        heuristic.add(d);
                    }
                    numOrdenados = 0;
                }else{
                    heuristic.remove(index);
                }
            }
            
            Global.showInfoMessage("Quedan "+V.size()+" elementos\n");
        }
        return ret;
    }
    
    private LearningToOrderThings(){
        
    }
    
    private static double[] heuristic(List<Object> V, Preff<Object> preff){
        double[] ret = new double[V.size()];
        for(int i=0;i<V.size();i++) {
            ret[i] = 0;
            Object v = V.get(i);
            
            double positive=0;
            for(Object u:V){
                positive += preff.preff(v, u);
            }

            double negative=0;
            for(Object u:V){
                 negative += preff.preff(u,v);
            }
            ret[i] = positive-negative;
        }
        
        return ret;
    }
    
    
    public static List greedyOrder(Set set,Preff preff){
        
        List<Object> ret = new ArrayList<Object>(set.size());
        List<Object> V = new ArrayList<Object>(set);
        
        double[] heuristic = heuristic(V,preff);
        
        while(!V.isEmpty()){
            Object t;
            
            int index = 0;
            double maxValue=-Double.MAX_VALUE;
            for(int i=0;i<heuristic.length;i++){
                if(heuristic[i] > maxValue){
                    index=i;
                    maxValue = heuristic[i];
                }
            }
            
            t = V.get(index);
            ret.add(t);
            boolean remove = V.remove(t);
            if(!remove) {
                throw new UnsupportedOperationException("the element t is not in the list");
            }
            heuristic = heuristic(V, preff);
            Global.showInfoMessage("Quedan "+V.size()+" elementos\n");
        }
        return ret;
    }
            
}
