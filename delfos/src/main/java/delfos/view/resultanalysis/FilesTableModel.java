package delfos.view.resultanalysis;

import java.io.File;
import javax.swing.table.AbstractTableModel;
import delfos.common.FileUtilities;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 19-Noviembre-2013
 */
public class FilesTableModel extends AbstractTableModel {

    public void updateOutput(File[] files) {

        values = new Object[files.length][3];
        for (int i = 0; i < files.length; i++) {
            File file = files[i];

            values[i][0] = true;
            values[i][1] = file.getAbsolutePath();
            values[i][2] = FileUtilities.getFileName(file);
        }
        fireTableDataChanged();
    }

    private String[] columnNames = {"Sel.", "Directory", "FileName"};
    private Object[][] values;

    public FilesTableModel() {
        values = new Object[1][3];

        values[0][0] = Boolean.TRUE;
        values[0][1] = new File(".").getAbsolutePath();
        values[0][2] = "DummyFile.tmp";

    }

    @Override
    public int getRowCount() {
        return values.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return values[rowIndex][columnIndex];
    }
}
