-- Script pour créer la table salle
CREATE TABLE IF NOT EXISTS `salle` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `nom` varchar(100) NOT NULL,
    `numero` varchar(20) NOT NULL,
    `capacite` int(11) NOT NULL DEFAULT 20,
    `notes` text DEFAULT NULL,
    `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
    PRIMARY KEY (`id`),
    UNIQUE KEY `numero` (`numero`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Insérer quelques salles par défaut
INSERT INTO `salle` (`nom`, `numero`, `capacite`, `notes`) VALUES
('Salle de Code A', 'A101', 25, 'Salle principale pour les cours de code'),
('Salle de Code B', 'B102', 20, 'Salle secondaire pour les cours de code'),
('Salle de Théorie', 'C103', 30, 'Salle pour les cours théoriques'),
('Salle d\'Examen', 'D104', 15, 'Salle réservée aux examens');