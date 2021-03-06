/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package invoicemanager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.JobName;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 *
 * @author vincentchauau
 */
public class Function {
    private static Random random = new Random();
    // 1. System functions
    // Get the operating system names
    public static String getOperatingSystemType() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            return "mac";
        } else if (OS.indexOf("win") >= 0) {
            return "win";
        } else if (OS.indexOf("nux") >= 0 || OS.indexOf("nix") >= 0) {
            return "linux";
        } else {
            return "other";
        }
    }
    public static boolean isPrintableKeyChar(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return (!Character.isISOControl(c))
                && c != KeyEvent.CHAR_UNDEFINED
                && block != null
                && block != Character.UnicodeBlock.SPECIALS;
    }
    // Print a pdf file
    public static boolean print(String pdf) {
        File file = new File(pdf);
        if (file.exists()) {
            FileInputStream stream = null;
            try {
                PrinterJob printerJob = PrinterJob.getPrinterJob();
                PrintService printService = null;
                if (printerJob.printDialog()) {
                    printService = printerJob.getPrintService();
                }
                DocPrintJob printJob = printService.createPrintJob();
                stream = new FileInputStream(pdf);
                Doc pdfDoc = new SimpleDoc(stream, DocFlavor.INPUT_STREAM.AUTOSENSE, null);
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                attributes.add(new JobName(pdf, Locale.getDefault()));
                printJob.print(pdfDoc, attributes);
                stream.close();
                return true;
            } catch (Exception ex) {
            }
        } else {
        }
        return false;
    }

    public static List<String> getPaths(String path) {
        File file = new File(path);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        return Arrays.asList(directories);
    }

    public static String getAppPath() {
        //System.out.println(System.getProperty("user.dir"));
        //System.out.println(Function.class.getClassLoader().getResource("").getPath());
        //System.out.println(Function.class.getProtectionDomain().getCodeSource().getLocation().getFile());
        //System.out.println(Paths.get("").toAbsolutePath().toString());
        //System.out.println(FileSystems.getDefault().getPath("").toAbsolutePath().toString());
        return new File("").getAbsoluteFile().getPath();
    }

    public static boolean open(File file) {
        try {
            if (file.isFile() == true) {
                if (getOperatingSystemType().equals("win")) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler",
                        file.getAbsolutePath()});
                    return true;
                } else if (getOperatingSystemType().equals("mac") || getOperatingSystemType().equals("linux")) {
                    Runtime.getRuntime().exec(new String[]{"/usr/bin/open",
                        file.getAbsolutePath()});
                    return true;
                } else if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    // 2. Data functions
    public static Map<String, String> file2Map(String file) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            String[] tokens;
            while ((line = reader.readLine())!= null)
            {
                tokens = line.split(":");
                map.put(tokens[0], tokens[1]);
            }
        } catch (Exception ex) {
        }
        return map;
    }
    public static void map2File(Map<String, String> map, String file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (String key: map.keySet())
            {
                writer.write(key + ":" + map.get(key) + "\r\n");
            }
            writer.close();
        } catch (Exception ex) {
        }
    }
    public static void table2CSV(JTable table, File file) {
        try {
            TableModel model = table.getModel();
            FileWriter excel = new FileWriter(file);
            for (int i = 0; i < model.getColumnCount(); i++) {
                excel.write(model.getColumnName(i) + ",");
            }
            excel.write("\n");
            for (int i = 0; i < model.getRowCount(); i++) {
                for (int j = 0; j < model.getColumnCount(); j++) {
                    excel.write(model.getValueAt(i, j).toString().replace(",", "_") + ",");
                }
                excel.write("\n");
            }
            excel.close();
        } catch (Exception e) {
        }
    }

    public static List<String[]> string2Array(String value) {
        // Escape character: \r\n\t,\b\f\v
        List<String[]> items = new ArrayList<>();
        String[] rows = value.split("\b");
        for (String row : rows) {
            items.add(row.split("\f", -1));
        }
        return items;
    }

    public static String[] file2List(String file) {
        try {
            return Files.readAllLines(new File(file).toPath()).toArray(new String[0]);
        } catch (Exception ex) {
            return new String[0];
        }
    }
    public static void list2File(List<String> list, String fileName) {
        FileOutputStream stream = null;
        try {
            File file = new File(fileName);
            stream = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(stream));
            for (String path : list) {
                bw.write(fileName);
                bw.newLine();
            }
        } catch (Exception ex) {
        }
    }
    public static boolean checkType(String value, String type) {
        switch (type) {
            case "number":
                return value.matches("^([+-]?(\\\\d+\\\\.)?\\\\d+)$");
            case "shortdate":
                return value.matches("^[0-3][0-9]/[0-3][0-9]/[0-9]{2}$");
            case "longdate":
                return value.matches("^[0-3][0-9]/[0-3][0-9]/[0-9]{4}$");
            case "date":
                return value.matches("^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}$");
            default:
                return false;
        }
    }
    public static double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }
    public static int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    // 3. Component functions
    public static void autoResize(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn tableColumn = table.getColumnModel().getColumn(column);
            int preferredWidth = tableColumn.getMinWidth();
            int maxWidth = tableColumn.getMaxWidth();
            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
                Component c = table.prepareRenderer(cellRenderer, row, column);
                int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
                preferredWidth = Math.max(preferredWidth, width);
                if (preferredWidth >= maxWidth) {
                    preferredWidth = maxWidth;
                    break;
                }
            }
            if (preferredWidth < 100) {
                preferredWidth = 100;
            } else {
            }
            tableColumn.setPreferredWidth(preferredWidth);
        }
    }

    public static void sort(JTable table) {
        table.getTableHeader().setReorderingAllowed(false);
        table.setRowHeight(20);
        table.setAutoCreateRowSorter(true);
        TableRowSorter sorter = new TableRowSorter(table.getModel());
        table.setRowSorter(sorter);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            private SortOrder currentOrder = SortOrder.UNSORTED;

            @Override
            public void mouseClicked(MouseEvent e) {
                int column = table.getTableHeader().columnAtPoint(e.getPoint());
                RowSorter sorter = table.getRowSorter();
                List sortKeys = new ArrayList();
                switch (currentOrder) {
                    case UNSORTED:
                        sortKeys.add(new RowSorter.SortKey(column, currentOrder = SortOrder.ASCENDING));
                        break;
                    case ASCENDING:
                        sortKeys.add(new RowSorter.SortKey(column, currentOrder = SortOrder.DESCENDING));
                        break;
                    case DESCENDING:
                        sortKeys.add(new RowSorter.SortKey(column, currentOrder = SortOrder.UNSORTED));
                        break;
                }
                sorter.setSortKeys(sortKeys);
            }
        });
    }
    public static int[] view2ModelIndices(JTable table) {
        int[] modelIndices = table.getSelectedRows();
        for (int i = 0; i < table.getSelectedRowCount(); ++i) {
            modelIndices[i] = table.convertRowIndexToModel(table.getSelectedRows()[i]);
        }
        return modelIndices;
    }

    // Setup font for controls
    public static void setFontColor(Font font, Color foreGround) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                UIManager.put(key, font);
            }
            if (key.toString().toLowerCase().contains("foreground")) {
                UIManager.put(key, foreGround);
            }
        }
    }

    // Setup font for containers
    public static void setFontColor(Container root, Font font, Color color) {
        for (Component c : root.getComponents()) {
            c.setFont(font);
            c.setForeground(color);
            if (c instanceof Container) {
                setFontColor((Container) c, font, color);
            }
        }
    }
    public static void autoComplete(JTextField tfInput, ArrayList<String> items) {
        JComboBox cbInput = new JComboBox() {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 0);
            }
        };
        cbInput.putClientProperty("adjusting", false);
        for (String item : items) {
            cbInput.addItem(item);
        }
        cbInput.setSelectedItem(null);
        cbInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!(Boolean) cbInput.getClientProperty("adjusting")) {
                    if (cbInput.getSelectedItem() != null) {
                        tfInput.setText(cbInput.getSelectedItem().toString());
                    }
                }
            }
        });

        tfInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                cbInput.putClientProperty("adjusting", true);
                if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.setSource(cbInput);
                    cbInput.dispatchEvent(e);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tfInput.setText(cbInput.getSelectedItem().toString());
                    cbInput.setPopupVisible(false);
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cbInput.setPopupVisible(false);
                } else {
                }
                cbInput.putClientProperty("adjusting", false);
            }
        });
        tfInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                getAutoCompleteData();
            }

            public void removeUpdate(DocumentEvent e) {
                getAutoCompleteData();
            }

            public void changedUpdate(DocumentEvent e) {
            }

            private void getAutoCompleteData() {
                cbInput.putClientProperty("adjusting", true);
                cbInput.removeAllItems();
                String input = tfInput.getText();
                if (!input.isEmpty()) {
                    for (String item : items) {
                        if (item.toLowerCase().startsWith(input.toLowerCase())) {
                            cbInput.addItem(item);
                        }
                    }
                }
                cbInput.setPopupVisible(cbInput.getItemCount() > 0);
                cbInput.putClientProperty("adjusting", false);
            }
        });
        tfInput.setLayout(new BorderLayout());
        tfInput.add(cbInput, BorderLayout.SOUTH);
    }
    public static Component getPreviousComponent() {
        Component c = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
        Container root = c.getFocusCycleRootAncestor();
        FocusTraversalPolicy policy = root.getFocusTraversalPolicy();
        Component prevFocus = policy.getComponentBefore(root, c);
        if (prevFocus == null) {
            prevFocus = policy.getDefaultComponent(root);
        }
        return prevFocus;
    }
    public static Component getCurrentComponent()
    {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
    }
    public static void focusDebug() {
        // java.awt.focus.DefaultKeyboardFocusManager
        // java.awt.focus.Component
    Logger focusLog = Logger.getLogger("java.awt.focus.DefaultKeyboardFocusManager");
    focusLog.setLevel(Level.ALL);
    ConsoleHandler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    focusLog.addHandler(handler);
    }
    public static void setColumns(JTable table, String[] columns)
    {
        for (int i = 0; i < columns.length; ++i)
        {
            table.getColumnModel().getColumn(i).setHeaderValue(columns[i]);
            table.repaint();
        }
    }
}
