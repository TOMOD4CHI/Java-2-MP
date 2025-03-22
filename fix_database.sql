-- Add missing columns to candidat table
ALTER TABLE `candidat` 
ADD COLUMN `date_naissance` DATE DEFAULT NULL AFTER `email`,
ADD COLUMN `type_permis` INT(11) DEFAULT NULL AFTER `date_naissance`;

-- Create moniteur_specialite table if it doesn't exist
CREATE TABLE IF NOT EXISTS `moniteur_specialite` (
  `moniteur_id` int(11) NOT NULL,
  `type_permis_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`moniteur_id`,`type_permis_id`),
  KEY `fk_moniteur_specialite_type_permis` (`type_permis_id`),
  CONSTRAINT `fk_moniteur_specialite_moniteur` FOREIGN KEY (`moniteur_id`) REFERENCES `moniteur` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_moniteur_specialite_type_permis` FOREIGN KEY (`type_permis_id`) REFERENCES `type_permis` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Create seance table if it doesn't exist
CREATE TABLE IF NOT EXISTS `seance` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `temps` time NOT NULL,
  `duree` int(11) NOT NULL DEFAULT 60,
  `type` varchar(20) NOT NULL,
  `lieu` varchar(100) DEFAULT NULL,
  `moniteur_id` int(11) NOT NULL,
  `candidat_id` int(11) NOT NULL,
  `vehicule_id` int(11) DEFAULT NULL,
  `status` varchar(20) DEFAULT NULL,
  `commentaire` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `moniteur_id` (`moniteur_id`),
  KEY `candidat_id` (`candidat_id`),
  KEY `vehicule_id` (`vehicule_id`),
  CONSTRAINT `seance_ibfk_1` FOREIGN KEY (`moniteur_id`) REFERENCES `moniteur` (`id`),
  CONSTRAINT `seance_ibfk_2` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`),
  CONSTRAINT `seance_ibfk_3` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicule` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Add missing columns to auto_ecole table
ALTER TABLE `auto_ecole` 
ADD COLUMN IF NOT EXISTS `directeur` VARCHAR(100) NULL DEFAULT NULL AFTER `email`,
MODIFY COLUMN `logo` VARCHAR(255) NULL DEFAULT NULL;

-- Create moniteur_specialite table if it doesn't exist
CREATE TABLE IF NOT EXISTS `moniteur_specialite` (
  `moniteur_id` INT NOT NULL,
  `type_permis_id` INT NOT NULL,
  PRIMARY KEY (`moniteur_id`, `type_permis_id`),
  INDEX `fk_moniteur_specialite_type_permis` (`type_permis_id` ASC),
  CONSTRAINT `fk_moniteur_specialite_moniteur`
    FOREIGN KEY (`moniteur_id`)
    REFERENCES `moniteur` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_moniteur_specialite_type_permis`
    FOREIGN KEY (`type_permis_id`)
    REFERENCES `type_permis` (`id`));

-- Check and fix type_permis table
-- First check if 'Voiture' exists in type_permis
SET @voitureExists = (SELECT COUNT(*) FROM `type_permis` WHERE `libelle` = 'Voiture');

-- If 'Voiture' doesn't exist, add it
INSERT INTO `type_permis` (`libelle`, `description`)
SELECT 'Voiture', 'Permis de conduire pour véhicule léger'
WHERE @voitureExists = 0;

-- Create seance table if it doesn't exist
CREATE TABLE IF NOT EXISTS `seance` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `date` DATETIME NOT NULL,
  `duree` INT NOT NULL,
  `moniteur_id` INT NOT NULL,
  `candidat_id` INT NOT NULL,
  `vehicule_id` INT NULL,
  `type` VARCHAR(50) NOT NULL,
  `statut` VARCHAR(50) NOT NULL DEFAULT 'Planifiée',
  PRIMARY KEY (`id`),
  INDEX `fk_seance_moniteur` (`moniteur_id` ASC),
  INDEX `fk_seance_candidat` (`candidat_id` ASC),
  INDEX `fk_seance_vehicule` (`vehicule_id` ASC),
  CONSTRAINT `fk_seance_moniteur`
    FOREIGN KEY (`moniteur_id`)
    REFERENCES `moniteur` (`id`),
  CONSTRAINT `fk_seance_candidat`
    FOREIGN KEY (`candidat_id`)
    REFERENCES `candidat` (`id`),
  CONSTRAINT `fk_seance_vehicule`
    FOREIGN KEY (`vehicule_id`)
    REFERENCES `vehicule` (`id`));

-- Add date_naissance to candidat if not exists
ALTER TABLE `candidat` 
ADD COLUMN IF NOT EXISTS `date_naissance` DATE NULL AFTER `nom`;

-- Add type_permis_id to candidat if not exists
ALTER TABLE `candidat` 
ADD COLUMN IF NOT EXISTS `type_permis_id` INT NULL AFTER `telephone`,
ADD INDEX IF NOT EXISTS `fk_candidat_type_permis_idx` (`type_permis_id` ASC);

-- Add constraint if it doesn't exist
ALTER TABLE `candidat` 
ADD CONSTRAINT `fk_candidat_type_permis`
  FOREIGN KEY (`type_permis_id`)
  REFERENCES `type_permis` (`id`)
  ON DELETE RESTRICT
  ON UPDATE CASCADE; 