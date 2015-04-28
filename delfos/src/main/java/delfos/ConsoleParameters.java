package delfos;

import delfos.common.Global;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Guarda los parámetros definidos en una estructura para que sean fácilmente
 * accesibles en el codigo de una aplicación
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (18-02-2013) Eliminación del fichero general de conexión de base
 * de datos.
 */
public class ConsoleParameters {

    /**
     * Función para inicializar este objeto a partir de los argumentos de la
     * linea de comandos.
     *
     * @param args Argumentos especificados en la llamada al programa.
     * @return
     */
    public static ConsoleParameters parseArguments(String[] args) {
        return new ConsoleParameters(args);
    }

    /**
     * Estructura de parámetros. Como clave se usan los tokens que satisfacen la
     * expresión regular "-*" y como valor, el siguiente parámetro si no
     * satisface la expresión regular. Si el siguiente parámetro satisface la
     * expresión regular, se asigna null y se considera que es un parámetro de
     * configuración que funciona por presencia o ausencia
     */
    private final Map<String, List<String>> parametersValues;
    private final TreeMap<String, Boolean> parametersUsed;
    private final String[] consoleRawParameters;

    /**
     * Constructor de la estructura de parámetros. No se recomienda su uso en la
     * programación de sistemas de recomendación.
     *
     * Use only in test methods.
     *
     * @param console Vector de cadenas con los parámetros de la línea de
     * comandos
     */
    public ConsoleParameters(String... console) {
        this.parametersValues = new TreeMap<>();
        this.parametersUsed = new TreeMap<>();
        this.consoleRawParameters = console;

        String lastValidParameter = null;

        for (String token : console) {
            if (canBeParameterDefinition(token)) {
                if (isValidParameter(token)) {
                    lastValidParameter = token;
                    this.parametersValues.put(lastValidParameter, new ArrayList<>());
                    this.parametersUsed.put(token, Boolean.FALSE);
                } else {
                    throw new IllegalArgumentException("Console argument '" + token + "' does not meet the parameter definition syntax.");
                }
            } else if (lastValidParameter != null) {
                this.parametersValues.get(lastValidParameter).add(token);
            } else {
                throw new IllegalArgumentException("Unexpected value token '" + token + "', it must be linked to a parameter definded before (-parameter value).");
            }
        }
    }

    /**
     * Comprueba si el nombre del parámetro cumple con la expresion regular
     * -[a-zA-Z][a-zA-Z]*[0-9]*.
     *
     * <p>
     * <p>
     * Ejemplos de nombres de parámetros válidos: -user -item1 -i1
     *
     * <p>
     * <p>
     * Ejemplos de nombres de parámetros <b>NO</b> válidos: -8 nombre
     *
     * @param parameter Cadena con el argumento por línea de comandos.
     * @return True si es un parámetro válido, false en otro caso
     */
    private boolean isValidParameter(String parameter) {
        return parameter.matches("-(-)?[a-zA-Z][a-zA-Z0-9]*((-[a-zA-Z][a-zA-Z0-9]*)*)");
    }

    /**
     * True si el parametro empieza por un guión seguido de un caracter
     * alfanumérico.
     *
     * @param parameter
     * @return
     */
    private boolean canBeParameterDefinition(String parameter) {
        return parameter.matches("-(-)?[a-zA-Z].*");
    }

    /**
     * Comprueba si un nombre de parámetro aparece en la línea de comandos de la
     * llamada al programa
     *
     * @param parameter Cadena con el nombre del parámetro buscado
     * @return Devuelve true si el parámetro aparece en la linea de comandos del
     * programa, tenga o no asociado un valor. Devuelve false en otro caso.
     *
     * @throws IllegalArgumentException El parámetro indicado no es válido.
     */
    public boolean isDefined(String parameter) {
        if (isValidParameter(parameter)) {
            setUsed(parameter);
            return parametersValues.containsKey(parameter);
        } else {
            isValidParameter(parameter);
            throw new IllegalArgumentException("The parameter '" + parameter + "' is not valid");
        }
    }

    public List<String> getValues(String parameter) throws UndefinedParameterException {
        if (!isValidParameter(parameter)) {
            throw new IllegalArgumentException("The parameter '" + parameter + "' is not valid");
        } else if (isDefined(parameter)) {
            setUsed(parameter);
            return parametersValues.get(parameter);
        } else {
            throw new UndefinedParameterException("The parameter '" + parameter + "' is not defined", parameter);
        }

    }

