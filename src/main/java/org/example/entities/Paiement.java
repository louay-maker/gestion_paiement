package org.example.entities;

import java.sql.Date;

public class Paiement {
    private int idPaiement;
    private double montant;
    private Date datePaiement;
    private String statutPaiement;
    private String methodePaiement;

    private String stripeSessionId;
    private int userId;

    public Paiement() {
    }

    public Paiement(int idPaiement, double montant, Date datePaiement, String statutPaiement, String methodePaiement, String stripeSessionId) {
        this.idPaiement = idPaiement;
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statutPaiement = statutPaiement;
        this.methodePaiement = methodePaiement;
        this.stripeSessionId = stripeSessionId;
    }

    public Paiement(double montant, Date datePaiement, String statutPaiement, String methodePaiement, String stripeSessionId) {
        this.montant = montant;
        this.datePaiement = datePaiement;
        this.statutPaiement = statutPaiement;
        this.methodePaiement = methodePaiement;
        this.stripeSessionId = stripeSessionId;
    }

    public Paiement(double montant, Date datePaiement, String statutPaiement, String methodePaiement) {
        this(montant, datePaiement, statutPaiement, methodePaiement, null);
    }

    public Paiement(double montant, Date datePaiement, String statutPaiement) {
        this(montant, datePaiement, statutPaiement, "Carte Bancaire", null);
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

    public String getStripeSessionId() {
        return stripeSessionId;
    }

    public void setStripeSessionId(String stripeSessionId) {
        this.stripeSessionId = stripeSessionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Paiement{" +
                "idPaiement=" + idPaiement +
                ", montant=" + montant +
                ", datePaiement=" + datePaiement +
                ", statutPaiement='" + statutPaiement + '\'' +
                ", methodePaiement='" + methodePaiement + '\'' +
                ", stripeSessionId='" + stripeSessionId + '\'' +
                '}';
    }
}
