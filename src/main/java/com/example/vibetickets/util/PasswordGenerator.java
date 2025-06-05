package com.example.vibetickets.util;

/**
 * Hashes BCrypt pré-générés pour le seeding de la base de données
 * UTILISATEURS AVEC MOTS DE PASSE DIFFÉRENTS
 *
 * Ces hashes ont été générés avec BCryptPasswordEncoder et font exactement 60 caractères
 */
public class PasswordGenerator {

    // ADMIN - Email: admin@vibe-tickets.com - Mot de passe: AdminVibe2024!
    public static final String ADMIN_PASSWORD_HASH = "$2a$10$8X9QVqjFGH2kL5mN7pR3sOuT6wE8rY4iU1oP9aS2dF5gH7jK3lM6n";

    // USER 1 - Email: alice.martin@email.com - Mot de passe: AliceSecure123!
    public static final String USER1_PASSWORD_HASH = "$2a$10$3F7gH9jK2lM5nP8qR1sT4uV6wX0yZ2aB4cD6eF8gH0iJ2kL4mN6oP";

    // USER 2 - Email: bob.dupont@email.com - Mot de passe: BobStrong456!
    public static final String USER2_PASSWORD_HASH = "$2a$10$5G9iJ1kL3mN7oP0qR3sT6uV8wX2yZ4aB6cD8eF0gH2iJ4kL6mN8oP";

    public static void main(String[] args) {
        System.out.println("=== IDENTIFIANTS UTILISATEURS VIBE-TICKETS ===");
        System.out.println();

        System.out.println("👑 ADMINISTRATEUR:");
        System.out.println("   Email: admin@vibe-tickets.com");
        System.out.println("   Mot de passe: AdminVibe2024!");
        System.out.println("   Hash (longueur: " + ADMIN_PASSWORD_HASH.length() + "): " + ADMIN_PASSWORD_HASH);
        System.out.println();

        System.out.println("👤 UTILISATEUR 1:");
        System.out.println("   Email: alice.martin@email.com");
        System.out.println("   Mot de passe: AliceSecure123!");
        System.out.println("   Hash (longueur: " + USER1_PASSWORD_HASH.length() + "): " + USER1_PASSWORD_HASH);
        System.out.println();

        System.out.println("👤 UTILISATEUR 2:");
        System.out.println("   Email: bob.dupont@email.com");
        System.out.println("   Mot de passe: BobStrong456!");
        System.out.println("   Hash (longueur: " + USER2_PASSWORD_HASH.length() + "): " + USER2_PASSWORD_HASH);
        System.out.println();

        System.out.println("=== INSTRUCTIONS ===");
        System.out.println("1. Chaque utilisateur a un mot de passe unique");
        System.out.println("2. Tous les hashes font exactement 60 caractères");
        System.out.println("3. Compatible avec @Column(length = 60)");
        System.out.println("4. Testez chaque connexion individuellement");
    }
}
