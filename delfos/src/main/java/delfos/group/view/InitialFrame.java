package delfos.group.view;

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
import delfos.group.view.groupexperimentation.GroupExperimentationWindow;
import delfos.group.view.grouprecommendation.GroupRecommendationWindow;

/**
 *
* @author Jorge Castro Gallardo
 */
public class InitialFrame extends JFrame {

    public InitialFrame() {
        super("Group Recommender Systems - " + ManagementFactory.getRuntimeMXBean().getName());

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
        JButton contenido = new JButton("Recommendation module");
        contenido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long time = System.currentTimeMillis();

                GroupRecommendationWindow mw = new GroupRecommendationWindow(InitialFrame.this);
                InitialFrame.this.setVisible(false);

                mw.setVisible(true);
                mw.toFront();

                Global.showInfoMessage("Interface built in " + (System.currentTimeMillis() - time) + " ms\n");
            }
        });
        add(contenido, constraints);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1.0;
        constraints.weighty = 0.0;
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.insets = new Insets(3, 4, 3, 4);
        JButton colaborativo = new JButton("Experimentation module");
        colaborativo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long time = System.currentTimeMillis();
                GroupExperimentationWindow mw = new GroupExperimentationWindow(InitialFrame.this);
                InitialFrame.this.setVisible(false);
                mw.setVisible(true);
                mw.toFront();
                Global.showInfoMessage("Interface built in " + (System.currentTimeMillis() - time) + " ms\n");
            }
        });
        add(colaborativo, constraints);
    }
}
