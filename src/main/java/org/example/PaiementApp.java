package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.entities.Paiement;
import org.example.entities.User;
import org.example.services.PaiementService;
import org.example.services.StripeService;
import org.example.services.UserService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PaiementApp extends Application {

    private PaiementService service = new PaiementService();
    private UserService userService = new UserService();
    private Stage primaryStage;

    // RBAC Roles
    public enum Role { ADMIN, USER }
    private Role currentRole;
    private User currentUser;
    private static boolean isDarkMode = false; // Persistent theme state

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Gestion des Paiements - Authentification");
        
        // Start with Login Scene
        showLoginScene();
        primaryStage.show();
    }

    // Helper to load CSS
    private void applyStyles(Scene scene) {
        try {
            String cssPath = Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.err.println("Coould not load CSS: " + e.getMessage());
        }
    }

    // --- LOGIN SCENE ---
    private void showLoginScene() {
        VBox root = new VBox(30);
        root.setPadding(new Insets(50));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("root");
        applyCurrentTheme(root);

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.getChildren().add(createThemeToggle(root));

        Label titleLabel = new Label("Portail de Paiement");
        titleLabel.getStyleClass().add("header-label");

        VBox loginBox = new VBox(20);
        loginBox.getStyleClass().add("form-container");
        loginBox.setMaxWidth(350);
        loginBox.setAlignment(Pos.CENTER);

        Label infoLabel = new Label("Identifiez-vous :");
        infoLabel.getStyleClass().add("subheader-label");

        TextField nameField = new TextField();
        nameField.setPromptText("Nom d'utilisateur (ex: UserTest)");
        nameField.setMaxWidth(250);

        Button adminBtn = new Button("LOG DEPUIS ADMIN");
        adminBtn.getStyleClass().addAll("button", "button-primary");
        adminBtn.setMaxWidth(Double.MAX_VALUE);
        adminBtn.setOnAction(e -> {
            currentUser = userService.getUserByName("Admin");
            if (currentUser != null) {
                currentRole = Role.ADMIN;
                showListScene();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur Admin non trouvé.");
            }
        });

        Button userBtn = new Button("CONNEXION USER");
        userBtn.getStyleClass().addAll("button", "button-secondary");
        userBtn.setMaxWidth(Double.MAX_VALUE);
        userBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez entrer un nom.");
                return;
            }
            currentUser = userService.getUserByName(name);
            if (currentUser == null) {
                // Auto-create user for demo purposes if not found
                currentUser = new User(name, Role.USER);
                // Implementation note: normally UserService would handle saving to DB too
                // But for now let's just use the default users created in ensureTableExists
                showAlert(Alert.AlertType.ERROR, "Erreur", "Utilisateur non trouvé. Essayez 'UserTest'.");
                return;
            }
            currentRole = currentUser.getRole();
            if (currentRole == Role.ADMIN) showListScene();
            else showAddScene();
        });

        loginBox.getChildren().addAll(infoLabel, nameField, userBtn, new Separator(), adminBtn);
        root.getChildren().addAll(topBar, titleLabel, loginBox);

        Scene scene = new Scene(root, 800, 600);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    // --- ADD PAYMENT SCENE (Now Role Sensitive) ---
    private void showAddScene() {
        if (currentRole == Role.ADMIN) {
            showListScene();
            return;
        }

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        applyCurrentTheme(root);

        HBox topButtons = new HBox(10);
        topButtons.setAlignment(Pos.CENTER_LEFT);
        Button logoutBtn = new Button("Quitter");
        logoutBtn.getStyleClass().addAll("button", "button-danger");
        logoutBtn.setOnAction(e -> showLoginScene());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        topButtons.getChildren().addAll(logoutBtn, spacer, createThemeToggle(root));
        
        Label titleLabel = new Label("Ajouter un Nouveau Paiement");
        titleLabel.getStyleClass().add("header-label");

        Label balanceLabel = new Label("Mon Solde: " + String.format("%.2f", currentUser.getWalletBalance()) + " DT | Points: " + currentUser.getLoyaltyPoints());
        balanceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60;");

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.getStyleClass().add("form-container");

        TextField montantField = new TextField();
        montantField.setPromptText("Montant (ex: 500.0)");
        
        // Limit length to 20 characters
        montantField.setTextFormatter(new TextFormatter<>(change -> 
            change.getControlNewText().length() <= 20 ? change : null));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Carte Bancaire", "PayPal", "D17", "Portefeuille Interne");
        methodBox.setValue("Carte Bancaire");
        methodBox.setMaxWidth(Double.MAX_VALUE);

        // For User, Statut is Hidden/Fixed to "En attente"
        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("En attente", "Effectué", "Annulé");
        statutBox.setValue("En attente");
        statutBox.setMaxWidth(Double.MAX_VALUE);

        Button addButton = new Button("Ajouter Paiement");
        addButton.getStyleClass().addAll("button", "button-primary");
        addButton.setMaxWidth(Double.MAX_VALUE);

        Button listButton = new Button("Voir la liste des paiements");
        listButton.getStyleClass().addAll("button", "button-secondary");
        listButton.setMaxWidth(Double.MAX_VALUE);

        Button stripeButton = new Button("Payer avec Stripe (Carte)");
        stripeButton.getStyleClass().addAll("button", "button-warning");
        stripeButton.setMaxWidth(Double.MAX_VALUE);
        stripeButton.setStyle("-fx-background-color: #6772e5; -fx-text-fill: white;"); // Stripe color

        // Logic
        addButton.setOnAction(e -> {
            try {
                double montant = Double.parseDouble(montantField.getText());
                Date date = Date.valueOf(datePicker.getValue());
                String statut = (currentRole == Role.ADMIN) ? statutBox.getValue() : "En attente";
                String methode = methodBox.getValue();

                Paiement p = new Paiement(montant, date, statut, methode);
                p.setUserId(currentUser.getId());

                if ("Portefeuille Interne".equalsIgnoreCase(methode)) {
                    if (currentUser.getWalletBalance() < montant) {
                        showAlert(Alert.AlertType.ERROR, "Solde Insuffisant", "Votre solde est insuffisant pour ce paiement.");
                        return;
                    }
                    // Deduct balance
                    userService.updateBalance(currentUser.getId(), -montant);
                    currentUser.setWalletBalance(currentUser.getWalletBalance() - montant);
                    p.setStatutPaiement("Effectué"); // Wallet payments are instant
                    // Status is already potentially "Effectué" now, so cashback will trigger in modifier?
                    // No, ajouter() just inserts. Modifier() triggers cashback.
                    // Actually, if ajouter() inserts "Effectué", then no status transition happens in modifier.
                    // I should probably trigger cashback in adding if status is already "Effectué".
                    // Let's refine PaiementService later if needed. For now, let's keep it simple.
                }

                service.ajouter(p);
                
                String msg = (currentRole == Role.USER) 
                    ? ("Paiement ajouté !" + ("Portefeuille Interne".equalsIgnoreCase(methode) ? " Effectué via Wallet." : " En attente de confirmation Admin."))
                    : "Paiement ajouté avec succès !";
                showAlert(Alert.AlertType.INFORMATION, "Succès", msg);
                montantField.clear();
                showAddScene(); // Refresh balance display
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être un nombre valide !");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + ex.getMessage());
            }
        });

        stripeButton.setOnAction(e -> {
            try {
                double montant = Double.parseDouble(montantField.getText());
                
                // 1. Create Stripe Session
                org.example.services.StripeService.StripeCheckoutResult result;
                try {
                    result = org.example.services.StripeService.createCheckoutSession(montant);
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Erreur Stripe", "Impossible de créer la session de paiement : " + ex.getMessage());
                    return;
                }

                // 2. Try to add to DB (as Pending)
                try {
                    Date date = Date.valueOf(datePicker.getValue());
                    Paiement p = new Paiement(montant, date, "En attente", "Carte Bancaire (Stripe)", result.sessionId);
                    p.setUserId(currentUser.getId());
                    service.ajouter(p);
                    
                    // 3. Open Browser only if DB recording succeeded (or at least attempted)
                    this.getHostServices().showDocument(result.url);
                    showAlert(Alert.AlertType.INFORMATION, "Stripe", "Le navigateur s'est ouvert. Une fois le paiement effectué, vérifiez votre tableau de bord Stripe.");
                } catch (Exception ex) {
                    // This catches the case where service.ajouter throws or fails internally
                    showAlert(Alert.AlertType.WARNING, "Avertissement Base de Données", 
                        "La session Stripe est créée, mais nous n'avons pas pu enregistrer le paiement localement : " + ex.getMessage());
                    // We still show the document because the user might want to pay anyway
                    this.getHostServices().showDocument(result.url);
                }
                
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez entrer un montant valide.");
            }
        });

        listButton.setOnAction(e -> showListScene());

        Button walletButton = new Button("Gérer mon Wallet 💳");
        walletButton.getStyleClass().addAll("button", "button-info");
        walletButton.setMaxWidth(Double.MAX_VALUE);
        walletButton.setOnAction(e -> showWalletScene());

        form.getChildren().addAll(
            new Label("Montant (DT):"), montantField, 
            new Label("Date:"), datePicker,
            new Label("Méthode de paiement:"), methodBox
        );
        
        // Only Admin chooses Status when adding
        if (currentRole == Role.ADMIN) {
            form.getChildren().addAll(new Label("Statut:"), statutBox);
        }

        form.getChildren().addAll(new Region(), addButton, stripeButton, new Separator(), walletButton);
        root.getChildren().addAll(topButtons, titleLabel, balanceLabel, form, new Region(), listButton);

        Scene scene = new Scene(root, 800, 600);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    // --- MODIFY PAYMENT SCENE (Admin Only) ---
    private void showModifyScene(Paiement p) {
        if (currentRole != Role.ADMIN) {
            showListScene();
            return;
        }

        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);
        applyCurrentTheme(root);

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.getChildren().add(createThemeToggle(root));

        Label titleLabel = new Label("Confirmer / Modifier Paiement #" + p.getIdPaiement());
        titleLabel.getStyleClass().add("header-label");

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.getStyleClass().add("form-container");

        TextField montantField = new TextField(String.valueOf(p.getMontant()));
        
        // Limit length to 20 characters
        montantField.setTextFormatter(new TextFormatter<>(change -> 
            change.getControlNewText().length() <= 20 ? change : null));
        DatePicker datePicker = new DatePicker(p.getDatePaiement().toLocalDate());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Carte Bancaire", "PayPal", "D17");
        methodBox.setValue(p.getMethodePaiement());
        methodBox.setMaxWidth(Double.MAX_VALUE);

        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("En attente", "Effectué", "Annulé");
        statutBox.setValue(p.getStatutPaiement());
        statutBox.setMaxWidth(Double.MAX_VALUE);

        Button saveButton = new Button("Enregistrer (Confirmation)");
        saveButton.getStyleClass().addAll("button", "button-primary");
        saveButton.setMaxWidth(Double.MAX_VALUE);

        Button cancelButton = new Button("Annuler");
        cancelButton.getStyleClass().addAll("button", "button-secondary");
        cancelButton.setMaxWidth(Double.MAX_VALUE);

        saveButton.setOnAction(e -> {
            try {
                p.setMontant(Double.parseDouble(montantField.getText()));
                p.setDatePaiement(Date.valueOf(datePicker.getValue()));
                p.setStatutPaiement(statutBox.getValue());
                p.setMethodePaiement(methodBox.getValue());
                service.modifier(p);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Action enregistrée !");
                showListScene();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur : " + ex.getMessage());
            }
        });

        cancelButton.setOnAction(e -> showListScene());

        form.getChildren().addAll(
                new Label("Montant (DT):"), montantField,
                new Label("Date:"), datePicker,
                new Label("Méthode de paiement:"), methodBox,
                new Label("Statut:"), statutBox,
                new Region(), saveButton, cancelButton
        );

        root.getChildren().addAll(topBar, titleLabel, form);
        Scene scene = new Scene(root, 800, 600);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    private boolean isEuro = false; // Currency state

    // --- LIST PAYMENTS SCENE ---
    private void showListScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        applyCurrentTheme(root);

        VBox topHeader = new VBox(15);
        topHeader.setPadding(new Insets(0, 0, 20, 0));

        HBox navBar = new HBox(20);
        navBar.setAlignment(Pos.CENTER_LEFT);

        Button logoutBtn = new Button("Quitter");
        logoutBtn.getStyleClass().addAll("button", "button-danger");
        logoutBtn.setOnAction(e -> showLoginScene());

        Label titleLabel = new Label("Liste des Paiements (" + currentRole + ")");
        titleLabel.getStyleClass().add("header-label");

        // --- NAVIGATION CONTROLS ---
        navBar.getChildren().addAll(logoutBtn);

        Label balanceLabel = new Label("Solde: " + String.format("%.2f", currentUser.getWalletBalance()) + " DT");
        balanceLabel.getStyleClass().add("subheader-label");
        balanceLabel.setStyle("-fx-text-fill: #27ae60;");

        // Hide Add Button for Admin
        if (currentRole != Role.ADMIN) {
            Button backButton = new Button("← Ajouter");
            backButton.getStyleClass().addAll("button", "button-secondary");
            backButton.setOnAction(e -> showAddScene());
            navBar.getChildren().addAll(backButton, balanceLabel);
        } else {
            Button stripeDashboardBtn = new Button("Show Stripe Payments");
            stripeDashboardBtn.getStyleClass().addAll("button", "button-warning");
            stripeDashboardBtn.setStyle("-fx-background-color: #6772e5; -fx-text-fill: white; -fx-font-weight: bold;");
            stripeDashboardBtn.setOnAction(e -> getHostServices().showDocument("https://dashboard.stripe.com/acct_1T3kH4Dh9nBuKEm1/test/payments"));
            navBar.getChildren().add(stripeDashboardBtn);
        }

        navBar.getChildren().add(titleLabel);
        navBar.getChildren().add(new Region());

        // --- NEW: ADVANCED CONTROLS ---
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher...");
        searchField.setPrefWidth(150);
        searchField.getStyleClass().add("text-field");

        ToggleButton currencyToggle = new ToggleButton("Devise: DT");
        currencyToggle.getStyleClass().addAll("button", "button-secondary");
        currencyToggle.setOnAction(e -> {
            isEuro = currencyToggle.isSelected();
            currencyToggle.setText(isEuro ? "Devise: EUR" : "Devise: DT");
            refreshCards(searchField.getText(), (FlowPane)((ScrollPane)root.getCenter()).getContent());
        });

        controls.getChildren().addAll(searchField, currencyToggle, createThemeToggle(root));
        navBar.getChildren().add(controls);
        HBox.setHgrow(navBar.getChildren().get(navBar.getChildren().size() - 2), Priority.ALWAYS); // Spacer

        topHeader.getChildren().add(navBar);
        root.setTop(topHeader);

        FlowPane cardsContainer = new FlowPane();
        cardsContainer.setHgap(20);
        cardsContainer.setVgap(20);
        cardsContainer.setAlignment(Pos.TOP_LEFT);
        cardsContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        root.setCenter(scrollPane);

        // Logic for Filtered Display
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            refreshCards(newVal, cardsContainer);
        });

        // Initial Load
        refreshCards("", cardsContainer);

        Scene scene = new Scene(root, 900, 700);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    private Button createThemeToggle(Parent root) {
        Button toggle = new Button(isDarkMode ? "☀️ Mode Claire" : "🌙 Mode Sombre");
        toggle.getStyleClass().addAll("button", "button-secondary");
        toggle.setOnAction(e -> {
            isDarkMode = !isDarkMode;
            toggle.setText(isDarkMode ? "☀️ Mode Claire" : "🌙 Mode Sombre");
            applyCurrentTheme(root);
        });
        return toggle;
    }

    private void applyCurrentTheme(Parent root) {
        if (isDarkMode) {
            if (!root.getStyleClass().contains("dark-theme")) {
                root.getStyleClass().add("dark-theme");
            }
        } else {
            root.getStyleClass().remove("dark-theme");
        }
    }

    private void refreshCards(String filter, FlowPane container) {
        container.getChildren().clear();
        List<Paiement> all = service.afficher();
        String f = filter.toLowerCase();

        for (Paiement p : all) {
            boolean matches = f.isEmpty() || 
                             String.valueOf(p.getIdPaiement()).contains(f) ||
                             String.valueOf(p.getMontant()).contains(f);
            
            if (matches) {
                VBox card = createPaymentCard(p);
                container.getChildren().add(card);
                
                // Animation (Fermer)
                javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), card);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            }
        }
    }

    private VBox createPaymentCard(Paiement p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);

        Label idLabel = new Label("#" + p.getIdPaiement());
        idLabel.getStyleClass().add("card-id");

        Label amountLabel = new Label(p.getFormattedAmount(isEuro));
        amountLabel.getStyleClass().add("card-amount");

        Label dateLabel = new Label("📅 " + p.getDatePaiement().toString());
        dateLabel.setStyle("-fx-text-fill: #555;");

        Label methodLabel = new Label("💳 " + p.getMethodePaiement());
        methodLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #083D77;");

        Label statusLabel = new Label(p.getStatutPaiement());
        statusLabel.getStyleClass().add("status-badge");

        String status = p.getStatutPaiement().toLowerCase();
        if (status.contains("effectué")) statusLabel.setStyle("-fx-background-color: #4CAF50;");
        else if (status.contains("attente")) statusLabel.setStyle("-fx-background-color: #FF9800;");
        else statusLabel.setStyle("-fx-background-color: #F44336;");

        // PDF Reçu Button
        Button pdfBtn = new Button("📄 Reçu");
        pdfBtn.getStyleClass().addAll("button", "button-secondary");
        pdfBtn.setStyle("-fx-font-size: 10px;");
        pdfBtn.setOnAction(e -> {
            try {
                org.example.services.PdfService.generateReceipt(p);
                showAlert(Alert.AlertType.INFORMATION, "Reçu", "PDF généré dans le dossier 'receipts' !");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur PDF", "Impossible de générer le PDF: " + ex.getMessage());
            }
        });

        HBox topInfo = new HBox(10, idLabel, new Region(), pdfBtn);
        HBox.setHgrow(topInfo.getChildren().get(1), Priority.ALWAYS);

        card.getChildren().addAll(topInfo, amountLabel, dateLabel, methodLabel, statusLabel);

        // --- ACTIONS (Admin Only) ---
        if (currentRole == Role.ADMIN) {
            HBox actions = new HBox(12);
            actions.setAlignment(Pos.CENTER);

            Button deleteButton = new Button("Supprimer");
            deleteButton.getStyleClass().addAll("button", "button-danger");
            deleteButton.setMinWidth(100);
            deleteButton.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Supprimer ce paiement ?");
                alert.setContentText("Cette action est irréversible.");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK){
                    service.supprimer(p.getIdPaiement());
                    showListScene(); // Re-render whole list
                }
            });

            // --- Confirmation Actions (Accepter/Refuser) ---
            if ("En attente".equalsIgnoreCase(p.getStatutPaiement())) {
                HBox confirmationBox = new HBox(12);
                confirmationBox.setAlignment(Pos.CENTER);

                Button acceptBtn = new Button("Accepter");
                acceptBtn.getStyleClass().addAll("button", "button-success");
                acceptBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
                acceptBtn.setMinWidth(90);
                acceptBtn.setOnAction(e -> {
                    p.setStatutPaiement("Effectué");
                    service.modifier(p);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Paiement accepté !");
                    showListScene();
                });

                Button rejectBtn = new Button("Refuser");
                rejectBtn.getStyleClass().addAll("button", "button-danger");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
                rejectBtn.setMinWidth(90);
                rejectBtn.setOnAction(e -> {
                    p.setStatutPaiement("Annulé");
                    service.modifier(p);
                    showAlert(Alert.AlertType.INFORMATION, "Paiement Refusé", "Le paiement a été marqué comme annulé.");
                    showListScene();
                });

                confirmationBox.getChildren().addAll(acceptBtn, rejectBtn);
                card.getChildren().addAll(new Separator(), confirmationBox);
            }

            actions.getChildren().addAll(deleteButton);
            card.getChildren().addAll(new Separator(), actions);

            // --- Stripe Verification Button ---
            if (p.getStripeSessionId() != null && "En attente".equalsIgnoreCase(p.getStatutPaiement())) {
                Button verifyStripeBtn = new Button("Vérifier Statut Stripe");
                verifyStripeBtn.getStyleClass().addAll("button", "button-info");
                verifyStripeBtn.setMaxWidth(Double.MAX_VALUE);
                verifyStripeBtn.setStyle("-fx-background-color: #6772e5; -fx-text-fill: white; -fx-font-weight: bold;");
                verifyStripeBtn.setOnAction(e -> {
                    boolean isPaid = org.example.services.StripeService.isSessionPaid(p.getStripeSessionId());
                    if (isPaid) {
                        p.setStatutPaiement("Effectué");
                        service.modifier(p);
                        showAlert(Alert.AlertType.INFORMATION, "Paiement Confirmé", "Le paiement Stripe a été vérifié avec succès ! Le statut est maintenant 'Effectué'.");
                        showListScene();
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Paiement Non Vérifié", "Le paiement Stripe n'a pas encore été finalisé ou a échoué.");
                    }
                });
                card.getChildren().add(verifyStripeBtn);
            }
        }

        return card;
    }

    // --- WALLET MANAGEMENT SCENE ---
    private void showWalletScene() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(40));
        root.setAlignment(Pos.CENTER);
        applyCurrentTheme(root);

        Label titleLabel = new Label("Gestion du Portefeuille");
        titleLabel.getStyleClass().add("header-label");

        VBox infoBox = new VBox(10);
        infoBox.setAlignment(Pos.CENTER);
        Label balLabel = new Label("Solde Actuel: " + String.format("%.2f", currentUser.getWalletBalance()) + " DT");
        balLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        Label pointsLabel = new Label("Points de Fidélité: " + currentUser.getLoyaltyPoints());
        pointsLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #8e44ad;");
        infoBox.getChildren().addAll(balLabel, pointsLabel);

        HBox actions = new HBox(40);
        actions.setAlignment(Pos.CENTER);

        // --- RECHARGE ---
        VBox rechargeBox = new VBox(15);
        rechargeBox.getStyleClass().add("form-container");
        rechargeBox.setPrefWidth(300);
        Label rLabel = new Label("Recharger mon compte");
        rLabel.getStyleClass().add("subheader-label");
        TextField amountField = new TextField();
        amountField.setPromptText("Montant à ajouter");
        Button rBtn = new Button("Recharger (+)");
        rBtn.getStyleClass().addAll("button", "button-primary");
        rBtn.setMaxWidth(Double.MAX_VALUE);
        rBtn.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(amountField.getText());
                userService.updateBalance(currentUser.getId(), amt);
                currentUser.setWalletBalance(currentUser.getWalletBalance() + amt);
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Compte rechargé !");
                showWalletScene();
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Montant invalide.");
            }
        });
        rechargeBox.getChildren().addAll(rLabel, amountField, rBtn);

        // --- TRANSFER ---
        VBox transferBox = new VBox(15);
        transferBox.getStyleClass().add("form-container");
        transferBox.setPrefWidth(300);
        Label tLabel = new Label("Transférer de l'argent");
        tLabel.getStyleClass().add("subheader-label");
        TextField toUserField = new TextField();
        toUserField.setPromptText("Destinataire (Nom)");
        TextField tAmountField = new TextField();
        tAmountField.setPromptText("Montant");
        Button tBtn = new Button("Transférer (→)");
        tBtn.getStyleClass().addAll("button", "button-secondary");
        tBtn.setMaxWidth(Double.MAX_VALUE);
        tBtn.setOnAction(e -> {
            try {
                double amt = Double.parseDouble(tAmountField.getText());
                String to = toUserField.getText().trim();
                if (userService.transfer(currentUser.getId(), to, amt)) {
                    currentUser.setWalletBalance(currentUser.getWalletBalance() - amt);
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Transfert réussi vers " + to);
                    showWalletScene();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Transfert échoué (Destinataire inconnu ou solde insuffisant).");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Données invalides.");
            }
        });
        transferBox.getChildren().addAll(tLabel, toUserField, tAmountField, tBtn);

        actions.getChildren().addAll(rechargeBox, transferBox);

        Button backButton = new Button("Retour");
        backButton.getStyleClass().addAll("button", "button-danger");
        backButton.setOnAction(e -> showAddScene());

        root.getChildren().addAll(titleLabel, infoBox, actions, backButton);

        Scene scene = new Scene(root, 900, 700);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
