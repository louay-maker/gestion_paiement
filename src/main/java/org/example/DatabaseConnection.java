package org.example;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/gestion_paiement";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // ← METTEZ VOTRE MOT DE PASSE ICI

    public static void testConnection() {
        try {
            System.out.println("=== TEST DE CONNEXION MYSQL ===");
            System.out.println("URL: " + URL);
            System.out.println("User: " + USER);

            // 1. Charger le driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ Driver MySQL chargé");

            // 2. Établir la connexion
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✅ Connexion à MySQL établie!");

            // 3. Vérifier la base de données
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery("SHOW TABLES LIKE 'paiements'");

            if (rs.next()) {
                System.out.println("✅ Table 'paiements' existe");
            } else {
                System.out.println("⚠️ Table 'paiements' n'existe pas");
                System.out.println("Exécutez le script SQL pour la créer");
            }

            // 4. Fermer la connexion
            conn.close();
            System.out.println("✅ Connexion fermée proprement");
            System.out.println("=== TEST RÉUSSI ===");

        } catch (ClassNotFoundException e) {
            System.err.println("❌ ERREUR: Driver MySQL non trouvé!");
            System.err.println("Solution: Vérifiez que mysql-connector-java est dans pom.xml");
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println("❌ ERREUR: Connexion impossible!");
            System.err.println("Cause: " + e.getMessage());
            System.err.println("\nVÉRIFIEZ QUE:");
            System.err.println("1. MySQL Server est démarré");
            System.err.println("2. La base 'gestion_paiement' existe");
            System.err.println("3. Le mot de passe est correct");
            System.err.println("4. Le port 3306 est accessible");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}