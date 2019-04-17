/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package invoicemanager;

import java.io.File;
import java.nio.file.Files;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Database {

    private static String sql;
    private static Connection connection;
    private static Statement statement;

    // 1. Open connection
    public static boolean open(String database) {
        try {
            // Connect database
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + database);
            statement = connection.createStatement();
            // Create table
            // BUY table: what you buy: id, add, seller, cat, des, inv, idate, amt with gst, gst, file, pdate, note
            sql = "CREATE TABLE IF NOT EXISTS BUY(ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ADR TEXT DEFAULT '', SELLER TEXT DEFAULT '', CAT TEXT DEFAULT '', DES TEXT DEFAULT '', INV TEXT DEFAULT '', IDATE TEXT DEFAULT '', AMT REAL DEFAULT 0, GST REAL DEFAULT 0, FILE TEXT DEFAULT '', PDATE TEXT DEFAULT '', NOTE TEXT DEFAULT '')";
            statement.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS OLDBUY";
            statement.executeUpdate(sql);
            sql = "ALTER TABLE BUY RENAME TO OLDBUY";
            statement.executeUpdate(sql);
            sql = "CREATE TABLE BUY(ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ADR TEXT DEFAULT '', SELLER TEXT DEFAULT '', CAT TEXT DEFAULT '', DES TEXT DEFAULT '', INV TEXT DEFAULT '', IDATE TEXT DEFAULT '', AMT REAL DEFAULT 0, GST REAL DEFAULT 0, FILE TEXT DEFAULT '', PDATE TEXT DEFAULT '', NOTE TEXT DEFAULT '')";
            statement.executeUpdate(sql);
            sql = "INSERT INTO BUY(ADR, SELLER, CAT, DES, INV, IDATE, AMT, GST, FILE, PDATE, NOTE) SELECT ADR, SELLER, CAT, DES, INV, IDATE, AMT, GST, FILE, PDATE, NOTE FROM OLDBUY ORDER BY ID";
            statement.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS OLDBUY";
            statement.executeUpdate(sql);

            // SELL table: what you sell: id, add, buyer, cat, iden, inv, idate, amt with gst, gst, file, pdate, items
            // items: job, amt without gst, qtt, gst
            sql = "CREATE TABLE IF NOT EXISTS SELL(ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ADR TEXT DEFAULT '', BUYER TEXT DEFAULT '', CAT TEXT DEFAULT '', DES TEXT DEFAULT '', INV TEXT DEFAULT '', IDATE TEXT DEFAULT '', AMT REAL DEFAULT 0, GST REAL DEFAULT 0, FILE TEXT DEFAULT '', PDATE TEXT DEFAULT '', ITEMS TEXT DEFAULT '\f\f\f\b\f\f\f\b\f\f\f\b\f\f')";
            statement.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS OLDSELL";
            statement.executeUpdate(sql);
            sql = "ALTER TABLE SELL RENAME TO OLDSELL";
            statement.executeUpdate(sql);
            sql = "CREATE TABLE SELL(ID INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, ADR TEXT DEFAULT '', BUYER TEXT DEFAULT '', CAT TEXT DEFAULT '', DES TEXT DEFAULT '', INV TEXT DEFAULT '', IDATE TEXT DEFAULT '', AMT REAL DEFAULT 0, GST REAL DEFAULT 0, FILE TEXT DEFAULT '', PDATE TEXT DEFAULT '', ITEMS TEXT DEFAULT '\f\f\f\b\f\f\f\b\f\f\f\b\f\f')";
            statement.executeUpdate(sql);
            sql = "INSERT INTO SELL(ADR, BUYER, CAT, DES, INV, IDATE, AMT, GST, FILE, PDATE, ITEMS) SELECT ADR, BUYER, CAT, DES, INV, IDATE, AMT, GST, FILE, PDATE, ITEMS FROM OLDSELL ORDER BY ID";
            statement.executeUpdate(sql);
            sql = "DROP TABLE IF EXISTS OLDSELL";
            statement.executeUpdate(sql);
            return true;
        } catch (Exception error) {
            return false;
        }
    }

    // 2. Close connection
    public static void close() {
        try {
            connection.close();
        } catch (Exception error) {
        }
    }

    // 3. Get databases
    // catalog, schema, table, types/column
    public static String[] getDatabases() {
        List<String> databases = new ArrayList<>();
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getCatalogs();
            while (rs.next()) {
                databases.add(rs.getString("TABLE_CAT"));
            }
            rs.close();
        } catch (Exception error) {
        }
        return databases.toArray(new String[0]);
    }

    // 4. Get tables of a database
    public static String[] getTables(String database) {
        List<String> tables = new ArrayList<>();
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getTables(database, null, null, null);
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } catch (Exception error) {
        }
        return tables.toArray(new String[0]);
    }

    // 5. Get columns of a table of a database
    public static String[] getColumns(String database, String table) {
        List<String> columns = new ArrayList<>();
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getColumns(database, null, table, "%");
            while (rs.next()) {
                columns.add(rs.getString("COLUMN_NAME"));
            }
            rs.close();
        } catch (Exception error) {
        }
        return columns.toArray(new String[0]);
    }

    // 6. Get column types of columns of a table of a database
    public static String[] getColumnTypes(String database, String table) {
        List<String> columns = new ArrayList<>();
        try {
            DatabaseMetaData meta = connection.getMetaData();
            ResultSet rs = meta.getColumns(database, null, table, "%");
            while (rs.next()) {
                columns.add(rs.getString("TYPE_NAME"));
            }
            rs.close();
        } catch (Exception error) {
        }
        return columns.toArray(new String[0]);
    }
    // Date to string: dd/MM/yyyy to yyyy/MM/dd
    public static String reverseDateString(String date, boolean hyphenate)
    {
        if (hyphenate)
            return String.format("(SUBSTR('%s',7,2)||SUBSTR('%s',4,2)||SUBSTR('%s',1,2))",date,date,date);
        else
            return String.format("(SUBSTR(%s,7,2)||SUBSTR(%s,4,2)||SUBSTR(%s,1,2))",date,date,date);
    }
    // 7. Where string: WHERE COLUMN1=VALUE1 AND COLUMN2=VALUE2 AND ...
    public static String whereRowString(String id) {
        if (!id.equals(""))
            return " WHERE ID=" + id;
        else return "";
    }

    public static String whereRowString(String[] columns, String[] types, String[] values) {
        String sql = "";
        String[] tokens;
        for (int i = 0; i < columns.length; ++i) {
            // having values
            if (!values[i].equals("")) {
                tokens = values[i].split(";", 2);
                if (tokens.length == 1) {
                    if (values[i].startsWith("\"") && values[i].endsWith("\"")) //exact
                    {
                        if (types[i].equals("TEXT")) // text
                        {
                            sql += columns[i] + "='" + values[i].substring(1, values[i].length()-1).replace("'", "''") + "' AND ";
                        } else // number
                        {
                            sql += columns[i] + "=" + values[i].substring(1, values[i].length()-1).replace("'", "''") + " AND ";
                        }

                    } else // approximate 
                    {
                        if (types[i].equals("TEXT")) // text
                        {
                            sql += columns[i] + " LIKE '%" + values[i].replace("'", "''") + "%' AND ";
                        } else // number
                        {
                            sql += "CAST(" + columns[i] + " AS CHAR) LIKE '%" + values[i].replace("'", "''") + "%' AND ";
                        }
                    }

                } else {
                    if (types[i].equals("TEXT")) // text
                    {
                        if (tokens[0].matches("^[0-3][0-9]/[0-3][0-9]/[0-9]{2}$"))
                            sql += reverseDateString(columns[i],false) + ">=" + reverseDateString(tokens[0],true) + " AND " + reverseDateString(columns[i],false) + "<=" + reverseDateString(tokens[1],true) + " AND ";
                        else sql += columns[i] + ">='" + tokens[0].replace("'", "''") + "' AND " + columns[i] + "<='" + tokens[1].replace("'", "''") + "' AND ";
                    } else // number
                    {
                        sql += columns[i] + ">=" + tokens[0].replace("'", "''") + " AND " + columns[i] + "<=" + tokens[1].replace("'", "''") + " AND ";
                    }
                }
            } else {
            }
        }
        if (!sql.equals("")) {
            sql = " WHERE " + sql.substring(0, sql.length() - 5);
        } else {
        }
        return sql;
    }

    // 8. Set string: COLUMN1=VALUE1,COLUMN2=VALUE2,...
    public static String setRowString(String[] columns, String[] types, String[] values) {
        String sql = "";
        for (int i = 0; i < columns.length; i++) 
        {
            if (types[i].equals("TEXT"))//text
            {
                sql += columns[i] + "='" + values[i].replace("'", "''") + "',";
            } else // number
            {
                sql += columns[i] + "=" + values[i].replace("'", "''") + ",";
            }
        }
        if (sql.endsWith(",") == true) {
            sql = sql.substring(0, sql.length() - 1);
        } else {
        }
        return sql;
    }

    // 9. Insert string: (COLUMN1,COLUMN2,...) VALUES (VALUE1,VALUE2,...)
    public static String insertRowString(String[] columns, String[] types, String[] values) {
        String sql = "";
        String column = "";
        String value = "";
        for (int i = 0; i < columns.length; ++i) {
            if (!values[i].equals("")) {
                column += columns[i] + ",";
                if (types[i].equals("TEXT")) {
                    value += "'" + values[i].replace("'", "''") + "',";
                } else {
                    value += "" + values[i].replace("'", "''") + ",";
                }
            } else {
            }
        }
        if (column.endsWith(",")) {
            sql = "(" + column.substring(0, column.length() - 1) + ")" + " VALUES (" + value.substring(0, value.length() - 1) + ")";
        } else {
        }
        return sql;
    }

    // 11. Get rows with input values
    public static ResultSet getRows(String table, String[] columns, String[] types, String[] values) {
        try {
            sql = "SELECT * FROM [" + table + "]" + whereRowString(columns, types, values);
            return statement.executeQuery(sql);
        } catch (Exception error) {
        }
        return null;
    }

    public static List<String[]> resultSet2Rows(ResultSet rs) {
        List<String[]> rows = new ArrayList<>();
        try {
            int columnCount = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = rs.getString(i + 1);
                }
                rows.add(row);
            }
        } catch (Exception error) {
        }
        return rows;
    }

    public static String[] getValuesByColumn(String table, String column) {
        Set<String> set = new HashSet<>();
        try {
            sql = "SELECT DISTINCT " + column + " FROM [" + table + "] WHERE " + column + "<> ''";
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                set.add(resultSet.getString(column));
            }
        } catch (Exception error) {
        }
        return set.toArray(new String[0]);
    }

    // 11. Set row with input values and id
    public static boolean setRow(String table, String[] columns, String[] types, String[] values, String id) {
        try {
            sql = "UPDATE [" + table + "] SET " + setRowString(columns, types, values) + whereRowString(id);
            statement.executeUpdate(sql);
            return true;
        } catch (Exception error) {
            return false;
        }
    }
    public static boolean setRow(String table, String[] columns, String[] types, int column, String value, String id) {
        try {
            if (types[column].equals("TEXT"))
            {
                sql = "UPDATE [" + table + "] SET " + columns[column] +"='" + value +"'" + whereRowString(id);
            }
            else
            {
                sql = "UPDATE [" + table + "] SET " + columns[column] +"=" + value + whereRowString(id);
            }
            statement.executeUpdate(sql);
            return true;
        } catch (Exception error) {
            return false;
        }
    }
    public static int insertRow(String table, String[] columns, String[] types, String[] values) {
        try {
            String insertRowString = insertRowString(columns, types, values);
            if (insertRowString.equals("")) {
                insertRowString = " DEFAULT VALUES";
            } else {
            }
            sql = "INSERT INTO [" + table + "]" + insertRowString;
            statement.executeUpdate(sql);
            sql = "SELECT LAST_INSERT_ROWID()";
            ResultSet rs = statement.executeQuery(sql);
            rs.next();
            return rs.getInt(1);
        } catch (Exception error) {
            return -1;
        }
    }

    // Delete rows with input values
    public static boolean deleteRows(String table, String[] columns, String[] types, String[] values) {
        try {
            sql = "DELETE FROM [" + table + "] " + whereRowString(columns, types, values);
            statement.executeUpdate(sql);
            return true;
        } catch (Exception error) {
            return false;
        }
    }
    // 13. Delete a selected row with id
    public static boolean deleteRow(String table, String[] columns, String[] types, String id) {
        try {
            sql = "DELETE FROM [" + table + "] " + whereRowString(id);
            statement.executeUpdate(sql);
            return true;
        } catch (Exception error) {
            return false;
        }
    }

    public static ResultSet getSummary(String group) {
        try {
            sql = "SELECT T1.GRP, T1.AMT, T1.GST, COALESCE(T2.PAID,0.0) FROM (SELECT A.GRP, SUM(A.AMT) AS AMT, SUM(A.GST) AS GST FROM BUY AS A GROUP BY A.GRP) AS T1 LEFT JOIN (SELECT B.GRP, SUM(B.AMT) AS PAID FROM BUY AS B WHERE B.PDATE<>'' GROUP BY B.GRP) AS T2 ON T1.GRP=T2.GRP".replace("GRP", group);
            return statement.executeQuery(sql);
        } catch (Exception e) {
        }
        return null;
    }

}
