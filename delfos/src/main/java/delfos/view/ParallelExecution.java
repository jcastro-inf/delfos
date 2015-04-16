package delfos.view;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import delfos.experiment.casestudy.CaseStudy;

/**
 * Clase que realiza la ejecución en paralelo de un caso de estudio, para que la
 * interfaz pueda seguir respondiendo eventos.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class ParallelExecution extends SwingWorker<Void, Void> {

    private final CaseStudy c;
    private final JFrame frame;

    /**
     * Crea una hebra para ejecutar un caso de estudio de forma paralela.
     *
     * @param c Caso de estudio que se ejecuta.
     * @param f Ventana, para mostrar diálogos de error.
     */
    public ParallelExecution(CaseStudy c, JFrame f) {
        super();
        this.frame = f;
        this.c = c;
    }

    @Override
    public Void doInBackground() {
        Thread.currentThread().setName("Execute --> " + c.getAlias());
        try {
            c.execute();
            return null;
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(frame, ex.getStackTrace(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(System.err);
        }
        return null;
    }
}
