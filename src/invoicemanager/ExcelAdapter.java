package invoicemanager;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.datatransfer.*;
import java.util.Arrays;

public class ExcelAdapter implements ActionListener {

    private Clipboard system;
    private StringSelection selection;
    private JTable table;

    public ExcelAdapter(JTable tbTable) {
        table = tbTable;
        KeyStroke copy = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        KeyStroke paste = KeyStroke.getKeyStroke(KeyEvent.VK_V, ActionEvent.CTRL_MASK, false);
        table.registerKeyboardAction(this, "Copy", copy, JComponent.WHEN_FOCUSED);
        table.registerKeyboardAction(this, "Paste", paste, JComponent.WHEN_FOCUSED);
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
    }

    public void actionPerformed(ActionEvent e) {
        try {
            String buffer = "";
            int rowCount = table.getSelectedRowCount(), columnCount = table.getSelectedColumnCount();
            int row = (table.getSelectedRows())[0], column = (table.getSelectedColumns())[0];
            if (e.getActionCommand().equals("Copy")) {
                for (int i = 0; i < rowCount; i++) {
                    for (int j = 0; j < columnCount; j++) {
                        buffer += table.getValueAt(row + i, column + j) + "\t";
                    }
                    buffer += "\n";
                }
                buffer = buffer.replace("\t\n", "\n");
                selection = new StringSelection(buffer);
                system = Toolkit.getDefaultToolkit().getSystemClipboard();
                system.setContents(selection, selection);
            } else if (e.getActionCommand().equals("Paste")) {
                buffer = (String) (system.getContents(this).getTransferData(DataFlavor.stringFlavor));
                String[] lines = buffer.split("\n");
                String[] tokens;
                if (lines.length > table.getRowCount() - row) {
                    rowCount = table.getRowCount() - row;
                } else {
                    rowCount = lines.length;
                }
                tokens = lines[0].split("\t", -1);
                if (tokens.length > (table.getColumnCount() - column)) {
                    columnCount = table.getColumnCount() - column;
                } else {
                    columnCount = tokens.length;
                }
                for (int i = 0; i < rowCount; i++) {
                    tokens = lines[i].split("\t", -1);
                    for (int j = 0; j < columnCount; ++j) {
                        table.setValueAt(tokens[j], row + i, column + j);
                    }
                }
            } else {
            }
        } catch (Exception error) {
        }
    }
}
