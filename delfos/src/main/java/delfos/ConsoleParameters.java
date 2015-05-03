package delfos;

import delfos.common.Global;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
     * @throws delfos.CommandLineParametersError If a parameter has no values or
     * a flag has values.
     */
    public static ConsoleParameters parseArguments(String[] args) throws CommandLineParametersError {
        return new ConsoleParameters(args);
    }

    /**
     * Estructura de parámetros. Como clave se usan los tokens que satisfacen la
     * expresión regular de los parametros con valores asociados y como valor,
     * el siguiente parámetro si no satisface la expresión regular. Si el
     * siguiente parámetro satisface la expresión regular, se asigna null y se
     * considera que es un flag.
     */
    private final Map<String, List<String>> parametersWithValues;
    private final Set<String> presentFlags;

    private final TreeSet<String> unusedParameters;
    private final String[] consoleRawParameters;

    /**
     * Constructor de la estructura de parámetros. No se recomienda su uso en la
     * programación de sistemas de recomendación.
     *
     * Use only in test methods.
     *
     * @param console Vector de cadenas con los parámetros de la línea de
     * comandos
     * @throws delfos.CommandLineParametersError
     */
    public ConsoleParameters(String... console) throws CommandLineParametersError {
        this.parametersWithValues = new TreeMap<>();
        this.presentFlags = new TreeSet<>();
        this.unusedParameters = new TreeSet<>();
        this.consoleRawParameters = console;

        for (int i = 0; i < console.length; i++) {
            String parameter = console[i];
            if (canBeParameterDefinition(parameter)) {
                if (isValidFlag(parameter)) {

                    if (presentFlags.contains(parameter)) {
                        String msg = "Flag '" + parameter + "' is duplicated.";
                        throw new CommandLineParametersError(parameter, msg, msg);
                    }
                    presentFlags.add(parameter);
                    unusedParameters.add(parameter);

                    checkFlagTakesNoValues(console, parameter, i);

                } else if (isValidParameter(parameter)) {
                    if (parametersWithValues.containsKey(parameter)) {
                        throw new IllegalArgumentException("Duplicated parameter '" + parameter + "'");
                    }

                    List<String> parameterValues;
                    if (i + 1 < console.length) {
                        parameterValues = getParameterValues(console, parameter, i);
                    } else {
                        parameterValues = Collections.EMPTY_LIST;
                    }

                    if (parameterValues.isEmpty()) {
                        String msg = "'" + parameter + "' has parameter syntax, but it has no values.Perhaps:\n"
                                + "\tit should be a flag (flags start with double minus (--).\n"
                                + "\tIt is misspelled.";
                        throw new CommandLineParametersError(parameter, "Command line parameter '" + parameter + "' has no values.", msg);
                    } else {
                        this.parametersWithValues.put(parameter, parameterValues);
                        this.unusedParameters.add(parameter);
                        i += parameterValues.size();
                    }

                } else {
                    throw new IllegalArgumentException("Console argument '" + parameter + "' does not meet the parameter definition syntax.");
                }
            } else {
                throw new IllegalArgumentException("Unexpected value token '" + parameter + "', it must be linked to a parameter definded before (-parameter value).");
            }
        }
    }

    private void checkFlagTakesNoValues(String[] console, String parameter, int i) throws CommandLineParametersError {
        if ((i + 1 < console.length)
                && (!canBeParameterDefinition(console[i + 1]))) {

            String msg = "Flag '" + parameter + "' cannot take values";
            throw new CommandLineParametersError(
                    parameter,
                    msg, msg);
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
        return parameter.matches("-[a-zA-Z][a-zA-Z0-9]*((-[a-zA-Z][a-zA-Z0-9]*)*)");
    }

    /**
     * Tests if the parameter is a valid flag. Examples of valid flags are:
     *
     * --verbose
     *
     * --default-yes
     *
     * Examples of not valid flags are:
     *
     * -verbose
     *
     * default-invalidflag
     *
     * -8675
     *
     * @param parameter Command line argument
     * @return true if it is a valid flag.
     */
    private boolean isValidFlag(String parameter) {
        return parameter.matches("--[a-zA-Z][a-zA-Z0-9]*((-[a-zA-Z][a-zA-Z0-9]*)*)");
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
    public boolean isParameterDefined(String parameter) {
        if (isValidParameter(parameter)) {
            setUsed(parameter);
            return parametersWithValues.containsKey(parameter);
        } else {
            isValidParameter(parameter);
            throw new IllegalArgumentException("The parameter '" + parameter + "' is not valid");
        }
    }

    /**
     * Check if a flag is present in the command line.
     *
     * @param flag Flag that this method searches for.
     * @return true if the flag is present, false otherwise.
     * @throws IllegalArgumentException The flag do not have the right syntax.
     * Check {@link ConsoleParameters#isValidFlag(java.lang.String) function.
     */
    public boolean isFlagDefined(String flag) {
        if (isValidFlag(flag)) {
            setUsed(flag);
            return this.presentFlags.contains(flag);
        } else {
            isValidParameter(flag);
            throw new IllegalArgumentException("The parameter '" + flag + "' is not valid");
        }
    }

    public List<String> getValues(String parameter) throws UndefinedParameterException {
        if (!isValidParameter(parameter)) {
            throw new IllegalArgumentException("The parameter '" + parameter + "' is not valid");
        } else if (isParameterDefined(parameter)) {
            setUsed(parameter);
            return parametersWithValues.get(parameter);
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
            if (isParameterDefined(parameter)) {
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

    private void setUsed(String parameter) {
        if (unusedParameters.contains(parameter)) {
            unusedParameters.remove(parameter);
        }
    }

    public Set<String> getAllUnusedParameters() {
        return Collections.unmodifiableSet(unusedParameters);
    }

    public void printUnusedParameters(PrintStream out) {
        Set<String> unusedList = getAllUnusedParameters();
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

    private List<String> getParameterValues(String[] console, String parameter, int i) {

        ArrayList<String> parameterValues = new ArrayList<>();
        for (int j = i + 1; j < console.length; j++) {
            if (!canBeParameterDefinition(console[j])) {
                parameterValues.add(console[j]);
            } else {
                return parameterValues;
            }
        }
        return parameterValues;
    }
}
