package delfos.group.grs;

import java.io.Serializable;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 29-May-2013
 */
public class SingleRecommenderSystemModel implements Serializable{
    
    private static final long serialVersionUID = 121L;

    private final Object recommenderSystemModel;

    public SingleRecommenderSystemModel(Object recommenderSystemModel) {
        this.recommenderSystemModel = recommenderSystemModel;
    }

    public Object getRecommenderSystemModel() {
        return recommenderSystemModel;
    }
}
