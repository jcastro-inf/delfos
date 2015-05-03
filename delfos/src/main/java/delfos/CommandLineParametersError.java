package delfos;

/**
 *
 * @author jcastro
 */
public class CommandLineParametersError extends Exception {

    String parameter;
    String msg;
    String userFriendlyMsg;

    public CommandLineParametersError(String parameter, String msg, String userFriendlyMsg) {
        this.parameter = parameter;
        this.msg = msg;
        this.userFriendlyMsg = userFriendlyMsg;
    }

    public String getUserFriendlyMsg() {
        return userFriendlyMsg;
    }

    public String getParameter() {
        return this.parameter;
    }

}
