package org.example;

import org.example.entities.Paiement;
import org.example.services.PaiementService;

import java.sql.Date;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        PaiementService ps = new PaiementService();
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\n=== GESTION DES PAIEMENTS ===");
            System.out.println("1. Ajouter un paiement");
            System.out.println("2. Afficher les paiements");
            System.out.println("3. Quitter");
            System.out.print("Votre choix : ");
            
            while (!scanner.hasNextInt()) {
                System.out.println("Veuillez entrer un nombre valide !");
                scanner.next();
            }
            choice = scanner.nextInt();
            scanner.nextLine(); // Consommer la nouvelle ligne

            switch (choice) {
                case 1:
                    System.out.println("\n-- Ajout d'un nouveau paiement --");
                    // Nom du client supprimé car non présent en base
                    
                    System.out.print("Montant : ");
                    while (!scanner.hasNextDouble()) {
                        System.out.println("Montant invalide !");
                        scanner.next();
                    }
                    double montant = scanner.nextDouble();
                    scanner.nextLine();
                    
                    // Pour simplifier, on prend la date actuelle
                    Date date = new Date(System.currentTimeMillis());
                    
                    System.out.print("Statut du paiement (ex: Effectué, En attente, Annulé) : ");
                    String statut = scanner.nextLine();

                    Paiement p = new Paiement(montant, date, statut);
                    ps.ajouter(p);
                    break;

                case 2:
                    System.out.println("\n-- Liste des paiements --");
                    ps.afficher().forEach(System.out::println);
                    break;

                case 3:
                    System.out.println("Au revoir !");
                    break;

                default:
                    System.out.println("Choix invalide !");
            }
        } while (choice != 3);
        
        scanner.close();
    }
}
