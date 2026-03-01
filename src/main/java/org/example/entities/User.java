package org.example.entities;

import org.example.PaiementApp.Role;

public class User {
    private int id;
    private String name;
    private String email;
    private Role role;
    private double walletBalance;
    private int loyaltyPoints;

    public User() {}

    public User(int id, String name, String email, Role role, double walletBalance, int loyaltyPoints) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.walletBalance = walletBalance;
        this.loyaltyPoints = loyaltyPoints;
    }

    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.walletBalance = 0.0;
        this.loyaltyPoints = 0;
    }

    public User(String name, Role role) {
        this.name = name;
        this.role = role;
        this.walletBalance = 0.0;
        this.loyaltyPoints = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(double walletBalance) { this.walletBalance = walletBalance; }

    public int getLoyaltyPoints() { return loyaltyPoints; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", walletBalance=" + walletBalance +
                ", loyaltyPoints=" + loyaltyPoints +
                '}';
    }
}
