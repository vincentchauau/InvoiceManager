package invoicemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.write.*;
class EditableTableModel extends AbstractTableModel {
    public String database;
    public String[] tables;
    public String table;
    public String[] columns;
    public String[] types;
    public String[] toolTips;
    public int[] columnAligns;
    public String[] columnFormats;
    public String[] columnDisplayFormats;
    public List<String[]> rows;
    boolean[] columnEditables;
    public int[] selectedRows;
    public double amount, gst, paid, GST;
    public List<String[]> autoCompleteColumns;
    Function function = new Function();
    // 1. Get columns, column types
    public void getColumns(String database, String table) {
        this.database = database;
        this.table = table;
        rows = new ArrayList<>();
        columns = Database.getColumns(database, table);
        types = Database.getColumnTypes(database, table);
        columnEditables = new boolean[columns.length];
        Arrays.fill(columnEditables, true);
        columnEditables[0] = false;
        columnAligns = new int[columns.length];// 0 left 1 middle 2 right
        columnFormats = new String[columns.length];
        Arrays.fill(columnFormats, ".*");
        columnDisplayFormats = new String[columns.length];
        autoCompleteColumns = new ArrayList<>();
        for (int i = 0; i < columns.length;++i)
        {
            autoCompleteColumns.add(Database.getValuesByColumn(table, columns[i]));
        }
    }
    // 2. Set editable columns
    public boolean isCellEditable(int row, int column) {
        return columnEditables[column];
    }
    
    // 3. Get row count, column count, column name, column class
    public int getRowCount() {
        return rows.size();
    }

    public int getColumnCount() {
        return columns.length;
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public Class getColumnClass(int column) {
        return String.class;
    }
    // 4. Get and set cell value
    public Object getValueAt(int row, int column) {
        return rows.get(row)[column];
    }
    
    public void setValueAt(Object value, int row, int column) 
    {
        String val = String.valueOf(value);
        if (val.matches(columnFormats[column]) || val.equals(""))
        {
            Database.setRow(table, columns, types, column, String.valueOf(value), rows.get(row)[0]);
            rows.get(row)[column] = String.valueOf(value);
            fireTableRowsUpdated(row, row);
            getStatistic();
            if (columns[column].equals("AMT"))
            {
                double gst = function.parseDouble(val)*GST;
                setValueAt(gst, row, column + 1);
            }
            else{}
        }
        else{}
    }
    // 5. Insert a number of rows
    public void insertRows(String[] values, int number) 
    {
        int id;
        for (int i = 0; i < number; ++i) 
        {
            id = Database.insertRow(table, columns, types, values);
            String[] newValues = values.clone();
            newValues[0] = String.valueOf(id);
            rows.add(newValues);
        }
        fireTableRowsInserted(rows.size()-number-1, rows.size() - 1);
        getStatistic();
    }
    // 6. Delete rows
    public void deleteRows(String[] values) {
        Database.deleteRows(table, columns, types, values);
        fireTableDataChanged();
        getStatistic();
    }
    public void deleteRows(int[] indices) {
        Arrays.sort(indices);
        for (int i = indices.length - 1; i >= 0; --i) {
            Database.deleteRow(table, columns, types, rows.get(indices[i])[0]);
            rows.remove(indices[i]);
        }
        fireTableRowsDeleted(indices[0],indices[indices.length-1]);
        getStatistic();
    }
    // 7. Set rows with values
    public void setRows(int[] indices, String[] values) {
        for (int i = 0; i < indices.length; ++i) 
        {
            values[0] = rows.get(indices[i])[0];
            Database.setRow(table, columns, types, values, rows.get(indices[i])[0]);
            rows.set(indices[i],values.clone());
        }
        fireTableRowsUpdated(indices[0], indices[indices.length-1]);
        getStatistic();
    }
    // 8. Get rows
    public void getRows(String[] values) {
        rows = Database.resultSet2Rows(Database.getRows(table, columns, types, values));
        getStatistic();
        fireTableDataChanged();
    }
    // 9. Get values by column
    public String[] getValuesByColumn(String column) {
        return Database.getValuesByColumn(table, column);
    }
    // 10. Import rows to a file
    public boolean importFile(File file) {
        try 
        {
            Database.deleteRow(table, columns, types, "");
            rows.clear();
            int id;
            String[] values;
            if (file.getName().endsWith(".csv")) 
            {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line = reader.readLine(); // skip the header
                while ((line = reader.readLine()) != null) 
                {
                    values = line.split("\t");
                    id = Database.insertRow(table, columns, types, values);
                    String[] newValues = values.clone();
                    newValues[0] = String.valueOf(id);
                    rows.add(newValues);
                }
                
                getStatistic();
                fireTableRowsInserted(0, rows.size() - 1);
                return true;
            } 
            else if (file.getName().endsWith(".xls")) 
            {
                Workbook excel = Workbook.getWorkbook(file);
                Sheet sheet = excel.getSheet(0);
                values = new String[sheet.getColumns()];
                for (int i = 1; i < sheet.getRows(); ++i)
                {
                    for (int j = 0; j < sheet.getColumns(); ++j)
                    {
                        values[j] = sheet.getCell(j, i).getContents();
                    }
                    id = Database.insertRow(table, columns, types, values);
                    String[] newValues = values.clone();
                    newValues[0] = String.valueOf(id);
                    rows.add(newValues);

                }
                getStatistic();
                fireTableRowsInserted(0, rows.size() - 1);
                return true;
            } else {
            }
        } catch (Exception e) {
        }
        return false;
    }
    // 11. Export rows to a file
    public boolean exportFile(File file) {
        try 
        {
            if (file.getName().endsWith(".csv")) 
            {
                FileWriter csv = new FileWriter(file);
                for (int i = 0; i < columns.length; i++) {
                    csv.write(columns[i] + ",");
                }
                csv.write("\n");
                for (int i = 0; i < rows.size(); i++) {
                    for (int j = 0; j < columns.length; j++) {
                        csv.write(rows.get(i)[j].toString().replace(',', ' ').replace('\r', ' ').replace('\n', ' ') + ",");
                    }
                    csv.write("\n");
                }
                csv.close();
                return true;
            } else if (file.getName().endsWith(".xls")) 
            {
                WritableWorkbook excel = Workbook.createWorkbook(file);
                WritableSheet sheet = excel.createSheet("Sheet 1", 0);
                Label cell;
                for (int i = 0; i < columns.length; ++i)
                {
                    cell = new Label(i, 0, columns[i]);
                    sheet.addCell(cell);
                    for (int j = 0; j < rows.size(); ++j)
                    {
                        cell = new Label(i, j + 1, rows.get(j)[i]);
                        sheet.addCell(cell);                        
                    }
                }
                excel.write();
                return true;
            } else {
            }
        } catch (Exception e) {
        }
        return false;
    }
    public void getStatistic() {
        amount = 0;
        gst = 0;
        paid = 0;
        try {
            for (int i = 0; i < rows.size(); ++i) {
                amount += function.parseDouble(rows.get(i)[7]);
                gst += function.parseDouble(rows.get(i)[8]);
                if (!rows.get(i)[10].equals("")) 
                {
                    paid += function.parseDouble(rows.get(i)[7]);
                } else {
                }
            }
        } catch (Exception error) {
        }
    }
}
