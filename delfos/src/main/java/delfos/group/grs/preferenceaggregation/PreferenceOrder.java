package delfos.group.grs.preferenceaggregation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Clase que encapsula el orden de preferencia de una serie de objetos.
 *
* @author Jorge Castro Gallardo
 */
public class PreferenceOrder<E> implements Iterable<E>,Comparable<PreferenceOrder<E>> {

    /**
     * Almacena los elementos en orden de preferencia descendiente.
     */
    private List<E> list;
    private Set<E> elementosDistintos;

    /**
     * Crea el objeto con el orden de preferencia establecido por parámetro
     *
     * @param preferenceOrder orden de los objetos
     */
    public PreferenceOrder(List<E> preferenceOrder) {
        list = new ArrayList<E>(preferenceOrder);
        elementosDistintos = new TreeSet<E>(preferenceOrder);
        
        if(elementosDistintos.size() != list.size()) {
            throw new IllegalArgumentException("Repeated elements in the list");
        }
    }

    /**
     * Crea el objeto con el orden de preferencia establecido por parámetro
     *
     * @param preferenceOrder orden de los objetos
     */
    public PreferenceOrder(PreferenceOrder<E> preferenceOrder) {
        list = new ArrayList<E>(preferenceOrder.list);
        elementosDistintos = new TreeSet<E>(list);
        
        if(elementosDistintos.size() != list.size()) {
            throw new IllegalArgumentException("Repeated elements in the list");
        }
        
    }

    /**
     * Crea el objeto de manera que e1 es preferido sobre e2
     *
     * @param e1
     * @param e2
     * 
     * @throws IllegalArgumentException Si los dos elementos que se pasan son 
     * iguales
     */
    public PreferenceOrder(E e1, E e2) {
        if(e1.equals(e2)){
            throw new IllegalArgumentException("Elements cannot be equal");
        }
        list= new ArrayList<E>();
        list.add(e1);
        list.add(e2);
        elementosDistintos = new TreeSet<E>();
        elementosDistintos.add(e1);
        elementosDistintos.add(e2);
    }

    /**
     * Crea el objeto de manera que solo se especifica una alternativa
     *
     * @param e
     */
    public PreferenceOrder(E e) {
        list= new ArrayList<E>(1);
        list.add(e);
        elementosDistintos = new TreeSet<E>();
        elementosDistintos.add(e);
    }

    /**
     * Crea el objeto sin ningun orden establecido, preparado para que se añadan
     * preferencias
     */
    public PreferenceOrder() {
        list = new ArrayList<E>();
        elementosDistintos = new TreeSet<E>();
    }

    /**
     * Añade un elemento con orden de preferencia mínimo.
     *
     * @param e
     * 
     * @throws IllegalArgumentException Si los dos elementos que se pasan son 
     * iguales
     */
    public void addLastElement(E e) {
        
        if(elementosDistintos.contains(e)) {
            throw new IllegalArgumentException("Repeated elements in the list");
        }
        list.add(e);
        elementosDistintos.add(e);
    }

    /**
     * Devuelve true si el elemento e1 es preferido sobre e2
     *
     * @param e1
     * @param e2
     * @return true si e1 es preferido sobre e2, false en otro caso
     * @throws IllegalArgumentException Si alguno de los elementos no se
     * encuentra en la lista de preferencias
     */
    public boolean isPreferedOver(E e1, E e2) {
        int indexE1 = -1, indexE2 = -1;
        for (int i = 0; i < list.size(); i++) {
        }

        if (indexE1 == -1) {
            throw new IllegalArgumentException("Elemen " + e1 + " not found int the preference order");
        }

        if (indexE2 == -1) {
            throw new IllegalArgumentException("Elemen " + e2 + " not found int the preference order");
        }

        return indexE1 < indexE2;
    }

    @Override
    public Iterator<E> iterator() {
        return list.iterator();
    }

    /**
     * Devuelve el elemento en la posición
     * <code>index</code>
     *
     * @param index Índice del elemento a devolver
     * @return Elemento en la posición <code>index</code>
     */
    public E get(int index) {

        if (index < 0 || index >= list.size()) {
            throw new IllegalArgumentException("Index out of range --> " + index + " not in (0," + (list.size() - 1));
        }

        return list.get(index);
    }

    /**
     * Devuelve el número de elementos que tiene definidos este orden de
     * preferencia
     *
     * @return Longitud del orden de preferencias
     */
    public int size() {
        return list.size();
    }

    /**
     * Devuelve la posición en la que se encuentra el elemento x.
     *
     * @param element Elemento a comprobar
     * @return Devuelve la posición en la que se encuentra el elemento x, si no
     * está, devuelve -1.
     */
    public int getIndexOf(E element) {
        int ret = -1;

        int i = 0;
        for (E innerElement : list) {
            if (innerElement.equals(element)) {
                ret = i;
                break;
            }
            i++;
        }

        return ret;
    }

    @Override
    public String toString() {
        StringBuilder ret = new StringBuilder();
        if (list.isEmpty()) {
            return "<empty>";
        }
        
        ret.append("'");
        for (E element : list) {
            ret.append(element.toString());
            ret.append(",");
        }
        ret.setCharAt(ret.length()-1, '\'');
        return ret.toString();
    }

    /**
     * Compara dos preferencias cuyo tipo es comparable
     * @param o
     * @return 1 si es mayor, 0 si es igual, -1 si es menor
     * 
     * @throws ClassCastException if the specified object's type prevents it
     *         from being compared to this object.
     * 
     */
    @Override
    public int compareTo(PreferenceOrder<E> o) {
        if(this.size() > o.size()) {
            return 1;
        }
        else{
            if(this.size() < o.size()){
                return -1;
            }else{
                //Son iguales
                for(int i=0;i<list.size();i++){
                    Comparable item1 = (Comparable)   list.get(i);
                    Comparable item2 = (Comparable) o.list.get(i);
                    
                    int comparison = item1.compareTo(item2);
                    if(comparison > 0){
                        return comparison;
                    }
                    if(comparison < 0){
                        return comparison;
                    }
                }
                
                return 0;
            }
        }
       
    }

    public Collection<E> getElements() {
        return elementosDistintos;
    }
    
    public List<E> getList(){
        return new ArrayList<E>(list);
    }
}
