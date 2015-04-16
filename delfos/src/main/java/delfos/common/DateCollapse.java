package delfos.common;

/**
 * Clase con métodos estáticos para convertir el tiempo en una cadena amigable
 * al usuario
 *
* @author Jorge Castro Gallardo
 */
public class DateCollapse {

    /**
     * Método estático que transforma el tiempo en milisegundos que se le ha
     * pasado por parámetro en una cadena amigable al usuario. La cadena
     * devuelta tendrá la forma [DD]d [HH]h [MM]m [SS]s [MM]ms. En el caso en
     * que la fecha transformada a cadena de lugar a un valor de cero en
     * cualquiera de las magnitudes de tiempo, no se mostrará. NOTA: En el caso
     * de que la magnitud sea muy grande, se omitirán los milisegundos
     * restantes. Ej: collapse(177100) devolvería 2d 1h en lugar de 2d 1h 700ms
     *
     * @param miliseconds Tiempo en milisegundos
     * @return Cadena que representa el tiempo en las magnitudes superiores
     * necesarias
     */
    public static String collapse(long miliseconds) {
        if (miliseconds <= 0) {
            return "";
        }
        String ret = new String();

        boolean showMS = true;
        boolean showS = true;

        long segundos = miliseconds / 1000;
        miliseconds -= segundos * 1000;

        long minutos = segundos / 60;
        segundos -= minutos * 60;

        long horas = minutos / 60;
        minutos -= horas * 60;

        long dias = horas / 24;
        horas -= dias * 24;

        if (dias != 0) {
            ret = ret + dias + "d ";
            showS = false;
            showMS = false;
        }
        if (horas != 0) {
            ret = ret + horas + "h ";
            showS = false;
            showMS = false;
        }
        if (minutos != 0) {
            ret = ret + minutos + "m ";
            showMS = false;
        }

        if (segundos > 10) {
            showMS = false;
        }

        if (showS && segundos != 0) {
            ret = ret + segundos + "s ";
        }
        if (showMS && miliseconds != 0) {
            ret = ret + miliseconds + "ms ";
        }
        return ret;
    }

}