    /**
     * Devuelve el valor asociado al parámetro dado. Si el parámetro tiene
     * múltiples valores, devuelve el primero.
     *
     * @param parameter Nombre del parámetro para el que se busca su valor.
     * @return Valor del parámetro indicado.
     *
     * @throws UndefinedParameterException Cuando el parámetro indicado no está
     * presente en la llamada por línea de comandos o cuando no tiene valores
     * asociados al mismo.
     *
     * @throws IllegalArgumentException El parámetro indicado no es válido. Debe
     * satisfacer la siguiente expresión regular:
     * <b>-[a-zA-Z][a-zA-Z]*[0-9]*</b>
     */
    public String getValue(String parameter) throws UndefinedParameterException {
        if (isValidParameter(parameter)) {
            if (isDefined(parameter)) {
                setUsed(parameter);
                List<String> values = getValues(parameter);
                if (values.isEmpty()) {
                    throw new UndefinedParameterException("Parameter '" + parameter + "' has no associated values.", parameter);
                } else {
                    if (values.size() == 1) {
                        return values.get(0);
                    } else {
                        Global.showWarning("Parameter '" + parameter + "' has more than one values, returning the first");
                        return values.get(0);
                    }
                }
            } else {
                throw new UndefinedParameterException("The parameter '" + parameter + "' is not defined", parameter);
            }
        } else {
            throw new IllegalArgumentException("The parameter '" + parameter + "' is not valid");
        }
    }

    public boolean deprecatedParameter_isDefined(String oldParameter, String parameter) {
        if (this.isDefined(oldParameter)) {
            Global.showWarning("Using deprecated parameter " + oldParameter + ", use " + parameter + " instead.");
        }

        if (this.isDefined(oldParameter) && this.isDefined(parameter)) {
            Global.showWarning("Use '" + parameter + "' parameter only. Do not use " + oldParameter);
        }

        return this.isDefined(oldParameter) || this.isDefined(parameter);
    }

    public String deprecatedParameter_getValue(String oldParameter, String parameter) {

        if (this.isDefined(parameter)) {
            return this.getValue(parameter);
        }
        if (this.isDefined(oldParameter)) {
            return this.getValue(oldParameter);
        }

        throw new UndefinedParameterException(parameter + "(" + oldParameter + ")", parameter);
    }

    public List<String> deprecatedParameter_getValues(String oldParameter, String parameter) {

        List<String> values = new LinkedList<>();
        if (this.isDefined(parameter)) {
            values.addAll(this.getValues(parameter));
        }
        if (this.isDefined(oldParameter)) {
            values.addAll(this.getValues(oldParameter));
        }

        if (values.isEmpty()) {
            throw new UndefinedParameterException(parameter + "(" + oldParameter + ")", parameter);
        }
        return values;
    }

    private boolean isUsed(String parameter) {
        return parametersUsed.containsKey(parameter);
    }

    private void setUsed(String parameter) {
        if (parametersUsed.containsKey(parameter)) {
            parametersUsed.put(parameter, Boolean.TRUE);
        }
    }

    public List<String> getAllUnusedParameters() {
        List<String> ret = new LinkedList<>();

        parametersUsed.keySet().stream()
                .filter((parameter) -> (!isUsed(parameter)))
                .forEach((parameter) -> {
                    ret.add(parameter);
                });

        return ret;
    }

    public void printUnusedParameters(PrintStream out) {
        List<String> unusedList = getAllUnusedParameters();
        unusedList.stream().forEach((unusedParameter) -> {
            out.println("The parameter '" + unusedParameter + "' has not been recognised");
        });
    }

    public String printOriginalParameters() {

        StringBuilder str = new StringBuilder();

        List<String> parameters = Arrays.asList(this.consoleRawParameters);

        if (parameters.isEmpty()) {
            str.append("No command line parameters specified.");
        } else {
            Iterator<String> iterator = parameters.iterator();
            str.append(iterator.next());

            for (; iterator.hasNext();) {
                String parameter = iterator.next();
                str.append(" ").append(parameter);
            }
        }
        return str.toString();
    }

    @Override
    public String toString() {
        return printOriginalParameters();
    }

    public String[] getConsoleRawParameters() {
        return consoleRawParameters;
    }
}
