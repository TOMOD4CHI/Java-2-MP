-- Fix the auto_ecole table
ALTER TABLE `auto_ecole` 
ADD COLUMN IF NOT EXISTS `directeur` VARCHAR(100) NULL DEFAULT NULL AFTER `email`,
MODIFY COLUMN `logo` VARCHAR(255) NULL DEFAULT NULL;

-- Insert default 'Voiture' type_permis if it doesn't exist
SET @voitureExists = (SELECT COUNT(*) FROM `type_permis` WHERE `libelle` = 'Permis Voiture');
INSERT INTO `type_permis` (`libelle`, `description`)
SELECT 'Permis Voiture', 'Permis de conduire pour véhicule léger'
WHERE @voitureExists = 0;

-- Create moniteur_specialite table for storing specializations
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
    REFERENCES `type_permis` (`id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

-- Create seance table for driving/code sessions
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
    REFERENCES `vehicule` (`id`)
);

-- Add missing fields to candidat table
ALTER TABLE `candidat` 
ADD COLUMN IF NOT EXISTS `date_naissance` DATE NULL AFTER `nom`;

-- Add type_permis_id to candidat if not exists
ALTER TABLE `candidat` 
ADD COLUMN IF NOT EXISTS `type_permis_id` INT NULL AFTER `telephone`,
ADD INDEX IF NOT EXISTS `fk_candidat_type_permis_idx` (`type_permis_id` ASC);

-- Add constraint if it doesn't exist
-- First check if the constraint already exists
SET @constraintExists = (
    SELECT COUNT(*) 
    FROM information_schema.TABLE_CONSTRAINTS 
    WHERE CONSTRAINT_NAME = 'fk_candidat_type_permis' 
    AND TABLE_NAME = 'candidat'
);

-- Add the constraint only if it doesn't exist
SET @addConstraintSQL = IF(@constraintExists = 0, 
    'ALTER TABLE `candidat` 
     ADD CONSTRAINT `fk_candidat_type_permis`
     FOREIGN KEY (`type_permis_id`)
     REFERENCES `type_permis` (`id`)
     ON DELETE RESTRICT
     ON UPDATE CASCADE', 
    'SELECT 1'
);

PREPARE stmt FROM @addConstraintSQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Update any existing NULL logos to use the default app_icon.png
UPDATE `auto_ecole` 
SET `logo` = '/images/app_icon.png' 
WHERE `logo` IS NULL OR `logo` = '';

-- Make sure there's at least one auto-ecole entry
INSERT IGNORE INTO `auto_ecole` 
(`nom`, `adresse`, `telephone`, `email`, `directeur`, `logo`) 
VALUES 
('Auto-École Centre', '123 Avenue Principale, Tunis', '12345678', 'contact@autoecole.com', 'Directeur General', '/images/app_icon.png');

-- Update any existing records with NULL directeur
UPDATE `auto_ecole` 
SET `directeur` = 'Directeur par défaut' 
WHERE `directeur` IS NULL; 