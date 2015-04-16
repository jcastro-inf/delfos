package delfos.view;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Clase que encapsula el comportamiento de las ventanas dentro del módulo de 
 * evaluación de sistemas de recomendación
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class ComportamientoSubVentanas extends WindowAdapter{
    private final InitialFrame f;
    private final JFrame subFrame;

    /**
     * Constructor que especifica la ventana inicial y la subventana que a la 
     * que se ha asignado este {@link WindowAdapter}
     * @param f
     * @param subFrame 
     */
    public ComportamientoSubVentanas(InitialFrame f,JFrame subFrame) {
        this.f = f;
        this.subFrame = subFrame;
    }

    @Override
    public void windowClosing(WindowEvent e) {
        subFrame.setVisible(false);
        subFrame.dispose();
        
        f.setVisible(true);
        f.toFront();
    }
}
