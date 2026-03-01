package org.example;

import org.example.services.UserService;
import org.example.entities.User;
import org.example.PaiementApp.Role;

public class DbFix {
    public static void main(String[] args) {
        UserService userService = new UserService();
        User user2 = userService.getUserByName("UserTest2");
        if (user2 == null) {
            System.out.println("Adding UserTest2...");
            // Since we don't have a direct 'save' method in UserService that takes an object,
            // we'll use the logic from ensureTableExists but manually.
            try {
                var conn = DatabaseConnection.getConnection();
                var st = conn.createStatement();
                st.execute("INSERT INTO users (name, role, wallet_balance) VALUES ('UserTest2', 'USER', 50.0)");
                System.out.println("✅ UserTest2 added successfully!");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("UserTest2 already exists.");
        }
    }
}
