/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.view;

import delfos.experiment.casestudy.CaseStudy;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Clase que realiza la ejecución en paralelo de un caso de estudio, para que la interfaz pueda seguir respondiendo
 * eventos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
            c.executeNew();
            return null;
        } catch (Throwable ex) {
            JOptionPane.showMessageDialog(frame, ex.getStackTrace(), ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace(System.err);
        }
        return null;
    }
}
