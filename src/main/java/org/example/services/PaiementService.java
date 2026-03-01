package org.example.services;

import org.example.DatabaseConnection;
import org.example.entities.Paiement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PaiementService {

    private Connection conn;

    public PaiementService() {
        try {
            this.conn = DatabaseConnection.getConnection();
            if (this.conn == null) {
                throw new SQLException("La connexion à la base de données a échoué (conn est null).");
            }
            ensureColumnExists(); // Automatically update schema
        } catch (Exception e) {
            System.err.println("Erreur de connexion à la base de données: " + e.getMessage());
        }
    }

    private void ensureColumnExists() {
        try (Statement st = conn.createStatement()) {
            // Check if column exists
            ResultSet rs = conn.getMetaData().getColumns(null, null, "paiements", "methode_paiement");
            if (!rs.next()) {
                System.out.println("⚠️ Column 'methode_paiement' missing. Adding it now...");
                st.execute("ALTER TABLE paiements ADD COLUMN methode_paiement VARCHAR(50) DEFAULT 'Carte Bancaire'");
                System.out.println("✅ Column added successfully!");
            }
            // Improved check for stripe_session_id
            try {
                st.executeQuery("SELECT stripe_session_id FROM paiements LIMIT 1");
            } catch (SQLException e) {
                System.out.println("⚠️ Column 'stripe_session_id' missing. Adding it now...");
                st.execute("ALTER TABLE paiements ADD COLUMN stripe_session_id VARCHAR(255)");
            }

            // Improved check for user_id
            try {
                st.executeQuery("SELECT user_id FROM paiements LIMIT 1");
            } catch (SQLException e) {
                System.out.println("⚠️ Column 'user_id' missing. Adding it now...");
                st.execute("ALTER TABLE paiements ADD COLUMN user_id INT");
            }
        } catch (SQLException e) {
            System.err.println("❌ Error updating schema: " + e.getMessage());
        }
    }

    public void ajouter(Paiement p) {
        if (conn == null) {
            System.err.println("❌ Erreur : Impossible d'ajouter le paiement (connexion JDBC null)");
            return;
        }
        String req = "INSERT INTO paiements (montant, date_paiement, statut_paiement, methode_paiement, stripe_session_id, user_id) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(req);
            ps.setDouble(1, p.getMontant());
            ps.setDate(2, p.getDatePaiement());
            ps.setString(3, p.getStatutPaiement());
            ps.setString(4, p.getMethodePaiement());
            ps.setString(5, p.getStripeSessionId());
            ps.setInt(6, p.getUserId());
            
            ps.executeUpdate();
            System.out.println("✅ Paiement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du paiement : " + e.getMessage());
        }
    }


    public void supprimer(int id) {
        if (conn == null) {
            System.err.println("❌ Erreur : Impossible de supprimer le paiement (connexion JDBC null)");
            return;
        }
        String req = "DELETE FROM paiements WHERE id_paiement = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("✅ Paiement supprimé avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public void modifier(Paiement p) {
        if (conn == null) {
            System.err.println("❌ Erreur : Impossible de modifier le paiement (connexion JDBC null)");
            return;
        }
        String req = "UPDATE paiements SET montant = ?, date_paiement = ?, statut_paiement = ?, methode_paiement = ?, stripe_session_id = ? WHERE id_paiement = ?";
        try {
            // Check original status to trigger cashback only once
            String checkStatusReq = "SELECT statut_paiement FROM paiements WHERE id_paiement = ?";
            PreparedStatement checkSt = conn.prepareStatement(checkStatusReq);
            checkSt.setInt(1, p.getIdPaiement());
            ResultSet rsStatus = checkSt.executeQuery();
            String oldStatus = rsStatus.next() ? rsStatus.getString(1) : "";

            PreparedStatement ps = conn.prepareStatement(req);
            ps.setDouble(1, p.getMontant());
            ps.setDate(2, p.getDatePaiement());
            ps.setString(3, p.getStatutPaiement());
            ps.setString(4, p.getMethodePaiement());
            ps.setString(5, p.getStripeSessionId());
            ps.setInt(6, p.getIdPaiement());
            ps.executeUpdate();

            // Cashback logic: 5% if status changes to "Effectué"
            if ("Effectué".equalsIgnoreCase(p.getStatutPaiement()) && !"Effectué".equalsIgnoreCase(oldStatus)) {
                double cashback = p.getMontant() * 0.05;
                new UserService().updateBalance(p.getUserId(), cashback);
                System.out.println("💰 Cashback de " + cashback + " ajouté pour l'utilisateur " + p.getUserId());
            }

            // --- TRIGGER EMAIL NOTIFICATION ---
            if ("Effectué".equalsIgnoreCase(p.getStatutPaiement()) && !"Effectué".equalsIgnoreCase(oldStatus)) {
                org.example.entities.User user = new UserService().getUserById(p.getUserId());
                if (user != null && user.getEmail() != null) {
                    EmailService.sendPaymentConfirmation(
                        user.getEmail(), 
                        user.getName(), 
                        p.getMontant(), 
                        "DT"
                    );
                }
            }

            System.out.println("✅ Paiement modifié avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    public List<Paiement> afficher() {
        List<Paiement> paiements = new ArrayList<>();
        if (conn == null) {
            System.err.println("❌ Erreur : Impossible d'afficher les paiements (connexion JDBC null)");
            return paiements;
        }
        String req = "SELECT * FROM paiements";
        try {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                Paiement p = new Paiement();
                p.setIdPaiement(rs.getInt("id_paiement"));
                p.setMontant(rs.getDouble("montant"));
                p.setDatePaiement(rs.getDate("date_paiement"));
                p.setStatutPaiement(rs.getString("statut_paiement"));
                p.setMethodePaiement(rs.getString("methode_paiement"));
                p.setStripeSessionId(rs.getString("stripe_session_id"));
                p.setUserId(rs.getInt("user_id"));
                paiements.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des paiements : " + e.getMessage());
        }
        return paiements;
    }
}
