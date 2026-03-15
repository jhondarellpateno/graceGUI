package config;

import java.awt.Image;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Base64;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import net.proteanit.sql.DbUtils;

public class config {

    public Connection con;

    // Constructor: Initializes the connection automatically
    public config() {
        con = connectDB();
    }

    public static Connection connectDB() {
        try {
            Class.forName("org.sqlite.JDBC");
            Connection con = DriverManager.getConnection("jdbc:sqlite:data.db");
            return con;
        } catch (Exception e) {
            System.out.println("Connection Failed: " + e);
            return null;
        }
    }

    // Single universal method for getting counts or sums
    public String getSingleValue(String sql, Object... params) {
        String value = "0";
        try (PreparedStatement pst = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                pst.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    value = rs.getString(1);
                    if (value == null) value = "0";
                }
            }
        } catch (SQLException e) {
            System.out.println("Error getting value: " + e.getMessage());
        }
        return value;
    }

    public void addRecord(String sql, Object... values) {
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding record: " + e.getMessage());
        }
    }

    public java.util.List<java.util.Map<String, Object>> fetchRecords(String sqlQuery, Object... values) {
        java.util.List<java.util.Map<String, Object>> records = new java.util.ArrayList<>();
        try (PreparedStatement pstmt = con.prepareStatement(sqlQuery)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    java.util.Map<String, Object> row = new java.util.HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnName(i), rs.getObject(i));
                    }
                    records.add(row);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching records: " + e.getMessage());
        }
        return records;
    }

    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            return null;
        }
    }

    public void displayData(String sql, javax.swing.JTable table, Object... values) {
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                table.setModel(DbUtils.resultSetToTableModel(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error filtering data: " + e.getMessage());
        }
    }

    public void updateRecord(String sql, Object... values) {
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            pstmt.executeUpdate();
            System.out.println("Record updated successfully!");
        } catch (SQLException e) {
            System.out.println("Error updating record: " + e.getMessage());
        }
    }

    public void deleteRecord(String sql, Object... values) {
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                pstmt.setObject(i + 1, values[i]);
            }
            pstmt.executeUpdate();
            System.out.println("Record deleted successfully!");
        } catch (SQLException e) {
            System.out.println("Error deleting record: " + e.getMessage());
        }
    }

    public void setProfileIcon(JLabel label, String path) {
        if (path == null || path.isEmpty()) {
            label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/profile.png")));
            return;
        }
        try {
            ImageIcon icon = new ImageIcon(path);
            Image img = icon.getImage().getScaledInstance(label.getWidth(), label.getHeight(), Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            label.setIcon(new javax.swing.ImageIcon(getClass().getResource("/image/profile.png")));
        }
    }
}