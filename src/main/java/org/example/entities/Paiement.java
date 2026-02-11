package org.example.entities;

import java.sql.Date;

public class Paiement {
    private int idPaiement;
    private double montant;
    private Date datePaiement;
    private String statutPaiement;
    private String methodePaiement;

    public Paiement() {
    }

    public Paiement(int idPaiement, double montant, Date datePaiement, String statutPaiement, String methodePaiement) {
        this.idPaiement = idPaiement;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statutPaiement = statutPaiement;
        this.methodePaiement = methodePaiement;
    }

    public Paiement(double montant, Date datePaiement, String statutPaiement, String methodePaiement) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statutPaiement = statutPaiement;
        this.methodePaiement = methodePaiement;
    }

    public Paiement(double montant, Date datePaiement, String statutPaiement) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statutPaiement = statutPaiement;
        this.methodePaiement = "Carte Bancaire";
    }

    public int getIdPaiement() {
        return idPaiement;
    }

    public void setIdPaiement(int idPaiement) {
        this.idPaiement = idPaiement;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public Date getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(Date datePaiement) {
        this.datePaiement = datePaiement;
    }

    public String getStatutPaiement() {
        return statutPaiement;
    }

    public void setStatutPaiement(String statutPaiement) {
        this.statutPaiement = statutPaiement;
    }

    public String getMethodePaiement() {
        return methodePaiement;
    }

    public void setMethodePaiement(String methodePaiement) {
        this.methodePaiement = methodePaiement;
    }

    public String getFormattedAmount(boolean isEuro) {
        if (isEuro) {
            double amountInEuro = montant / 3.4;
            return String.format("%.2f €", amountInEuro);
        }
        return String.format("%.2f DT", montant);
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "idPaiement=" + idPaiement +
                ", montant=" + montant +
                ", datePaiement=" + datePaiement +
                ", statutPaiement='" + statutPaiement + '\'' +
                ", methodePaiement='" + methodePaiement + '\'' +
                '}';
    }
}
