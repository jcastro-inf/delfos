package delfos.configureddatasets.mock;

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
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.view.configureddatasets.NewConfiguredDatasetDialog;

/**
 * Ventana inicial en la que se muestran las opciones para utilizar la interfaz
 * de experimentación o de recomendación.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class InitialFrameMock extends JFrame {

    public static final long serialVersionUID = 1L;

    /**
     * Crea la ventana inicial de la biblioteca de recomendación.
     */
    public InitialFrameMock() {
        super("Manage Configured Datasets- " + ManagementFactory.getRuntimeMXBean().getName());

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

        JButton colaborativo = new JButton("Add dataset");
        colaborativo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ConfiguredDatasetsFactory.getInstance().showCreateConfiguredDatasetDialog(InitialFrameMock.this);
            }
        });
        add(colaborativo, constraints);
    }

    public static void main(String[] args) {
        InitialFrameMock mock = new InitialFrameMock();
        mock.setVisible(true);
        mock.toFront();
    }
}
