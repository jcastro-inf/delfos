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

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/**
 * Clase que encapsula el comportamiento de las ventanas dentro del módulo de 
 * evaluación de sistemas de recomendación
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
