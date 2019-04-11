package invoicemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

@SuppressWarnings("serial")
public class InvoiceManager {

    private static JFrame frame;
    private static JPopupMenu mnGet;
    private static JToolBar tbToolbar;
    private static JButton btClear;
    private static JButton btGet;
    private static JButton btSet;
    private static JButton btInsert;
    private static JButton btDelete;
    private static JButton btImport;
    private static JButton btExport;
    private static JButton btBackup;
    private static JComboBox cbDatabases;
    private static JComboBox cbTables;
    private static JComboBox cbColumns;
    private static JLabel lbStatistic;
    private static JSplitPane spSetPickGet;
    private static JSplitPane spSetPick;
    private static JPanel pnSet;
    private static JPanel pnPick;
    private static JTable tbPick;
    private static JTable tbGet;
    private static String[] databases;
    private static String[] tables;
    private static Map<String, String> map;
    private static EditableTableModel modelGet;
    private static DefaultTableModel modelPick;
    private static SimpleDateFormat dateFormat1;
    private static SimpleDateFormat dateFormat2;
    private static NumberFormat currencyFormat;
    private static NumberFormat doubleFormat;
    private static NumberFormat percentageFormat;
    private static ExcelAdapter excelAdapter;
    private static Function function;

    // Clear inputs
    private static void clearInputPickValues() {
        modelPick.setRowCount(0);
        for (int i = 0; i < modelGet.getColumnCount(); ++i) {
            ((JTextFieldX) pnSet.getComponent(i * 2 + 1)).setText("");
        }
    }

    // Get inputs
    private static String[] getInputValues() {
        List<String> inputs = new ArrayList<>();
        for (int i = 0; i < pnSet.getComponentCount() / 2; ++i) {
            Component c = pnSet.getComponent(i * 2 + 1);
            if (c instanceof JTextFieldX) {
                inputs.add(((JTextFieldX) c).getText());
            } else {
            }
        }
        return inputs.toArray(new String[0]);
    }

    // Get input values, pick values
    private static void getInputPickValues(int r, int c) {
        if (r != -1 && c != -1) {
            String[] values = modelGet.rows.get(r);
            for (int i = 0; i < modelGet.columns.length; ++i) {
                ((JTextFieldX) pnSet.getComponent(i * 2 + 1)).setText(values[i]);
            }
            List<String[]> rows;
            if (modelGet.table.equals("SELL")) {
                String value = modelGet.getValueAt(r, modelGet.getColumnCount() - 1).toString();
                modelPick.setRowCount(0);
                rows = function.string2Array(value);
                for (String[] row : rows) {
                    modelPick.addRow(row);
                }
            } else if (modelGet.table.equals("BUY")) {
                modelPick.setRowCount(0);
                rows = Database.resultSet2Rows(Database.getSummary(cbColumns.getSelectedItem().toString()));
                for (String[] row : rows) {
                    modelPick.addRow(row);
                }
            } else {
            }
        } else {
        }
    }

