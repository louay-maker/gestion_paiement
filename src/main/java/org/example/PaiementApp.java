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
import org.example.services.PaiementService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PaiementApp extends Application {

    private PaiementService service = new PaiementService();
    private Stage primaryStage;

    // RBAC Roles
    public enum Role { ADMIN, USER }
    private Role currentRole;
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

        Label infoLabel = new Label("Veuillez choisir votre rôle :");
        infoLabel.getStyleClass().add("subheader-label");

        Button adminBtn = new Button("ADMINISTRATEUR");
        adminBtn.getStyleClass().addAll("button", "button-primary");
        adminBtn.setMaxWidth(Double.MAX_VALUE);
        adminBtn.setOnAction(e -> {
            currentRole = Role.ADMIN;
            showListScene();
        });

        Button userBtn = new Button("UTILISATEUR");
        userBtn.getStyleClass().addAll("button", "button-secondary");
        userBtn.setMaxWidth(Double.MAX_VALUE);
        userBtn.setOnAction(e -> {
            currentRole = Role.USER;
            showAddScene(); // Users start on Add scene
        });

        loginBox.getChildren().addAll(infoLabel, adminBtn, userBtn);
        root.getChildren().addAll(topBar, titleLabel, loginBox);

        Scene scene = new Scene(root, 800, 600);
        applyStyles(scene);
        primaryStage.setScene(scene);
    }

    // --- ADD PAYMENT SCENE (Now Role Sensitive) ---
    private void showAddScene() {
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

        VBox form = new VBox(15);
        form.setMaxWidth(400);
        form.getStyleClass().add("form-container");

        TextField montantField = new TextField();
        montantField.setPromptText("Montant (ex: 500.0)");

        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        
        ComboBox<String> methodBox = new ComboBox<>();
        methodBox.getItems().addAll("Carte Bancaire", "PayPal", "D17");
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

        // Logic
        addButton.setOnAction(e -> {
            try {
                double montant = Double.parseDouble(montantField.getText());
                Date date = Date.valueOf(datePicker.getValue());
                String statut = (currentRole == Role.ADMIN) ? statutBox.getValue() : "En attente";
                String methode = methodBox.getValue();

                Paiement p = new Paiement(montant, date, statut, methode);
                service.ajouter(p);
                
                String msg = (currentRole == Role.USER) 
                    ? "Paiement ajouté ! En attente de confirmation Admin." 
                    : "Paiement ajouté avec succès !";
                showAlert(Alert.AlertType.INFORMATION, "Succès", msg);
                montantField.clear();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Le montant doit être un nombre valide !");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de l'ajout : " + ex.getMessage());
            }
        });

        listButton.setOnAction(e -> showListScene());

        form.getChildren().addAll(
            new Label("Montant (DT):"), montantField, 
            new Label("Date:"), datePicker,
            new Label("Méthode de paiement:"), methodBox
        );
        
        // Only Admin chooses Status when adding
        if (currentRole == Role.ADMIN) {
            form.getChildren().addAll(new Label("Statut:"), statutBox);
        }

        form.getChildren().addAll(new Region(), addButton);
        root.getChildren().addAll(topButtons, titleLabel, form, new Region(), listButton);

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

        Button backButton = new Button("← Ajouter");
        backButton.getStyleClass().addAll("button", "button-secondary");
        backButton.setOnAction(e -> showAddScene());

        Button logoutBtn = new Button("Quitter");
        logoutBtn.getStyleClass().addAll("button", "button-danger");
        logoutBtn.setOnAction(e -> showLoginScene());

        Label titleLabel = new Label("Liste des Paiements (" + currentRole + ")");
        titleLabel.getStyleClass().add("header-label");

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
        navBar.getChildren().addAll(logoutBtn, backButton, titleLabel, new Region(), controls);
        HBox.setHgrow(navBar.getChildren().get(3), Priority.ALWAYS); // Spacer

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

            Button editButton = new Button("Confirmer"); 
            editButton.getStyleClass().addAll("button", "button-warning");
            editButton.setMinWidth(100);
            editButton.setOnAction(e -> showModifyScene(p));

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

            actions.getChildren().addAll(editButton, deleteButton);
            card.getChildren().addAll(new Separator(), actions);
        }

        return card;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
