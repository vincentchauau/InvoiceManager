/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package invoicemanager;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
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
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.UIManager;
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
    public static int getOperatingSystemType() {
        String OS = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if ((OS.indexOf("mac") >= 0) || (OS.indexOf("darwin") >= 0)) {
            return 0;
        } else if (OS.indexOf("win") >= 0) {
            return 1;
        } else if (OS.indexOf("nux") >= 0 || OS.indexOf("nix") >= 0) {
            return 2;
        } else {
            return 3;
        }
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

    public static boolean open(File file, boolean isFile) {
        try {
            if (file.isFile() == isFile) {
                if (getOperatingSystemType() == 1) {
                    Runtime.getRuntime().exec(new String[]{"rundll32", "url.dll,FileProtocolHandler",
                        file.getAbsolutePath()});
                    return true;
                } else if (getOperatingSystemType() == 0 || getOperatingSystemType() == 2) {
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
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);
        table.getTableHeader().setForeground(Color.BLUE);
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
    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    // Setup font for containers
    public static void changeFontRecursive(Container root, Font font) {
        for (Component c : root.getComponents()) {
            c.setFont(font);
            if (c instanceof Container) {
                changeFontRecursive((Container) c, font);
            }
        }
    }
}
