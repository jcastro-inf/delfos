package delfos.view;

import delfos.common.Global;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.management.ManagementFactory;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * Ventana inicial en la que se muestran las opciones para utilizar la interfaz
 * de experimentación o de recomendación.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class InitialFrame extends JFrame {

    public static final long serialVersionUID = 1L;

    /**
     * Crea la ventana inicial de la biblioteca de recomendación.
     */
    public InitialFrame() {
        super("Recommender System Evaluation - " + ManagementFactory.getRuntimeMXBean().getName());

        initComponents();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosed(e);
                System.exit(0);
            }
        });
        pack();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

        setLocation((d.width - getWidth()) / 2, (d.height - getHeight()) / 2);
        toFront();
    }

    private void initComponents() {
        setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);

        JButton colaborativo = new JButton("Collaborative filtering recommender system");
        colaborativo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long time = System.currentTimeMillis();
                SingleExperiment_TraditionalRecommender_Window mw = new SingleExperiment_TraditionalRecommender_Window(InitialFrame.this);
                InitialFrame.this.setVisible(false);
                mw.setVisible(true);
                mw.toFront();
                Global.showMessage("Interface built in " + (System.currentTimeMillis() - time) + " ms\n");
            }
        });
        add(colaborativo, constraints);
    }
}
