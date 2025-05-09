-- Renommer la table rule en role
ALTER TABLE rule RENAME TO role;

-- Renommer la colonne rule_id en role_id dans la table user_app
ALTER TABLE user_app RENAME COLUMN rule_id TO role_id;

-- Mettre à jour les séquences si nécessaire
ALTER SEQUENCE rule_rule_id_seq RENAME TO role_role_id_seq;
