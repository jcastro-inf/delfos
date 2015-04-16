package delfos.view;

/**
 * Interfaz que define el método invocado cuando se termine la ejecución deun
 * caso de estudio {@link CaseStudy.CaseStudy} concreto.
 *
 *
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 *
 * @deprecated Esta interfaz no se usa debido a que otras interfaces hacen la
 * misma funcion de observador
 */
@Deprecated
public interface ParallelExecutionListener {

    /**
     * Indica al listener que la ejecución del caso de estudio ha finalizado.
     *
     * @param error Indica si ha habido errores en la ejecución.
     */
    public void executionFinished(boolean error);
}
