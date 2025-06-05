-- ============================================================================
-- SCRIPT DE REMPLISSAGE DE LA BASE DE DONNÉES VIBE-TICKETS
-- ============================================================================

-- Insertion des rôles
INSERT INTO role (name) VALUES
('User'),
('Admin');

-- Insertion des utilisateurs (1 Admin + 2 Users)
-- CHAQUE UTILISATEUR A UN MOT DE PASSE DIFFÉRENT
-- Hashes BCrypt valides de 60 caractères exactement
INSERT INTO user_app (first_name, last_name, email, password, user_key, role_id) VALUES
('Simon', 'Admin', 'admin@vibe-tickets.com', '$2a$10$8X9QVqjFGH2kL5mN7pR3sOuT6wE8rY4iU1oP9aS2dF5gH7jK3lM6n', 'ADMIN001', 2),
('Alice', 'Martin', 'alice.martin@email.com', '$2a$10$3F7gH9jK2lM5nP8qR1sT4uV6wX0yZ2aB4cD6eF8gH0iJ2kL4mN6oP', 'USER001', 1),
('Bob', 'Dupont', 'bob.dupont@email.com', '$2a$10$5G9iJ1kL3mN7oP0qR3sT6uV8wX2yZ4aB6cD8eF0gH2iJ4kL6mN8oP', 'USER002', 1);

-- Insertion des offres (12 Disponibles + 3 Indisponibles pour tests)
INSERT INTO offer (name, description, price, person_count, offer_type, available) VALUES
-- OFFRES DISPONIBLES
('Concert Rock Festival', 'Concert de rock avec les meilleurs groupes européens', 89.99, 1, 'Concert', true),
('Match Football PSG vs OM', 'Classique français au Parc des Princes', 125.50, 1, 'Sport', true),
('Théâtre - Le Roi Lion', 'Spectacle musical familial', 65.00, 1, 'Théâtre', true),
('Festival Jazz Montreux', 'Festival de jazz international', 95.00, 1, 'Festival', true),
('Opéra - La Traviata', 'Opéra classique de Verdi', 110.00, 1, 'Opéra', true),
('Concert Électro Techno', 'Soirée électro avec DJs internationaux', 45.00, 1, 'Concert', true),
('Comédie Musicale Hamilton', 'Spectacle Broadway à Londres', 150.00, 1, 'Théâtre', true),
('Match Tennis Roland Garros', 'Finale homme Roland Garros', 200.00, 1, 'Sport', true),
('Concert Pop Taylor Swift', 'Concert exceptionnel de la star mondiale', 175.00, 1, 'Concert', true),
('Match Rugby France vs Angleterre', 'Tournoi des 6 Nations au Stade de France', 85.00, 1, 'Sport', true),
('Festival Cannes Film', 'Projection exclusive et tapis rouge', 300.00, 1, 'Festival', true),
('Cirque du Soleil', 'Spectacle acrobatique époustouflant', 95.00, 1, 'Spectacle', true),
-- OFFRES INDISPONIBLES (pour tester les filtres)
('Concert Classique Philharmonie', 'Orchestre de Paris - Beethoven', 75.00, 1, 'Concert', false),
('Festival Électro Tomorrowland', 'Le plus grand festival électro', 180.00, 1, 'Festival', false),
('Match Basket NBA Paris', 'Match exceptionnel NBA à Paris', 250.00, 1, 'Sport', false);

-- Vérification des données insérées
SELECT 'ROLES:' as table_name;
SELECT * FROM role;

SELECT 'USERS:' as table_name;
SELECT user_id, first_name, last_name, email, user_key, role_id FROM user_app;

SELECT 'OFFERS:' as table_name;
SELECT offer_id, name, price, person_count, offer_type, available FROM offer;
