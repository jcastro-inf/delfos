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
