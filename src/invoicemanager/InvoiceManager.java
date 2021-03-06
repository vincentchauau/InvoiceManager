package invoicemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventObject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.DefaultCellEditor;
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
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
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
    private static double GST;
    private static String operatingSystem;
    private static EditableTableModel modelGet;
    private static DefaultTableModel modelPick;
    private static SimpleDateFormat dateFormat1;
    private static SimpleDateFormat dateFormat2;
    private static NumberFormat doubleFormat;
    private static NumberFormat percentageFormat;
    private static ExcelAdapter excelAdapter;

    // Clear inputs
    private static void clearSetValues() {
        for (int i = 0; i < modelGet.getColumnCount(); ++i) {
            ((JTextFieldX) pnSet.getComponent(i * 2 + 1)).setText("");
        }
    }

    // Get inputs
    private static String[] getSetValues() {
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
    private static void getSetPickValues(int r) {
        String[] values = modelGet.rows.get(r);
        for (int i = 0; i < modelGet.columns.length; ++i) {
            ((JTextFieldX) pnSet.getComponent(i * 2 + 1)).setText(values[i]);
        }
        List<String[]> rows;
        switch (modelGet.table) {
            case "OUTPUT":
                String value = modelGet.getValueAt(r, modelGet.getColumnCount() - 1).toString();
                modelPick.setRowCount(0);
                rows = Function.string2Array(value);
                for (String[] row : rows) {
                    modelPick.addRow(row);
                }
                break;
            case "INPUT":
                modelPick.setRowCount(0);
                rows = Database.resultSet2Rows(Database.getSummary(cbColumns.getSelectedItem().toString()));
                for (String[] row : rows) {
                    modelPick.addRow(row);
                }
                break;
            default:
                break;
        }
    }

    public static void main(String args[]) {
        try {
            // Resource
            map = Function.file2Map("configuration.txt");
            Font defaultFont = new Font("Serif", Font.PLAIN, Function.parseInt(map.get("defaultFont")));
            Font headerFont = new Font("Serif", Font.PLAIN, Function.parseInt(map.get("headerFont")));
            Font labelFont = new Font("Serif", Font.PLAIN, Function.parseInt(map.get("labelFont")));
            Color defaultColor = new Color(Function.parseInt(map.get("defaultColor")));
            Color headerColor = new Color(Function.parseInt(map.get("headerColor")));
            Color labelColor = new Color(Function.parseInt(map.get("labelColor")));
            GST = Function.parseDouble(map.get("GST"));
            operatingSystem = map.get("operatingSystem");
            ImageIcon icClear = new ImageIcon("Icon/clear.png");
            ImageIcon icGet = new ImageIcon("Icon/get.png");
            ImageIcon icSet = new ImageIcon("Icon/set.png");
            ImageIcon icInsert = new ImageIcon("Icon/insert.png");
            ImageIcon icDelete = new ImageIcon("Icon/delete.png");
            ImageIcon icImport = new ImageIcon("Icon/import.png");
            ImageIcon icExport = new ImageIcon("Icon/export.png");
            ImageIcon icBackup = new ImageIcon("Icon/backup.png");
            ImageIcon icFrame = new ImageIcon("Icon/frame.png");
            // Interfaces
            Function.setFontColor(defaultFont, defaultColor);
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
            lbStatistic.setForeground(labelColor);
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
            mnGet.setFont(defaultFont);
            JMenuItem mnItem = new JMenuItem("Clear", icClear);
            mnItem.setFont(defaultFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btClear.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Get", icGet);
            mnItem.setFont(defaultFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btGet.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Set", icSet);
            mnItem.setFont(defaultFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btSet.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Insert", icInsert);
            mnItem.setFont(defaultFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btInsert.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Delete", icDelete);
            mnItem.setFont(defaultFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btDelete.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Import", icImport);
            mnItem.setFont(defaultFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btImport.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Export", icExport);
            mnItem.setFont(defaultFont);
            mnItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    btExport.doClick();
                }
            });
            mnGet.add(mnItem);
            mnItem = new JMenuItem("Backup", icBackup);
            mnItem.setFont(defaultFont);
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
            tbPick.getTableHeader().setFont(headerFont);
            tbPick.getTableHeader().setForeground(headerColor);
            modelPick = new DefaultTableModel(null, new String[]{"Col 1", "Col 2", "Col 3", "Col 4"}) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return true;
                }
            };
            tbPick.setModel(modelPick);
            pnPick = new JPanel(new BorderLayout());
            pnPick.add(new JScrollPane(tbPick));

            // Set pick split panel
            spSetPick = new JSplitPane();
            spSetPick.setOrientation(JSplitPane.VERTICAL_SPLIT);
            spSetPick.setTopComponent(pnSet);
            spSetPick.setBottomComponent(pnPick);

            // Get table
            tbGet = new JTable();
            modelGet = new EditableTableModel();
            tbGet.getTableHeader().setFont(headerFont);
            tbGet.getTableHeader().setForeground(headerColor);
            tbGet.setCellSelectionEnabled(true);
            tbGet.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
            percentageFormat = NumberFormat.getPercentInstance();
            doubleFormat = NumberFormat.getNumberInstance();
            doubleFormat.setMaximumFractionDigits(2);
            doubleFormat.setMinimumFractionDigits(2);
            dateFormat1 = new SimpleDateFormat("dd/MM/yy");
            dateFormat2 = new SimpleDateFormat("dd.MM.yy");
            // Events
            // Logic: database => table => model
            frame.addWindowListener(new WindowAdapter() {
                // 1. Get databases
                @Override
                public void windowOpened(WindowEvent e) {
                    try {
                        File appPath = new File(Function.getAppPath());
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
                            cbDatabases.setSelectedIndex(Function.parseInt(map.get("databaseIndex")));
                        } else {
                            if (JOptionPane.showInputDialog("Please enter your license to run this application:").equals("Asdf!234")) {
                                license.createNewFile();
                                JOptionPane.showConfirmDialog(null, "Please restart to run the full version.", "Registered successfully", JOptionPane.DEFAULT_OPTION);
                                System.exit(0);
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
                    Function.map2File(map, "configuration.txt");
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
                        cbColumns.removeAllItems();
                        pnSet.removeAll();
                        modelGet.getColumns(cbDatabases.getSelectedItem().toString(), cbTables.getSelectedItem().toString());
                        tbGet.setModel(modelGet);
                        tbGet.getColumnModel().getColumn(0).setPreferredWidth(40);
                        pnSet.setLayout(new GridLayout(modelGet.columns.length, 2));
                        for (int i = 0; i < modelGet.columns.length; ++i) {
                            JLabel lbSet = new JLabel(modelGet.columns[i]);
                            JTextFieldX tfSet;
                            JTextFieldX tfColumnCell;
                            lbSet.setForeground(labelColor);
                            pnSet.add(lbSet);
                            if (i >= 1 && i <= 3) {
                                tfColumnCell = new JTextFieldX(modelGet.autoCompleteColumns.get(i));
                                tfSet = new JTextFieldX(Database.getValuesByColumn(modelGet.table, modelGet.columns[i]));
                                cbColumns.addItem(modelGet.columns[i]);
                            } else {
                                tfColumnCell = new JTextFieldX(new String[]{});
                                tfSet = new JTextFieldX(new String[]{});
                            }
                            tbGet.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(tfColumnCell) {
                                public boolean isCellEditable(EventObject evt) {
                                    if (evt instanceof MouseEvent) {
                                        if (((MouseEvent) evt).getClickCount() == 2) {
                                            return true;
                                        } else {
                                            return false;
                                        }
                                    } else if (evt instanceof KeyEvent) {
                                        SwingUtilities.invokeLater(new Runnable() {
                                            public void run() {
                                                getComponent().requestFocusInWindow();
                                            }
                                        });
                                    }
                                    return true;
                                }
                            });
                            pnSet.add(tfSet);
                            // Invoice file
                            if (i == 7) {
                                tfSet.addKeyListener(new KeyAdapter() {
                                    @Override
                                    public void keyPressed(KeyEvent e) {
                                        // Default action
                                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                                            double gst = Function.parseDouble(tfSet.getText()) * GST;
                                            ((JTextFieldX) pnSet.getComponent(8 * 2 + 1)).setText(String.valueOf(gst));
                                        } else {
                                        }
                                    }
                                });
                            }
                            if (i == 9) {
                                tfSet.addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mouseClicked(MouseEvent e) {
                                        // Open file
                                        if (e.getClickCount() == 2) {
                                            File file = new File(tfSet.getText());
                                            Function.open(file);
                                            if (!file.isFile()) {
                                                if (operatingSystem.equals("win")) {
                                                    JFileChooser fileChooser = new JFileChooser();
                                                    fileChooser.setCurrentDirectory(new File("."));
                                                    fileChooser.setDialogTitle("Invoice");
                                                    fileChooser.setMultiSelectionEnabled(false);
                                                    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                                                    fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.pdf", "pdf"));
                                                    fileChooser.setAcceptAllFileFilterUsed(true);
                                                    int result = fileChooser.showOpenDialog(null);
                                                    if (result == JFileChooser.APPROVE_OPTION) {
                                                        tfSet.setText(fileChooser.getSelectedFile().getAbsolutePath());
                                                    }
                                                } else if (operatingSystem.equals("mac") || operatingSystem.equals("linux")) {
                                                    FileDialog fileDialog = new FileDialog((JFrame) null);
                                                    fileDialog.setDirectory(".");
                                                    fileDialog.setTitle("Invoice");
                                                    fileDialog.setMultipleMode(false);
                                                    fileDialog.setFilenameFilter(new FilenameFilter() {
                                                        public boolean accept(File dir, String name) {
                                                            return name.toLowerCase().endsWith(".pdf");
                                                        }
                                                    });
                                                    fileDialog.setFile("*.pdf");
                                                    fileDialog.setMode(FileDialog.LOAD);
                                                    fileDialog.setVisible(true);
                                                    if (fileDialog.getDirectory() != null && fileDialog.getFile() != null) {
                                                        tfSet.setText(fileDialog.getDirectory() + fileDialog.getFile());
                                                    }
                                                } else {
                                                }
                                            }
                                        } else {
                                        }
                                    }
                                });
                            } else {
                            }
                        }
                        spSetPick.setDividerLocation(pnSet.getPreferredSize().height / (double) spSetPick.getSize().height);
                        // Add pick columns
                        if (modelGet.table.equals("INPUT")) {
                            Function.setColumns(tbPick, new String[]{"GRP", "AMT", "GST", "PAID"});
                        } else if (modelGet.table.equals("OUTPUT")) {
                            Function.setColumns(tbPick, new String[]{"JOB", "AMT", "QTT", "GST"});
                        }
                        modelPick.setRowCount(0);

                        // Add combobox, tooltips
                        modelGet.columnEditables[9] = false;
                        if (modelGet.table.equals("INPUT")) {
                            modelGet.toolTips = new String[]{"Which row?", "Which address?", "Which supplier?", "Which tax category?", "Which items?", "Which invoice number?", "Which issued date?", "How much amount with GST?", "How much GST?", "Which invoice file", "Which paid date?", "How to pay?"};

                        } else {
                            modelGet.columnEditables[11] = false;
                            modelGet.toolTips = new String[]{"Which row?", "Which address?", "Which customer?", "Which customer category?", "Which customer description?", "Which invoice number?", "Which issued date?", "How much amount with GST?", "How much GST?", "Which invoice file?", "Which paid date?", "Which items?"};
                        }
                        modelGet.columnAligns[6] = 1;
                        modelGet.columnAligns[7] = 2;
                        modelGet.columnAligns[8] = 2;
                        modelGet.columnAligns[10] = 1;
                        modelGet.columnFormats[6] = "^[0-3][0-9]/[0-3][0-9]/[0-9]{2}$";
                        modelGet.columnFormats[7] = "^([+-]?([0-9]+\\.)?[0-9]+)$";
                        modelGet.columnFormats[8] = "^([+-]?([0-9]+\\.)?[0-9]+)$";
                        modelGet.columnFormats[10] = "^[0-3][0-9]/[0-3][0-9]/[0-9]{2}$";
                        modelGet.getRows(getSetValues());
                    }
                }
            });
            cbColumns.addItemListener(new ItemListener() {
                @Override
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
                        if (!e.getValueIsAdjusting()) {
                            getSetPickValues(tbGet.convertRowIndexToModel(tbGet.getSelectedRow()));
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
                    // SELECT, UPDATE => getSelectedRow, INSERT, DELETE => get FirstRow 
                    if (e.getType() == TableModelEvent.INSERT || e.getType() == TableModelEvent.DELETE) {
                        if (tbGet.getRowCount() > 0) {
                            getSetPickValues(tbGet.convertRowIndexToModel(0));
                        } else {
                            modelPick.setRowCount(0);
                        }
                    } else {
                        if (tbGet.getSelectedRow() != -1) {
                            getSetPickValues(tbGet.convertRowIndexToModel(tbGet.getSelectedRow()));
                        }
                    }
                }
            });
            // 5. Render(align, color, format) and sort get and pick tables
            tbGet.setDefaultRenderer(String.class,
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
                    // c.setToolTipText((String) value);
                    // color
                    if (isSelected) // selected cells
                    {
                        c.setBackground(Color.LIGHT_GRAY);
                    } else if (!modelGet.getValueAt(tbGet.convertRowIndexToModel(row), 10).equals("")) {
                        c.setBackground(Color.WHITE);
                    } else // Not paid yet
                    {
                        try {
                            Date date = dateFormat1.parse(modelGet.getValueAt(tbGet.convertRowIndexToModel(row), 6).toString());
                            long milies = new Date().getTime() - date.getTime();
                            long days = TimeUnit.DAYS.convert(milies, TimeUnit.MILLISECONDS);
                            if (days >= Function.parseInt(map.get("dueTime"))) {
                                c.setBackground(Color.ORANGE);
                            } else {
                                c.setBackground(Color.CYAN);
                            }
                        } catch (Exception error) {
                            c.setBackground(Color.CYAN);
                        }
                    }
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
            Function.sort(tbGet);
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
                                Function.open(file);
                            } else {
                            }
                        } else {
                        }
                    } else {
                    }
                }
            });
            // PICK events
            modelPick.addTableModelListener(new TableModelListener() {
                @Override
                public void tableChanged(TableModelEvent e) {
                    try {
                        if (modelGet.table.equals("OUTPUT")) {
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
                                amt = Function.parseDouble(tbPick.getValueAt(i, 1).toString());
                                qtt = Function.parseDouble(tbPick.getValueAt(i, 2).toString());
                                gst = Function.parseDouble(tbPick.getValueAt(i, 3).toString());
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
                            if (modelGet.table.equals("OUTPUT")) {
                                c = renderer.getTableCellRendererComponent(table, percentageFormat.format(Double.parseDouble(value.toString())), isSelected, hasFocus, row, column);
                            } else if (modelGet.table.equals("INPUT")) {
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
                        if (modelGet.table.equals("INPUT") == true) {
                            // Paid already
                            row = tbPick.convertRowIndexToModel(row);
                            if (modelPick.getValueAt(row, 1).equals(modelPick.getValueAt(row, 3))) {
                                c.setBackground(Color.WHITE);
                            } else // Not paid yet
                            {
                                c.setBackground(Color.CYAN);
                            }
                        } else // Income
                        {
                            c.setBackground(Color.CYAN);
                        }
                        tbGet.revalidate();
                    } catch (Exception e) {
                    }
                    return c;
                }
            }
            );
            tbPick.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    try {
                        if (e.getValueIsAdjusting() == false) {
                            if (modelGet.table.equals("OUTPUT")) {
                            } else {
                                String value = tbPick.getValueAt(tbPick.getSelectedRow(), 0).toString();
                                String grp = cbColumns.getSelectedItem().toString();
                                clearSetValues();
                                for (int i = 0; i < pnSet.getComponentCount() / 2; ++i) {
                                    if (((JLabel) pnSet.getComponent(i * 2)).getText().equals(grp)) {
                                        ((JTextFieldX) pnSet.getComponent(i * 2 + 1)).setText(value);
                                    } else {
                                    }
                                }
                                modelGet.getRows(getSetValues());
                            }
                        } else {
                        }
                    } catch (Exception error) {
                    }
                }
            });
            Function.sort(tbPick);
            // 7. Clear input values
            btClear.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    clearSetValues();
                }
            });
            // 8. Get get values with input values
            btGet.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        modelGet.getRows(getSetValues());
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
                            modelGet.setRows(Function.view2ModelIndices(tbGet), getSetValues());
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
                    String[] inputs = getSetValues();
                    if (inputs[7].equals("")) {
                        inputs[7] = "0";
                    }
                    if (inputs[8].equals("")) {
                        inputs[8] = "0";
                    }
                    inputs[0] = "";
                    if (modelGet.table.equals("OUTPUT")) {
                        if (inputs[11].equals("")) {
                            inputs[11] = "\f\f1\fx\b\f\f1\fx\b\f\f1\fx\b\f\f1\fx".replace("x", map.get("GST"));
                        }
                    } else {
                    }
                    String input = JOptionPane.showInputDialog("How many rows do you want to insert ?", 1);
                    int number = Function.parseInt(input);
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
                            modelGet.deleteRows(getSetValues());
                        } else {
                        }

                    } else {
                        if (JOptionPane.showConfirmDialog(null, "Do you want to delete these selected rows?", "Delete", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                            modelGet.deleteRows(Function.view2ModelIndices(tbGet));
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
                    if (operatingSystem.equals("win")) {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setCurrentDirectory(new File("./Result"));
                        fileChooser.setDialogTitle("Import");
                        fileChooser.setMultiSelectionEnabled(false);
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.xls", "xls"));
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.csv", "csv"));
                        fileChooser.setAcceptAllFileFilterUsed(true);
                        int result = fileChooser.showOpenDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            modelGet.importFile(fileChooser.getSelectedFile());
                        }
                    } else if (operatingSystem.equals("mac") || operatingSystem.equals("linux")) {
                        FileDialog fileDialog = new FileDialog((JFrame) null);
                        fileDialog.setDirectory("./Result");
                        fileDialog.setTitle("Import");
                        fileDialog.setMultipleMode(false);
                        fileDialog.setFilenameFilter(new FilenameFilter() {
                            public boolean accept(File file, String name) {
                                return name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith("*.*");
                            }
                        });
                        fileDialog.setFile("*.pdf");
                        fileDialog.setMode(FileDialog.LOAD);
                        fileDialog.setVisible(true);
                        modelGet.importFile(new File(fileDialog.getDirectory() + fileDialog.getFile()));
                    } else {
                    }
                }
            });
            // 13. Export get rows
            btExport.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    File file;
                    if (operatingSystem.equals("win")) {
                        JFileChooser fileChooser = new JFileChooser();
                        fileChooser.setCurrentDirectory(new File("./Result"));
                        fileChooser.setDialogTitle("Export");
                        fileChooser.setMultiSelectionEnabled(false);
                        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.xls", "xls"));
                        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("*.csv", "csv"));
                        fileChooser.setAcceptAllFileFilterUsed(true);
                        int result = fileChooser.showSaveDialog(null);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            file = fileChooser.getSelectedFile();
                            modelGet.exportFile(file);
                            Function.open(file);
                        }
                    } else if (operatingSystem.equals("mac") || operatingSystem.equals("linux")) {
                        FileDialog fileDialog = new FileDialog((JFrame) null);
                        fileDialog.setDirectory("./Result");
                        fileDialog.setTitle("Export");
                        fileDialog.setMultipleMode(false);
                        fileDialog.setFilenameFilter(new FilenameFilter() {
                            public boolean accept(File file, String name) {
                                return name.toLowerCase().endsWith(".xls") || name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith("*.*");
                            }
                        });
                        fileDialog.setFile("*.xls;*.csv");
                        fileDialog.setMode(FileDialog.SAVE);
                        fileDialog.setVisible(true);
                        file = new File(fileDialog.getDirectory() + fileDialog.getFile());
                        modelGet.exportFile(file);
                        Function.open(file);
                    } else {
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
            frame.pack();
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        } catch (Exception error) {
        }
    }
}
