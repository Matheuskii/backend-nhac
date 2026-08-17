package br.com.nhac.backend_nhac;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class CleanupFlyway {
    public static void main(String[] args) {
        String url = "jdbc:mariadb://localhost:3306/nhac_db";
        String user = "root";
        String password = "senai2026";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            String sql = "DELETE FROM flyway_schema_history WHERE version = '014' OR success = 0";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                int affectedRows = pstmt.executeUpdate();
                System.out.println("Deleted " + affectedRows + " rows from flyway_schema_history.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
