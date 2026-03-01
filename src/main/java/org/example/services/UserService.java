package org.example.services;

import org.example.DatabaseConnection;
import org.example.entities.User;
import org.example.PaiementApp.Role;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private Connection conn;

    public UserService() {
        try {
            this.conn = DatabaseConnection.getConnection();
            ensureTableExists();
        } catch (Exception e) {
            System.err.println("Database error in UserService: " + e.getMessage());
        }
    }

    private void ensureTableExists() {
        try (Statement st = conn.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL UNIQUE, " +
                    "email VARCHAR(150), " +
                    "role VARCHAR(20) NOT NULL, " +
                    "wallet_balance DOUBLE DEFAULT 0.0, " +
                    "loyalty_points INT DEFAULT 0)");
            
            // Improved check for email column
            try {
                st.executeQuery("SELECT email FROM users LIMIT 1");
            } catch (SQLException e) {
                System.out.println("⚠️ Column 'email' missing in 'users' table. Attempting to add it...");
                st.execute("ALTER TABLE users ADD COLUMN email VARCHAR(150) AFTER name");
                System.out.println("✅ Column 'email' added successfully!");
            }

            // Ensure specific test users exist
            checkAndAddUser(st, "Admin", "admin@gestion.com", "ADMIN", 1000.0);
            checkAndAddUser(st, "UserTest", "usertest@gmail.com", "USER", 100.0);
            checkAndAddUser(st, "UserTest2", "usertest2@gmail.com", "USER", 50.0);
            
        } catch (SQLException e) {
            System.err.println("Error creating users table: " + e.getMessage());
        }
    }

    private void checkAndAddUser(Statement st, String name, String email, String role, double balance) throws SQLException {
        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users WHERE name = '" + name + "'");
        if (rs.next() && rs.getInt(1) == 0) {
            st.execute("INSERT INTO users (name, email, role, wallet_balance) VALUES ('" + name + "', '" + email + "', '" + role + "', " + balance + ")");
            System.out.println("✅ User '" + name + "' added to database.");
        }
    }

    public User getUserByName(String name) {
        String req = "SELECT * FROM users WHERE name = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    Role.valueOf(rs.getString("role")),
                    rs.getDouble("wallet_balance"),
                    rs.getInt("loyalty_points")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }
    
    public User getUserById(int id) {
        String req = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    Role.valueOf(rs.getString("role")),
                    rs.getDouble("wallet_balance"),
                    rs.getInt("loyalty_points")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
        }
        return null;
    }

    public void updateBalance(int userId, double amount) {
        String req = "UPDATE users SET wallet_balance = wallet_balance + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating balance: " + e.getMessage());
        }
    }

    public void addLoyaltyPoints(int userId, int points) {
        String req = "UPDATE users SET loyalty_points = loyalty_points + ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, points);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating loyalty points: " + e.getMessage());
        }
    }

    public boolean transfer(int fromId, String toName, double amount) {
        User toUser = getUserByName(toName);
        if (toUser == null) return false;

        try {
            conn.setAutoCommit(false);
            
            // Check sender balance
            String checkReq = "SELECT wallet_balance FROM users WHERE id = ?";
            PreparedStatement checkPs = conn.prepareStatement(checkReq);
            checkPs.setInt(1, fromId);
            ResultSet rs = checkPs.executeQuery();
            if (rs.next() && rs.getDouble(1) >= amount) {
                // Deduct from sender
                updateBalance(fromId, -amount);
                // Add to receiver
                updateBalance(toUser.getId(), amount);
                // Award loyalty points to sender (e.g., 10 points)
                addLoyaltyPoints(fromId, 10);
                
                conn.commit();
                return true;
            } else {
                conn.rollback();
            }
        } catch (SQLException e) {
            try { conn.rollback(); } catch (SQLException ex) {}
            System.err.println("Transfer error: " + e.getMessage());
        } finally {
            try { conn.setAutoCommit(true); } catch (SQLException e) {}
        }
        return false;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String req = "SELECT * FROM users";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    Role.valueOf(rs.getString("role")),
                    rs.getDouble("wallet_balance"),
                    rs.getInt("loyalty_points")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
        return list;
    }
}
