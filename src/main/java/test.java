import org.example.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class test {
    public static void main(String[] args) {
        System.out.println("=== Testing Database Connection ===");

        try {
            // Test database connection
            // DatabaseConnection is static, so we don't need getInstance()
            Connection conn = DatabaseConnection.getConnection();

            if (conn != null) {
                System.out.println("✅ Database connection successful!");

                // Create table if not exists (matching existing schema)
                createPaiementTable(conn);

                // Test CRUD operations
                testInsert(conn);
                testSelect(conn);

                // Close connection
                conn.close();
                System.out.println("✅ Connection closed successfully");
            }
        } catch (Exception e) {
            System.out.println("❌ Database connection failed!");
            e.printStackTrace();
        }
    }

    private static void createPaiementTable(Connection conn) {
        String sql = "CREATE TABLE IF NOT EXISTS paiements (" +
                "id_paiement INT PRIMARY KEY AUTO_INCREMENT," +
                "montant DECIMAL(10,2) NOT NULL," +
                "date_paiement DATE," +
                "statut_paiement VARCHAR(50)" +
                ")";

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Table 'paiements' check/creation done!");
        } catch (SQLException e) {
            System.out.println("❌ Error creating/checking table: " + e.getMessage());
        }
    }

    private static void testInsert(Connection conn) {
        String sql = "INSERT INTO paiements (montant, date_paiement, statut_paiement) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, 150.75);
            pstmt.setDate(2, java.sql.Date.valueOf("2024-02-11"));
            pstmt.setString(3, "En attente");

            int rows = pstmt.executeUpdate();
            System.out.println("✅ Inserted " + rows + " row(s) successfully!");
        } catch (SQLException e) {
            System.out.println("❌ Error inserting data: " + e.getMessage());
        }
    }

    private static void testSelect(Connection conn) {
        String sql = "SELECT * FROM paiements";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n=== Existing Payments in Database ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id_paiement") +
                        ", Montant: " + rs.getDouble("montant") +
                        ", Date: " + rs.getDate("date_paiement") +
                        ", Statut: " + rs.getString("statut_paiement"));
            }
        } catch (SQLException e) {
            System.out.println("❌ Error reading data: " + e.getMessage());
        }
    }
}