    public static void main(String args[]) {
        try {
            // Font & image
            Font smallFont = new Font("Serif", Font.PLAIN, 14);
            Font mediumFont = new Font("Serif", Font.PLAIN, 18);
            ImageIcon icClear = new ImageIcon("Icon/clear.png");
            ImageIcon icGet = new ImageIcon("Icon/get.png");
            ImageIcon icSet = new ImageIcon("Icon/set.png");
            ImageIcon icInsert = new ImageIcon("Icon/insert.png");
            ImageIcon icDelete = new ImageIcon("Icon/delete.png");
            ImageIcon icImport = new ImageIcon("Icon/import.png");
            ImageIcon icExport = new ImageIcon("Icon/export.png");
            ImageIcon icBackup = new ImageIcon("Icon/backup.png");
            ImageIcon icFrame = new ImageIcon("Icon/frame.png");

            // Buttons
            btClear = new JButton("Clear", icClear);
            btClear.setToolTipText("Clear input values");
            btGet = new JButton("Get", icGet);
            btGet.setToolTipText("Get rows with input values");
            btSet = new JButton("Set", icSet);
            btSet.setToolTipText("Set selected rows with input values");
            btInsert = new JButton("Insert", icInsert);
            btInsert.setToolTipText("Insert new rows with input values");
            btDelete = new JButton("Delete", icDelete);
            btDelete.setToolTipText("Delete selected rows");
            btImport = new JButton("Import", icImport);
            btImport.setToolTipText("Import current table from excel or csv file");
            btExport = new JButton("Export", icExport);
            btExport.setToolTipText("Export current table to excel or csv file");
            btBackup = new JButton("Backup", icBackup);
            btBackup.setToolTipText("Backup current database to sqlite file");
            lbStatistic = new JLabel();
            lbStatistic.setToolTipText("Get summary from all rows");
            lbStatistic.setForeground(Color.BLUE);
            cbDatabases = new JComboBox();
            cbDatabases.setToolTipText("Load database from .sqlite file");
            cbDatabases.setPreferredSize(new Dimension(40, 20));
            cbTables = new JComboBox();
            cbTables.setToolTipText("Load table from this database");
            cbTables.setPreferredSize(new Dimension(40, 20));
            cbColumns = new JComboBox();
            cbColumns.setToolTipText("Get summary from this GRP column");
            cbColumns.setPreferredSize(new Dimension(40, 20));
            // Menus
            mnGet = new JPopupMenu();
            JMenuItem mnItem = new JMenuItem("Clear", icClear);
            mnItem.setFont(smallFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btClear.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Get", icGet);
            mnItem.setFont(smallFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btGet.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Set", icSet);
            mnItem.setFont(smallFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btSet.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Insert", icInsert);
            mnItem.setFont(smallFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btInsert.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Delete", icDelete);
            mnItem.setFont(smallFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btDelete.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Import", icImport);
            mnItem.setFont(smallFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btImport.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Export", icExport);
            mnItem.setFont(smallFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btExport.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Backup", icBackup);
            mnItem.setFont(smallFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btBackup.doClick();
                }
            });
            mnGet.add(mnItem);

            // Toolbar
            tbToolbar = new JToolBar();
            tbToolbar.add(btClear);
            tbToolbar.add(btGet);
            tbToolbar.add(btSet);
            tbToolbar.add(btInsert);
            tbToolbar.add(btDelete);
            tbToolbar.add(btImport);
            tbToolbar.add(btExport);
            tbToolbar.add(btBackup);
            tbToolbar.addSeparator(new Dimension(10, 50));
            tbToolbar.add(lbStatistic);
            tbToolbar.addSeparator(new Dimension(10, 50));
            tbToolbar.add(cbDatabases);
            tbToolbar.add(cbTables);
            tbToolbar.add(cbColumns);

            // Set and pick panel
            pnSet = new JPanel();
            tbPick = new JTable();
            pnPick = new JPanel(new BorderLayout());
            pnPick.add(new JScrollPane(tbPick));

            // Set pick split panel
            spSetPick = new JSplitPane();
            spSetPick.setOrientation(JSplitPane.VERTICAL_SPLIT);
            spSetPick.setTopComponent(pnSet);
            spSetPick.setBottomComponent(pnPick);

            // Get table
            tbGet = new JTable();
            tbGet.setComponentPopupMenu(mnGet);

            // Set pick get panel
            spSetPickGet = new JSplitPane();
            spSetPickGet.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
            spSetPickGet.setLeftComponent(spSetPick);
            spSetPickGet.setRightComponent(new JScrollPane(tbGet));

            // Frame
            frame = new JFrame("Building Manager 1.0");
            frame.add(tbToolbar, BorderLayout.NORTH);
            frame.add(spSetPickGet);
            frame.setIconImage(icFrame.getImage());

            // Variables
            excelAdapter = new ExcelAdapter(tbGet);
            function = new Function();

            modelGet = new EditableTableModel();
            modelPick = new DefaultTableModel();
            currencyFormat = NumberFormat.getCurrencyInstance();
            percentageFormat = NumberFormat.getPercentInstance();
            doubleFormat = NumberFormat.getNumberInstance();
            doubleFormat.setMaximumFractionDigits(2);
            doubleFormat.setMinimumFractionDigits(2);
            dateFormat1 = new SimpleDateFormat("dd/MM/yy");
            dateFormat2 = new SimpleDateFormat("dd.MM.yy");
            function.changeFontRecursive(frame, mediumFont);
            function.setUIFont(new javax.swing.plaf.FontUIResource(mediumFont));
            map = function.file2Map("configuration.txt");
            modelGet.GST = function.parseDouble(map.get("GST"));
            // Events
            // Logic: database => table => model
            frame.addWindowListener(new WindowListener() {
                // 1. Get databases
                @Override
                public void windowOpened(WindowEvent e) {
                    try {
                        File appPath = new File(function.getAppPath());
                        File license = new File(appPath.getParent() + "/" + "license");
                        if (license.exists()) {
                            File invoiceFolder = new File("Invoice");
                            invoiceFolder.mkdir();
                            File resultFolder = new File("Result");
                            resultFolder.mkdir();
                            File backupFolder = new File("Backup");
                            backupFolder.mkdir();
                            databases = backupFolder.list();
                            for (String database : databases) {
                                cbDatabases.addItem(database);
                            }
                            if (cbDatabases.getItemCount() == 0) {
                                cbDatabases.addItem(dateFormat2.format(new Date()));
                            } else {
                            }
                            cbDatabases.setSelectedIndex(function.parseInt(map.get("databaseIndex")));
                        } else {
                            if (JOptionPane.showInputDialog("Please enter your license to run this application:").equals("Asdf!234")) {
                                license.createNewFile();
                                JOptionPane.showConfirmDialog(null, "Please restart to run the full version.", "Registered successfully", JOptionPane.DEFAULT_OPTION);
                            } else {
                                System.exit(0);
                            }
                        }
                    } catch (Exception error) {
                    }
                }

                // close
                @Override
                public void windowClosing(WindowEvent e) {
                    Database.close();
                    map.put("databaseIndex", String.valueOf(cbDatabases.getSelectedIndex()));
                    function.map2File(map, "configuration.txt");
                }

                @Override
                public void windowClosed(WindowEvent e) {
                }

                @Override
                public void windowIconified(WindowEvent e) {
                }

                @Override
                public void windowDeiconified(WindowEvent e) {
                }

                @Override
                public void windowActivated(WindowEvent e) {
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                }
            });
            // 2. Get tables
            cbDatabases.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent event) {
                    if (event.getStateChange() == ItemEvent.SELECTED) {
                        Database.close();
                        Database.open("Backup/" + cbDatabases.getSelectedItem().toString());
                        cbTables.removeAllItems();
                        tables = Database.getTables(cbDatabases.getSelectedItem().toString());
                        for (String table : tables) {
                            cbTables.addItem(table);
                        }
                    }
                }
            });
            // 3. Get input controls, pick columns
            cbTables.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent event) {
                    if (event.getStateChange() == ItemEvent.SELECTED) {
                        // Remove and add input controls
                        pnSet.removeAll();
                        modelGet.getColumns(cbDatabases.getSelectedItem().toString(), cbTables.getSelectedItem().toString());
                        pnSet.setLayout(new GridLayout(modelGet.columns.length, 2));
                        for (int i = 0; i < modelGet.columns.length; ++i) {
                            JLabel lbSet = new JLabel(modelGet.columns[i]);
                            lbSet.setFont(smallFont);
                            lbSet.setForeground(Color.red);
                            pnSet.add(lbSet);
                            String[] values = Database.getValuesByColumn(modelGet.table, modelGet.columns[i]);
                            JTextFieldX tfSet = new JTextFieldX(values);
                            tfSet.setFont(smallFont);
                            pnSet.add(tfSet);
                            // Invoice file
                            if (modelGet.columns[i].equals("FILE")) {
                                tfSet.addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mouseClicked(MouseEvent e) {
                                        // Open file
                                        if (e.getClickCount() == 2) {
                                            JFileChooser fileChooser = new JFileChooser();
                                            fileChooser.setDialogTitle("Select the invoice file");
                                            fileChooser.setCurrentDirectory(new File(function.getAppPath()));
                                            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.pdf", "pdf"));
                                            fileChooser.setAcceptAllFileFilterUsed(true);
                                            int result = fileChooser.showSaveDialog(null);
                                            if (result == JFileChooser.APPROVE_OPTION) {
                                                tfSet.setText(fileChooser.getSelectedFile().getAbsolutePath());
                                            }
                                        }
                                        else{}
                                    }
                                });
                            } else {
                            }
                        }

                        // Add pick columns
                        String[] pickColumns;
                        if (modelGet.table.equals("BUY")) {
                            pickColumns = new String[]{"GRP", "AMT", "GST", "PAID"};
                            modelPick = new DefaultTableModel(null, pickColumns) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return false;
                                }
                            };
                        } else if (modelGet.table.equals("SELL")) {
                            pickColumns = new String[]{"JOB", "AMT", "QTT", "GST"};
                            modelPick = new DefaultTableModel(null, pickColumns) {
                                @Override
                                public boolean isCellEditable(int row, int column) {
                                    return true;
                                }
                            };
                            modelPick.addTableModelListener(new TableModelListener() {
                                @Override
                                public void tableChanged(TableModelEvent e) {
                                    try {
                                        if (modelGet.table.equals("SELL")) {
                                            String items = "";
                                            String value = "";
                                            for (int i = 0; i < tbPick.getRowCount(); ++i) {
                                                for (int j = 0; j < tbPick.getColumnCount(); ++j) {
                                                    value = tbPick.getValueAt(i, j).toString().replace("\b", "");
                                                    items += value + "\f";
                                                }
                                                items += "\b";
                                            }
                                            items = items.replace("\f\b", "\b");
                                            if (!items.equals("")) {
                                                items = items.substring(0, items.length() - 1);
                                            }
                                            ((JTextFieldX) pnSet.getComponent(modelGet.getColumnCount() * 2 - 1)).setText(items);
                                            double totalAMT = 0;
                                            double totalGST = 0;
                                            double amt = 0, qtt = 0, gst = 0;
                                            for (int i = 0; i < tbPick.getRowCount(); ++i) {
                                                amt = function.parseDouble(tbPick.getValueAt(i, 1).toString());
                                                qtt = function.parseDouble(tbPick.getValueAt(i, 2).toString());
                                                gst = function.parseDouble(tbPick.getValueAt(i, 3).toString());
                                                totalAMT += amt * qtt * (1 + gst);
                                                totalGST += amt * qtt * gst;
                                            }
                                            ((JTextFieldX) pnSet.getComponent(7 * 2 + +1)).setText(String.valueOf(totalAMT));
                                            ((JTextFieldX) pnSet.getComponent(8 * 2 + +1)).setText(String.valueOf(totalGST));

                                        } else {
                                        }
                                    } catch (Exception error) {
                                    }
                                }
                            });
                        }
                        spSetPick.setDividerLocation(pnSet.getPreferredSize().height / (double) spSetPick.getSize().height);
                        tbPick.setModel(modelPick);
                        tbPick.createDefaultColumnsFromModel();

                        // Add combobox, tooltips
                        cbColumns.removeAllItems();
                        modelGet.columnEditables[9] = false;
                        if (modelGet.table.equals("BUY")) {
                            cbColumns.addItem("ADR");
                            cbColumns.addItem("SELLER");
                            cbColumns.addItem("CAT");
                            modelGet.toolTips = new String[]{"Which row?", "Which job?", "Who sell items", "Which tax category?", "Which items?", "Which invoice number?", "Which issued date?", "How much amount with GST?", "How much GST?", "Invoice file", "Which paid date?", "How to pay?"};

                        } else {
                            modelGet.columnEditables[11] = false;
                            modelGet.toolTips = new String[]{"ID", "Which customer address?", "Which customer?", "Which customer category?", "Which customer description?", "Which invoice number?", "Which issued date?", "How much amount with GST?", "How much GST?", "Which invoice file?", "Which paid date?", "Which items?"};
                        }
                        modelGet.columnAligns[6] = 1;
                        modelGet.columnAligns[10] = 1;
                        modelGet.columnAligns[7] = 2;
                        modelGet.columnAligns[8] = 2;
                        modelGet.columnFormats[7] = "^([+-]?([0-9]+\\.)?[0-9]+)$";
                        modelGet.columnFormats[8] = "^([+-]?([0-9]+\\.)?[0-9]+)$";
                        modelGet.columnFormats[6] = "^[0-3][0-9]/[0-3][0-9]/[0-9]{2}$";
                        modelGet.columnFormats[10] = "^[0-3][0-9]/[0-3][0-9]/[0-9]{2}$";
                        modelGet.getRows(getInputValues());
                        tbGet.setModel(modelGet);
                        tbGet.createDefaultColumnsFromModel();
                        tbGet.getColumnModel().getColumn(0).setPreferredWidth(40);
                    }
                }
            });
            cbColumns.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent event) {
                    if (event.getStateChange() == ItemEvent.SELECTED) {
                        modelPick.setRowCount(0);
                        List<String[]> rows = Database.resultSet2Rows(Database.getSummary(cbColumns.getSelectedItem().toString()));
                        for (String[] row : rows) {
                            modelPick.addRow(row);
                        }
                    } else {
                    }
                }
            });
            // 4. Get input values, pick values
            tbGet.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    try {
                        if (e.getValueIsAdjusting() == false) {
                            getInputPickValues(tbGet.convertRowIndexToModel(tbGet.getSelectedRow()), tbGet.convertColumnIndexToModel(tbGet.getSelectedColumn()));
                        } else {
                        }
                    } catch (Exception error) {
                    }
                }
            });
            modelGet.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    lbStatistic.setText("Amount: " + doubleFormat.format(modelGet.amount) + " GST: " + doubleFormat.format(modelGet.gst) + " Paid: " + doubleFormat.format(modelGet.paid));
                    getInputPickValues(e.getFirstRow(), e.getColumn());
                }
            });

            // 5. Render(align, color, format) and sort get and pick tables
            tbGet.setDefaultRenderer(Object.class,
                    new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (modelGet.columnFormats[column].equals("^([+-]?([0-9]+\\.)?[0-9]+)$")) {
                        c = (JComponent) super.getTableCellRendererComponent(table, doubleFormat.format(Double.parseDouble(value.toString())), isSelected, hasFocus, row, column);
                    } else {
                    }
                    // allignment
                    if (modelGet.columnAligns[column] == 0) {
                        ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                    } else if (modelGet.columnAligns[column] == 1) {
                        ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                    } else {
                        ((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);
                    }
                    // tooltip
                    c.setToolTipText((String) value);
                    // color
                    // selected cell
                    if (isSelected) {
                        c.setBackground(Color.LIGHT_GRAY);
                    } else if (!modelGet.getValueAt(tbGet.convertRowIndexToModel(row), 10).equals("")) {
                        c.setBackground(Color.CYAN);
                    } else // Not paid yet
                    {
                        try {
                            Date date = dateFormat1.parse(modelGet.getValueAt(tbGet.convertRowIndexToModel(row), 6).toString());
                            long milies = new Date().getTime() - date.getTime();
                            long days = TimeUnit.DAYS.convert(milies, TimeUnit.MILLISECONDS);
                            if (days >= function.parseInt(map.get("dueTime"))) {
                                c.setBackground(Color.ORANGE);
                            } else {
                                c.setBackground(Color.WHITE);
                            }
                        } catch (Exception error) {
                            c.setBackground(Color.WHITE);
                        }
                    }
                    tbGet.revalidate();
                    return c;
                }
            });
            TableCellRenderer header = tbGet.getTableHeader().getDefaultRenderer();
            tbGet.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table,
                        Object value, boolean isSelected, boolean hasFocus, int row,
                        int column) {
                    Component tableCellRendererComponent = header.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    ((JComponent) tableCellRendererComponent).setToolTipText(modelGet.toolTips[column]);
                    return tableCellRendererComponent;
                }
            });
            tbPick.setDefaultRenderer(Object.class,
                    new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
                    // format
                    Component c = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    try {
                        if (column == 1 || column == 2) {
                            c = renderer.getTableCellRendererComponent(table, doubleFormat.format(Double.parseDouble(value.toString())), isSelected, hasFocus, row, column);
                        } else if (column == 3) {
                            if (modelGet.table.equals("SELL")) {
                                c = renderer.getTableCellRendererComponent(table, percentageFormat.format(Double.parseDouble(value.toString())), isSelected, hasFocus, row, column);
                            } else if (modelGet.table.equals("BUY")) {
                                c = renderer.getTableCellRendererComponent(table, doubleFormat.format(Double.parseDouble(value.toString())), isSelected, hasFocus, row, column);
                            } else {
                            }
                        } else {
                        }
                        // allignment
                        if (column == 0) {
                            ((JLabel) c).setHorizontalAlignment(JLabel.LEFT);
                        } else {
                            ((JLabel) c).setHorizontalAlignment(JLabel.RIGHT);
                        }
                        // color
                        // select cell
                        if (isSelected) {
                            c.setBackground(Color.LIGHT_GRAY);
                        } else // Outcome
                        if (modelGet.table.equals("BUY") == true) {
                            // Paid already
                            row = tbPick.convertRowIndexToModel(row);
                            if (modelPick.getValueAt(row, 1).equals(modelPick.getValueAt(row, 3))) {
                                c.setBackground(Color.CYAN);
                            } else // Not paid yet
                            {
                                c.setBackground(Color.WHITE);
                            }
                        } else // Income
                        {
                            c.setBackground(Color.WHITE);
                        }
                        tbGet.revalidate();
                    } catch (Exception e) {
                    }
                    return c;
                }
            }
            );
            function.sort(tbGet);
            function.sort(tbPick);

            // 6. Open file
            tbGet.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    int column = tbGet.columnAtPoint(e.getPoint());
                    int row = tbGet.rowAtPoint(e.getPoint());
                    if (column >= 0 && row >= 0) {
                        if (modelGet.columns[column].equals("FILE")) {
                            if (e.getClickCount() == 2) {
                                File file = new File(tbGet.getValueAt(row, column).toString());
                                if (file.isFile()) {
                                    Function.open(file, true);
                                } else {
                                    JFileChooser fileChooser = new JFileChooser();
                                    fileChooser.setDialogTitle("Select the invoice file");
                                    fileChooser.setCurrentDirectory(new File(function.getAppPath()));
                                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PDF files", "pdf"));
                                    fileChooser.setAcceptAllFileFilterUsed(true);
                                    int result = fileChooser.showOpenDialog(null);
                                    if (result == JFileChooser.APPROVE_OPTION) {
                                        modelGet.setValueAt(fileChooser.getSelectedFile().getAbsolutePath(), row, column);
                                    }
                                }
                            } else {
                            }
                        } else {
                        }
                    } else {
                    }
                }
            });

            // 7. Clear input values
            btClear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearInputPickValues();
                }
            });
            // 8. Get get values with input values
            btGet.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        modelGet.getRows(getInputValues());
                    } catch (Exception error) {
                    }
                }
            });

            // 9. Set selected row with input values and id
            btSet.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (JOptionPane.showConfirmDialog(null, "Do you want to set input values into these selected rows?", "Set", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            modelGet.setRows(function.view2ModelIndices(tbGet), getInputValues());
                        } else {
                        }
                    } catch (Exception error) {
                    }
                }
            });
            // 10. Insert a number of rows with input values
            btInsert.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String[] inputs = getInputValues();
                    if (inputs[7].equals("")) {
                        inputs[7] = "0";
                    }
                    if (inputs[8].equals("")) {
                        inputs[8] = "0";
                    }
                    inputs[0] = "";
                    if (modelGet.table.equals("SELL")) {
                        if (inputs[11].equals("")) {
                            inputs[11] = "\f\f1\fx\b\f\f1\fx\b\f\f1\fx\b\f\f1\fx".replace("x", map.get("GST"));
                        }
                    } else {
                    }
                    String input = JOptionPane.showInputDialog("How many rows do you want to insert ?", 1);
                    int number = function.parseInt(input);
                    modelGet.insertRows(inputs, number);
                }
            });
            // 11. Delete get values
            tbGet.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(final java.awt.event.KeyEvent evt) {
                    if (evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_D) {
                        btDelete.doClick();
                    } else {
                    }
                }
            });
            btDelete.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (tbGet.getSelectedRowCount() == -1) {
                        if (JOptionPane.showConfirmDialog(null, "Do you want to delete all of these rows?", "Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            modelGet.deleteRows(getInputValues());
                        } else {
                        }

                    } else {
                        if (JOptionPane.showConfirmDialog(null, "Do you want to delete these selected rows?", "Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            modelGet.deleteRows(function.view2ModelIndices(tbGet));
                        } else {
                        }
                    }
                }
            }
            );
            // 12. Import get rows
            btImport.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setCurrentDirectory(new File(function.getAppPath() + "/Result"));
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel files", "xls"));
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
                        fileChooser.setAcceptAllFileFilterUsed(true);
                        int result = fileChooser.showOpenDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            modelGet.importFile(fileChooser.getSelectedFile());
                        }
                    } catch (Exception error) {
                    }
                }
            });
            // 13. Export get rows
            btExport.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setCurrentDirectory(new File(function.getAppPath() + "/Result"));
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel files", "xls"));
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
                        fileChooser.setAcceptAllFileFilterUsed(true);
                        int result = fileChooser.showSaveDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            modelGet.exportFile(fileChooser.getSelectedFile());
                            function.open(fileChooser.getSelectedFile(), true);
                        }
                    } catch (Exception error) {
                    }
                }
            });
            // 14. Backup get rows
            btBackup.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        File source = new File("Backup/" + modelGet.database);
                        String targetFile = dateFormat2.format(new Date());
                        File target = new File("Backup/" + targetFile);
                        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        cbDatabases.addItem(targetFile);
                    } catch (Exception error) {
                    }
                }
            });
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
            frame.pack();
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } catch (Exception error) {
        }
    }
}
