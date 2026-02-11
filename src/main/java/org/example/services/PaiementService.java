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
        } catch (SQLException e) {
            System.err.println("❌ Error updating schema: " + e.getMessage());
        }
    }

    public void ajouter(Paiement p) {
        String req = "INSERT INTO paiements (montant, date_paiement, statut_paiement, methode_paiement) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(req);
            ps.setDouble(1, p.getMontant());
            ps.setDate(2, p.getDatePaiement());
            ps.setString(3, p.getStatutPaiement());
            ps.setString(4, p.getMethodePaiement());
            
            ps.executeUpdate();
            System.out.println("✅ Paiement ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'ajout du paiement : " + e.getMessage());
        }
    }


    public void supprimer(int id) {
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
        String req = "UPDATE paiements SET montant = ?, date_paiement = ?, statut_paiement = ?, methode_paiement = ? WHERE id_paiement = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(req);
            ps.setDouble(1, p.getMontant());
            ps.setDate(2, p.getDatePaiement());
            ps.setString(3, p.getStatutPaiement());
            ps.setString(4, p.getMethodePaiement());
            ps.setInt(5, p.getIdPaiement());

            ps.executeUpdate();
            System.out.println("✅ Paiement modifié avec succès !");
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    public List<Paiement> afficher() {
        List<Paiement> paiements = new ArrayList<>();
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
                paiements.add(p);
            }
        } catch (SQLException e) {
            System.err.println("❌ Erreur lors de l'affichage des paiements : " + e.getMessage());
        }
        return paiements;
    }
}
