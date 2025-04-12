-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 12, 2025 at 02:19 PM
-- Generation Time: Apr 12, 2025 at 02:19 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `autoecole`
--

-- --------------------------------------------------------

--
-- Table structure for table `auto_ecole`
--

CREATE TABLE `auto_ecole` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `adresse` text NOT NULL,
  `logo_path` varchar(255) DEFAULT NULL,
  `telephone` varchar(20) NOT NULL,
  `email` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `auto_ecole`
--

INSERT INTO `auto_ecole` VALUES(1, 'Auto École Exemplaire', 'Avenue des Conducteurs 123, Ville', 'logos/default_logo.png', '12345678', 'contact@autoecole.com', 'aaaa', 'aaaa', '2025-03-22 13:11:27');
INSERT INTO `auto_ecole` VALUES(1, 'Auto École Exemplaire', 'Avenue des Conducteurs 123, Ville', 'logos/default_logo.png', '12345678', 'contact@autoecole.com', 'aaaa', 'aaaa', '2025-03-22 13:11:27');

-- --------------------------------------------------------

--
-- Table structure for table `candidat`
--

CREATE TABLE `candidat` (
  `id` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `cin` varchar(20) NOT NULL,
  `adresse` text DEFAULT NULL,
  `telephone` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `date_naissance` date DEFAULT NULL,
  `type_permis` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `candidat`
--










-- --------------------------------------------------------

--
-- Table structure for table `document`
--

CREATE TABLE `document` (
  `id` int(11) NOT NULL,
  `dossier_id` int(11) NOT NULL,
  `type_document_id` int(11) NOT NULL,
  `nom_fichier` varchar(255) NOT NULL,
  `chemin_fichier` varchar(255) NOT NULL,
  `date_upload` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `document`
--














-- --------------------------------------------------------

--
-- Table structure for table `dossier`
--

CREATE TABLE `dossier` (
  `id` int(11) NOT NULL,
  `candidat_id` int(11) NOT NULL,
  `statut` varchar(50) DEFAULT 'En attente',
  `date_creation` date DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `dossier`
--










-- --------------------------------------------------------

--
-- Table structure for table `entretien`
--

CREATE TABLE `entretien` (
  `id` int(11) NOT NULL,
  `vehicule_id` int(11) NOT NULL,
  `date_entretien` date NOT NULL,
  `type_entretien` varchar(100) NOT NULL,
  `description` text DEFAULT NULL,
  `cout` decimal(10,2) NOT NULL,
  `entretien_suivant` date DEFAULT NULL,
  `maintenance` int(1) NOT NULL DEFAULT 0,
  `kilometrage` int(11) NOT NULL,
  `facture_path` varchar(255) DEFAULT NULL,
  `statut` varchar(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `entretien`
--














--
-- Dumping data for table `entretien`
--














-- --------------------------------------------------------

--
-- Table structure for table `examen`
--

CREATE TABLE `examen` (
  `id` int(11) NOT NULL,
  `candidat_id` int(11) NOT NULL,
  `type_examen_id` int(11) NOT NULL,
  `date_examen` date NOT NULL,
  `heure_examen` time DEFAULT NULL,
  `lieu` varchar(255) DEFAULT NULL,
  `frais` decimal(10,2) NOT NULL,
  `resultat` tinyint(1) DEFAULT NULL,
  `commentaire` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `examen`
--




--
-- Dumping data for table `examen`
--




-- --------------------------------------------------------

--
-- Table structure for table `inscription`
--

CREATE TABLE `inscription` (
  `id` int(10) NOT NULL,
  `cin` varchar(20) NOT NULL,
  `plan_id` int(10) NOT NULL,
  `statut` varchar(20) NOT NULL DEFAULT 'Actif',
  `statut_paiement` varchar(20) NOT NULL DEFAULT 'En attente',
  `cycle_paiement` varchar(20) NOT NULL DEFAULT 'Totale',
  `date_inscription` date NOT NULL DEFAULT current_timestamp(),
  `date_paiement_suivant` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `inscription`
--










-- --------------------------------------------------------

--
-- Table structure for table `moniteur`
--

CREATE TABLE `moniteur` (
  `id` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `prenom` varchar(50) NOT NULL,
  `cin` varchar(20) NOT NULL,
  `adresse` text DEFAULT NULL,
  `telephone` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `date_naissance` date DEFAULT NULL,
  `date_embauche` date NOT NULL,
  `salaire` decimal(10,2) DEFAULT NULL,
  `statut` varchar(20) DEFAULT 'Actif',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `moniteur`
--




-- --------------------------------------------------------

--
-- Table structure for table `moniteur_specialite`
--

CREATE TABLE `moniteur_specialite` (
  `id` int(11) NOT NULL,
  `moniteur_id` int(11) NOT NULL,
  `type_permis_id` int(11) NOT NULL,
  `date_obtention` date DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `moniteur_specialite`
--




-- --------------------------------------------------------

--
-- Table structure for table `paiement`
--

CREATE TABLE `paiement` (
  `id` int(11) NOT NULL,
  `id_candidat` int(11) NOT NULL,
  `inscription_id` int(10) DEFAULT NULL,
  `id_examen` int(11) DEFAULT NULL,
  `type_paiement` varchar(20) NOT NULL DEFAULT 'Totale',
  `montant` decimal(10,2) NOT NULL,
  `date_paiement` date NOT NULL,
  `mode_paiement` varchar(50) DEFAULT 'Espèces',
  `statut` varchar(100) DEFAULT 'COMPLETE',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `paiement`
--














-- --------------------------------------------------------

--
-- Table structure for table `plan`
--

CREATE TABLE `plan` (
  `id` int(10) NOT NULL,
  `libelle` varchar(50) NOT NULL,
  `description` text DEFAULT NULL,
  `type_permis_id` int(11) DEFAULT NULL,
  `prix` decimal(10,2) NOT NULL,
  `duree` int(11) DEFAULT 0,
  `heures_code` int(11) DEFAULT 0,
  `heures_conduite` int(11) DEFAULT 0,
  `actif` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `plan`
--

INSERT INTO `plan` VALUES(1, 'Basic Motorcycle Course', 'Fundamental motorcycle training for beginners', 1, 700.00, 2, 8, 6, 1, '2025-03-23 22:54:51');
INSERT INTO `plan` VALUES(3, 'Basic Car Driving Course', 'Essential car driving skills for new drivers', 2, 1200.00, 3, 12, 10, 1, '2025-03-23 22:54:51');
INSERT INTO `plan` VALUES(7, 'Heavy Truck License Course', 'Complete training for heavy goods vehicle license', 3, 1900.00, 4, 14, 8, 1, '2025-03-23 22:54:51');

-- --------------------------------------------------------

--
-- Table structure for table `presence_code`
--

CREATE TABLE `presence_code` (
  `id` int(11) NOT NULL,
  `session_code_id` int(11) NOT NULL,
  `candidat_id` int(11) NOT NULL,
  `present` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `presence_code`
--





--
-- Dumping data for table `presence_code`
--





-- --------------------------------------------------------

--
-- Table structure for table `presence_conduite`
--

CREATE TABLE `presence_conduite` (
  `id` int(11) NOT NULL,
  `session_conduite_id` int(11) NOT NULL,
  `candidat_id` int(11) NOT NULL,
  `present` tinyint(1) DEFAULT 0,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rendez_vous`
--

CREATE TABLE `rendez_vous` (
  `id` int(11) NOT NULL,
  `candidat_id` int(11) NOT NULL,
  `session_code_id` int(11) DEFAULT NULL,
  `session_conduite_id` int(11) DEFAULT NULL,
  `date` date NOT NULL,
  `heure` time NOT NULL,
  `duree` int(11) DEFAULT 60,
  `confirme` tinyint(1) DEFAULT 0,
  `annule` tinyint(1) DEFAULT 0,
  `raison_annulation` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `salle`
--

CREATE TABLE `salle` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `numero` varchar(20) NOT NULL,
  `capacite` int(11) NOT NULL DEFAULT 20,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `seance`
--

CREATE TABLE `seance` (
  `id` int(11) NOT NULL,
  `date` date NOT NULL,
  `heure` time NOT NULL,
  `duree` int(11) NOT NULL DEFAULT 60,
  `type` varchar(20) NOT NULL,
  `lieu` varchar(100) DEFAULT NULL,
  `moniteur_id` int(11) NOT NULL,
  `candidat_id` int(11) NOT NULL,
  `vehicule_id` int(11) DEFAULT NULL,
  `statut` varchar(20) DEFAULT 'Planifiée',
  `commentaire` text DEFAULT NULL,
  `kilometrage_debut` int(11) DEFAULT NULL,
  `kilometrage_fin` int(11) DEFAULT NULL,
  `latitude` double DEFAULT NULL,
  `longitude` double DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `session_code`
--

CREATE TABLE `session_code` (
  `id` int(11) NOT NULL,
  `plan_id` int(10) NOT NULL,
  `date_session` date NOT NULL,
  `heure_debut` time NOT NULL,
  `heure_fin` time NOT NULL,
  `moniteur_id` int(11) NOT NULL,
  `salle` varchar(50) DEFAULT NULL,
  `capacite_max` int(11) DEFAULT 20,
  `nombre_inscrits` int(11) DEFAULT 0,
  `statut` varchar(20) NOT NULL DEFAULT 'Planifiée',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `session_code`
--






-- --------------------------------------------------------

--
-- Table structure for table `session_conduite`
--

CREATE TABLE `session_conduite` (
  `id` int(11) NOT NULL,
  `plan_id` int(10) NOT NULL,
  `date_session` date NOT NULL,
  `heure_debut` time NOT NULL,
  `heure_fin` time NOT NULL,
  `moniteur_id` int(11) NOT NULL,
  `vehicule_id` int(11) NOT NULL,
  `point_rencontre_lat` decimal(10,8) DEFAULT NULL,
  `point_rencontre_lon` decimal(11,8) DEFAULT NULL,
  `point_rencontre_adresse` text DEFAULT NULL,
  `kilometres_parcourus` int(11) DEFAULT NULL,
  `statut` varchar(20) NOT NULL DEFAULT 'Planifiée',
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `type_document`
--

CREATE TABLE `type_document` (
  `id` int(11) NOT NULL,
  `libelle` varchar(50) NOT NULL,
  `description` text DEFAULT NULL,
  `obligatoire` tinyint(1) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `type_document`
--

INSERT INTO `type_document` VALUES(1, 'CIN', 'Carte d\'identité nationale', 1, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(2, 'PERMIS_A', 'Permis de conduire d\'un Moto', 0, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(3, 'CERTIFICAT_MEDICAL', 'Certificat d\'aptitude médicale', 1, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(4, 'PHOTO', 'Photo d\'identité', 1, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(5, 'PROOF_OF_RESIDENCE', 'Justificatif de domicile', 1, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(6, 'AUTRE', 'Autre document', 0, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(7, 'PERMIS_B', 'Permis de Counduite d\'une Voiture', 1, '2025-03-27 19:33:03');
INSERT INTO `type_document` VALUES(8, 'PERMIS_C', 'Permis de counduite d\'un Camion', 1, '2025-03-27 19:33:36');
INSERT INTO `type_document` VALUES(1, 'CIN', 'Carte d\'identité nationale', 1, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(2, 'PERMIS_A', 'Permis de conduire d\'un Moto', 0, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(3, 'CERTIFICAT_MEDICAL', 'Certificat d\'aptitude médicale', 1, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(4, 'PHOTO', 'Photo d\'identité', 1, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(5, 'PROOF_OF_RESIDENCE', 'Justificatif de domicile', 1, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(6, 'AUTRE', 'Autre document', 0, '2025-03-23 22:49:32');
INSERT INTO `type_document` VALUES(7, 'PERMIS_B', 'Permis de Counduite d\'une Voiture', 1, '2025-03-27 19:33:03');
INSERT INTO `type_document` VALUES(8, 'PERMIS_C', 'Permis de counduite d\'un Camion', 1, '2025-03-27 19:33:36');

-- --------------------------------------------------------

--
-- Table structure for table `type_examen`
--

CREATE TABLE `type_examen` (
  `id` int(11) NOT NULL,
  `libelle` varchar(50) NOT NULL,
  `description` text DEFAULT NULL,
  `cout` decimal(10,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `type_examen`
--

INSERT INTO `type_examen` VALUES(1, 'Code', 'Examen du code de la route', 100.00, '2025-03-22 13:11:28');
INSERT INTO `type_examen` VALUES(2, 'Conduite', 'Examen de conduite en circulation', 200.00, '2025-03-22 13:11:28');
INSERT INTO `type_examen` VALUES(1, 'Code', 'Examen du code de la route', 100.00, '2025-03-22 13:11:28');
INSERT INTO `type_examen` VALUES(2, 'Conduite', 'Examen de conduite en circulation', 200.00, '2025-03-22 13:11:28');

-- --------------------------------------------------------

--
-- Table structure for table `type_permis`
--

CREATE TABLE `type_permis` (
  `id` int(11) NOT NULL,
  `code` varchar(10) NOT NULL,
  `libelle` varchar(50) NOT NULL,
  `description` text DEFAULT NULL,
  `age_minimum` int(11) DEFAULT 18,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `type_permis`
--

INSERT INTO `type_permis` VALUES(1, 'A', 'Moto', 'Permis pour conduire une moto', 18, '2025-03-22 13:11:28');
INSERT INTO `type_permis` VALUES(2, 'B', 'Voiture', 'Permis pour conduire une voiture', 18, '2025-03-22 13:11:28');
INSERT INTO `type_permis` VALUES(3, 'C', 'Camion', 'Permis pour conduire un camion', 21, '2025-03-22 13:11:28');
INSERT INTO `type_permis` VALUES(1, 'A', 'Moto', 'Permis pour conduire une moto', 18, '2025-03-22 13:11:28');
INSERT INTO `type_permis` VALUES(2, 'B', 'Voiture', 'Permis pour conduire une voiture', 18, '2025-03-22 13:11:28');
INSERT INTO `type_permis` VALUES(3, 'C', 'Camion', 'Permis pour conduire un camion', 21, '2025-03-22 13:11:28');

-- --------------------------------------------------------

--
-- Table structure for table `vehicule`
--

CREATE TABLE `vehicule` (
  `id` int(11) NOT NULL,
  `immatriculation` varchar(20) NOT NULL,
  `type_permis_id` int(11) NOT NULL,
  `marque` varchar(50) NOT NULL,
  `modele` varchar(50) NOT NULL,
  `annee` int(11) DEFAULT NULL,
  `date_mise_service` date NOT NULL,
  `kilometrage_total` int(11) NOT NULL DEFAULT 0,
  `kilometrage_prochain_entretien` int(11) DEFAULT NULL,
  `date_prochain_entretien` date DEFAULT NULL,
  `date_derniere_visite_technique` date DEFAULT NULL,
  `date_prochaine_visite_technique` date DEFAULT NULL,
  `date_expiration_assurance` date DEFAULT NULL,
  `statut` varchar(20) DEFAULT 'Disponible',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data 
--


INSERT INTO `candidat` (`id`, `nom`, `prenom`, `cin`, `adresse`, `telephone`, `email`, `date_naissance`, `type_permis`, `created_at`) VALUES
(22, 'ayoub', 'zneidi', '12345678', 'aerazerazer', '93487161', 'dalmc132@gmail.com', '2007-04-12', NULL, '2025-04-12 21:16:44'),
(23, 'injeniour', 'zneidi', '87654321', 'aezrazerazera', '12345678', 'aezrear@aeaezr.azer', '2007-04-12', NULL, '2025-04-12 21:33:33'),
(24, 'Ben Salah', 'Mohamed', '08321456', 'Rue Marseille 15, Tunis', '51234567', 'mohamed.bs@gmail.com', '1998-05-10', 2, '2025-04-13 08:00:00'),
(25, 'Laabidi', 'Sami', '08765123', 'Avenue Habib Thameur 42, Ariana', '52876543', 'sami.laabidi@yahoo.fr', '2000-03-15', 2, '2025-04-13 08:05:00'),
(26, 'Trabelsi', 'Amira', '09812345', 'Rue Ibn Khaldoun 87, Sousse', '53987612', 'amira.trabelsi@gmail.com', '1995-07-20', 2, '2025-04-13 08:10:00'),
(27, 'Abidi', 'Karim', '09123789', 'Rue de Marseille 23, Sfax', '54321765', 'karim.abidi@hotmail.com', '1997-11-05', 1, '2025-04-13 08:15:00'),
(28, 'Ben Ammar', 'Yasmine', '08651234', 'Avenue Habib Bourguiba 78, Monastir', '55432198', 'yasmine.ba@gmail.com', '1999-09-18', 2, '2025-04-13 08:20:00'),
(29, 'Chaabane', 'Ahmed', '07896543', 'Rue Ali Bach Hamba 34, Bizerte', '56789012', 'ahmed.chaabane@yahoo.fr', '2001-02-22', 2, '2025-04-13 08:25:00'),
(30, 'Riahi', 'Nour', '07123654', 'Avenue Mohamed V 56, Nabeul', '57123459', 'nour.riahi@gmail.com', '1996-06-30', 1, '2025-04-13 08:30:00'),
(31, 'Mejri', 'Bilel', '07896123', 'Rue Lac Leman 12, Marsa', '58765432', 'bilel.mejri@hotmail.com', '1994-12-15', 3, '2025-04-13 08:35:00'),
(32, 'Saidi', 'Ines', '06543219', 'Avenue Habib Bourguiba 65, Hammamet', '59871234', 'ines.saidi@gmail.com', '2000-08-25', 2, '2025-04-13 08:40:00'),
(33, 'Boughanmi', 'Malek', '06789123', 'Rue Ibn Sina 43, Gafsa', '50123987', 'malek.boughanmi@yahoo.fr', '1998-01-10', 1, '2025-04-13 08:45:00'),
(34, 'Chebbi', 'Rania', '05432198', 'Avenue de la République 29, Mahdia', '51234098', 'rania.chebbi@gmail.com', '1999-05-12', 2, '2025-04-13 08:50:00'),
(35, 'Messaoudi', 'Hamza', '05789456', 'Rue Principale 76, Kairouan', '52345678', 'hamza.messaoudi@hotmail.com', '1997-03-28', 3, '2025-04-13 08:55:00'),
(36, 'Hammami', 'Sarra', '04567891', 'Avenue Farhat Hached 32, Tozeur', '53456789', 'sarra.hammami@gmail.com', '2001-07-15', 2, '2025-04-13 09:00:00'),
(37, 'Jebali', 'Tarek', '04123789', 'Rue 7 Novembre 54, Kebili', '54567890', 'tarek.jebali@yahoo.fr', '1995-10-08', 2, '2025-04-13 09:05:00'),
(38, 'Ben Amor', 'Emna', '03987654', 'Avenue Habib Bourguiba 21, Djerba', '55678901', 'emna.benamor@gmail.com', '1998-04-20', 1, '2025-04-13 09:10:00'),
(39, 'Belhadj', 'Youssef', '03456789', 'Rue des Orangers 67, Ben Arous', '56789012', 'youssef.belhadj@hotmail.com', '1996-12-03', 3, '2025-04-13 09:15:00'),
(40, 'Ferchichi', 'Nadia', '02345678', 'Avenue Mohamed V 43, Manouba', '57890123', 'nadia.ferchichi@gmail.com', '2000-02-17', 2, '2025-04-13 09:20:00'),
(41, 'Oueslati', 'Omar', '02987654', 'Rue Taher Sfar 19, Zaghouan', '58901234', 'omar.oueslati@yahoo.fr', '1995-09-05', 2, '2025-04-13 09:25:00'),
(42, 'Nasri', 'Asma', '01876543', 'Avenue Ali Belhouane 78, Béja', '59012345', 'asma.nasri@gmail.com', '1997-06-22', 2, '2025-04-13 09:30:00'),
(43, 'Saadaoui', 'Mehdi', '01234567', 'Rue Ibn Rochd 31, Jendouba', '50123456', 'mehdi.saadaoui@hotmail.com', '1999-11-14', 1, '2025-04-13 09:35:00');

INSERT INTO `dossier` (`id`, `candidat_id`, `statut`, `date_creation`, `notes`, `created_at`) VALUES
(13, 22, 'En attente', NULL, NULL, '2025-04-12 21:16:44'),
(14, 23, 'En attente', NULL, NULL, '2025-04-12 21:33:33'),
(15, 24, 'En cours', '2025-04-13', 'Dossier complet', '2025-04-13 08:00:00'),
(16, 25, 'En cours', '2025-04-13', 'Manque certificat médical', '2025-04-13 08:05:00'),
(17, 26, 'Validé', '2025-04-13', 'Prêt pour examen', '2025-04-13 08:10:00'),
(18, 27, 'En attente', '2025-04-13', 'Dossier incomplet', '2025-04-13 08:15:00'),
(19, 28, 'En cours', '2025-04-13', NULL, '2025-04-13 08:20:00'),
(20, 29, 'Validé', '2025-04-13', 'Formation en cours', '2025-04-13 08:25:00'),
(21, 30, 'En attente', '2025-04-13', 'Attente paiement', '2025-04-13 08:30:00'),
(22, 31, 'En cours', '2025-04-13', NULL, '2025-04-13 08:35:00'),
(23, 32, 'Validé', '2025-04-13', 'Dossier complet', '2025-04-13 08:40:00'),
(24, 33, 'En cours', '2025-04-13', NULL, '2025-04-13 08:45:00'),
(25, 34, 'En attente', '2025-04-13', 'Manque photo', '2025-04-13 08:50:00'),
(26, 35, 'Validé', '2025-04-13', NULL, '2025-04-13 08:55:00'),
(27, 36, 'En cours', '2025-04-13', 'En attente validation', '2025-04-13 09:00:00'),
(28, 37, 'En attente', '2025-04-13', NULL, '2025-04-13 09:05:00'),
(29, 38, 'Validé', '2025-04-13', 'Prêt pour examen', '2025-04-13 09:10:00'),
(30, 39, 'En cours', '2025-04-13', NULL, '2025-04-13 09:15:00'),
(31, 40, 'En attente', '2025-04-13', 'Manque justificatif de domicile', '2025-04-13 09:20:00'),
(32, 41, 'Validé', '2025-04-13', NULL, '2025-04-13 09:25:00'),
(33, 42, 'En cours', '2025-04-13', 'Dossier presque complet', '2025-04-13 09:30:00'),
(34, 43, 'En attente', '2025-04-13', NULL, '2025-04-13 09:35:00');


INSERT INTO `type_document` (`id`, `libelle`, `description`, `obligatoire`, `created_at`) VALUES
(1, 'CIN', 'Carte d\'identité nationale', 1, '2025-03-23 22:49:32'),
(2, 'PERMIS_A', 'Permis de conduire d\'un Moto', 0, '2025-03-23 22:49:32'),
(3, 'CERTIFICAT_MEDICAL', 'Certificat d\'aptitude médicale', 1, '2025-03-23 22:49:32'),
(4, 'PHOTO', 'Photo d\'identité', 1, '2025-03-23 22:49:32'),
(5, 'PROOF_OF_RESIDENCE', 'Justificatif de domicile', 1, '2025-03-23 22:49:32'),
(6, 'AUTRE', 'Autre document', 0, '2025-03-23 22:49:32'),
(7, 'PERMIS_B', 'Permis de Counduite d\'une Voiture', 1, '2025-03-27 19:33:03'),
(8, 'PERMIS_C', 'Permis de counduite d\'un Camion', 1, '2025-03-27 19:33:36');

INSERT INTO `document` (`id`, `dossier_id`, `type_document_id`, `nom_fichier`, `chemin_fichier`, `date_upload`) VALUES
(10, 13, 4, 'ayoub', 'uploads/documents/13/87c75970-c7a6-482f-9833-d6ca8c7cf85f', '2025-04-12 21:17:34'),
(11, 15, 1, 'cin_mohamed', 'uploads/documents/15/cin_mohamed_24', '2025-04-13 08:00:01'),
(12, 15, 3, 'certif_medical_mohamed', 'uploads/documents/15/certif_medical_mohamed_24', '2025-04-13 08:00:02'),
(13, 15, 4, 'photo_mohamed', 'uploads/documents/15/photo_mohamed_24', '2025-04-13 08:00:03'),
(14, 15, 5, 'justif_domicile_mohamed', 'uploads/documents/15/justif_domicile_mohamed_24', '2025-04-13 08:00:04'),
(15, 16, 1, 'cin_sami', 'uploads/documents/16/cin_sami_25', '2025-04-13 08:05:01'),
(16, 16, 4, 'photo_sami', 'uploads/documents/16/photo_sami_25', '2025-04-13 08:05:02'),
(17, 16, 5, 'justif_domicile_sami', 'uploads/documents/16/justif_domicile_sami_25', '2025-04-13 08:05:03'),
(18, 17, 1, 'cin_amira', 'uploads/documents/17/cin_amira_26', '2025-04-13 08:10:01'),
(19, 17, 3, 'certif_medical_amira', 'uploads/documents/17/certif_medical_amira_26', '2025-04-13 08:10:02'),
(20, 17, 4, 'photo_amira', 'uploads/documents/17/photo_amira_26', '2025-04-13 08:10:03'),
(21, 17, 5, 'justif_domicile_amira', 'uploads/documents/17/justif_domicile_amira_26', '2025-04-13 08:10:04'),
(22, 18, 1, 'cin_karim', 'uploads/documents/18/cin_karim_27', '2025-04-13 08:15:01'),
(23, 19, 1, 'cin_yasmine', 'uploads/documents/19/cin_yasmine_28', '2025-04-13 08:20:01'),
(24, 19, 3, 'certif_medical_yasmine', 'uploads/documents/19/certif_medical_yasmine_28', '2025-04-13 08:20:02'),
(25, 19, 4, 'photo_yasmine', 'uploads/documents/19/photo_yasmine_28', '2025-04-13 08:20:03'),
(26, 20, 1, 'cin_ahmed', 'uploads/documents/20/cin_ahmed_29', '2025-04-13 08:25:01'),
(27, 20, 3, 'certif_medical_ahmed', 'uploads/documents/20/certif_medical_ahmed_29', '2025-04-13 08:25:02'),
(28, 20, 4, 'photo_ahmed', 'uploads/documents/20/photo_ahmed_29', '2025-04-13 08:25:03'),
(29, 20, 5, 'justif_domicile_ahmed', 'uploads/documents/20/justif_domicile_ahmed_29', '2025-04-13 08:25:04');


INSERT INTO `inscription` (`id`, `cin`, `plan_id`, `statut`, `statut_paiement`, `cycle_paiement`, `date_inscription`, `date_paiement_suivant`, `created_at`) VALUES
(13, '12345678', 3, 'En Cours', 'unpaid', 'Mensuel', '2025-04-12', '2025-04-12', '2025-04-12 21:32:31'),
(14, '87654321', 1, 'En Cours', 'unpaid', 'Mensuel', '2025-04-12', '2025-05-12', '2025-04-12 21:33:33'),
(15, '08321456', 3, 'En Cours', 'paid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 08:00:05'),
(16, '08765123', 8, 'En Cours', 'unpaid', 'Trimestriel', '2025-04-13', '2025-07-13', '2025-04-13 08:05:04'),
(17, '09812345', 3, 'En Cours', 'paid', 'Totale', '2025-04-13', NULL, '2025-04-13 08:10:05'),
(18, '09123789', 1, 'En attente', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 08:15:02'),
(19, '08651234', 8, 'En Cours', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 08:20:04'),
(20, '07896543', 3, 'En Cours', 'paid', 'Totale', '2025-04-13', NULL, '2025-04-13 08:25:05'),
(21, '07123654', 1, 'En attente', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 08:30:01'),
(22, '07896123', 7, 'En Cours', 'unpaid', 'Trimestriel', '2025-04-13', '2025-07-13', '2025-04-13 08:35:01'),
(23, '06543219', 3, 'En Cours', 'paid', 'Totale', '2025-04-13', NULL, '2025-04-13 08:40:01'),
(24, '06789123', 1, 'En Cours', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 08:45:01'),
(25, '05432198', 8, 'En attente', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 08:50:01'),
(26, '05789456', 7, 'En Cours', 'paid', 'Totale', '2025-04-13', NULL, '2025-04-13 08:55:01'),
(27, '04567891', 3, 'En Cours', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 09:00:01'),
(28, '04123789', 3, 'En attente', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 09:05:01'),
(29, '03987654', 1, 'En Cours', 'paid', 'Totale', '2025-04-13', NULL, '2025-04-13 09:10:01'),
(30, '03456789', 7, 'En Cours', 'unpaid', 'Trimestriel', '2025-04-13', '2025-07-13', '2025-04-13 09:15:01'),
(31, '02345678', 8, 'En attente', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 09:20:01'),
(32, '02987654', 3, 'En Cours', 'paid', 'Totale', '2025-04-13', NULL, '2025-04-13 09:25:01'),
(33, '01876543', 3, 'En Cours', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 09:30:01'),
(34, '01234567', 1, 'En attente', 'unpaid', 'Mensuel', '2025-04-13', '2025-05-13', '2025-04-13 09:35:01');





--
-- Indexes for dumped tables
--

--
-- Indexes for table `auto_ecole`
--
ALTER TABLE `auto_ecole`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `candidat`
--
ALTER TABLE `candidat`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cin` (`cin`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `fk_candidat_type_permis` (`type_permis`);

--
-- Indexes for table `document`
--
ALTER TABLE `document`
  ADD PRIMARY KEY (`id`),
  ADD KEY `dossier_id` (`dossier_id`),
  ADD KEY `type_document_id` (`type_document_id`);

--
-- Indexes for table `dossier`
--
ALTER TABLE `dossier`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `candidat_id` (`candidat_id`);

--
-- Indexes for table `entretien`
--
ALTER TABLE `entretien`
  ADD PRIMARY KEY (`id`),
  ADD KEY `vehicule_id` (`vehicule_id`);

--
-- Indexes for table `examen`
--
ALTER TABLE `examen`
  ADD PRIMARY KEY (`id`),
  ADD KEY `candidat_id` (`candidat_id`),
  ADD KEY `type_examen_id` (`type_examen_id`);

--
-- Indexes for table `inscription`
--
ALTER TABLE `inscription`
  ADD PRIMARY KEY (`id`),
  ADD KEY `plan_id` (`plan_id`),
  ADD KEY `cin` (`cin`);

--
-- Indexes for table `moniteur`
--
ALTER TABLE `moniteur`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `cin` (`cin`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Indexes for table `moniteur_specialite`
--
ALTER TABLE `moniteur_specialite`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_moniteur_specialite` (`moniteur_id`,`type_permis_id`),
  ADD KEY `fk_moniteur_specialite_type_permis` (`type_permis_id`);

--
-- Indexes for table `paiement`
--
ALTER TABLE `paiement`
  ADD PRIMARY KEY (`id`),
  ADD KEY `inscription_id` (`inscription_id`),
  ADD KEY `id_examen` (`id_examen`),
  ADD KEY `id_candidat` (`id_candidat`);

--
-- Indexes for table `plan`
--
ALTER TABLE `plan`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_plan_type_permis` (`type_permis_id`);

--
-- Indexes for table `presence_code`
--
ALTER TABLE `presence_code`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_presence_code` (`session_code_id`,`candidat_id`),
  ADD KEY `presence_code_ibfk_1` (`candidat_id`);

--
-- Indexes for table `presence_conduite`
--
ALTER TABLE `presence_conduite`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_presence_conduite` (`session_conduite_id`,`candidat_id`),
  ADD KEY `presence_conduite_ibfk_1` (`candidat_id`);

--
-- Indexes for table `rendez_vous`
--
ALTER TABLE `rendez_vous`
  ADD PRIMARY KEY (`id`),
  ADD KEY `candidat_id` (`candidat_id`),
  ADD KEY `session_code_id` (`session_code_id`),
  ADD KEY `session_conduite_id` (`session_conduite_id`);

--
-- Indexes for table `salle`
--
ALTER TABLE `salle`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero` (`numero`);

--
-- Indexes for table `seance`
--
ALTER TABLE `seance`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_seance_moniteur` (`moniteur_id`),
  ADD KEY `fk_seance_candidat` (`candidat_id`),
  ADD KEY `fk_seance_vehicule` (`vehicule_id`);

--
-- Indexes for table `session_code`
--
ALTER TABLE `session_code`
  ADD PRIMARY KEY (`id`),
  ADD KEY `moniteur_id` (`moniteur_id`),
  ADD KEY `plan_id` (`plan_id`);

--
-- Indexes for table `session_conduite`
--
ALTER TABLE `session_conduite`
  ADD PRIMARY KEY (`id`),
  ADD KEY `moniteur_id` (`moniteur_id`),
  ADD KEY `vehicule_id` (`vehicule_id`),
  ADD KEY `plan_id` (`plan_id`);

--
-- Indexes for table `type_document`
--
ALTER TABLE `type_document`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `libelle` (`libelle`);

--
-- Indexes for table `type_examen`
--
ALTER TABLE `type_examen`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `libelle` (`libelle`);

--
-- Indexes for table `type_permis`
--
ALTER TABLE `type_permis`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`),
  ADD UNIQUE KEY `libelle` (`libelle`);

--
-- Indexes for table `vehicule`
--
ALTER TABLE `vehicule`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `immatriculation` (`immatriculation`),
  ADD KEY `type_permis_id` (`type_permis_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `auto_ecole`
--
ALTER TABLE `auto_ecole`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `candidat`
--
ALTER TABLE `candidat`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `document`
--
ALTER TABLE `document`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `dossier`
--
ALTER TABLE `dossier`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `entretien`
--
ALTER TABLE `entretien`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=66;

--
-- AUTO_INCREMENT for table `examen`
--
ALTER TABLE `examen`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `inscription`
--
ALTER TABLE `inscription`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `moniteur`
--
ALTER TABLE `moniteur`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `moniteur_specialite`
--
ALTER TABLE `moniteur_specialite`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `paiement`
--
ALTER TABLE `paiement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `plan`
--
ALTER TABLE `plan`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `presence_code`
--
ALTER TABLE `presence_code`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `presence_conduite`
--
ALTER TABLE `presence_conduite`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `rendez_vous`
--
ALTER TABLE `rendez_vous`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `salle`
--
ALTER TABLE `salle`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `seance`
--
ALTER TABLE `seance`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `session_code`
--
ALTER TABLE `session_code`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `session_conduite`
--
ALTER TABLE `session_conduite`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `type_document`
--
ALTER TABLE `type_document`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `type_examen`
--
ALTER TABLE `type_examen`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `type_permis`
--
ALTER TABLE `type_permis`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `vehicule`
--
ALTER TABLE `vehicule`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `candidat`
--
ALTER TABLE `candidat`
  ADD CONSTRAINT `fk_candidat_type_permis` FOREIGN KEY (`type_permis`) REFERENCES `type_permis` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `document`
--
ALTER TABLE `document`
  ADD CONSTRAINT `document_ibfk_1` FOREIGN KEY (`dossier_id`) REFERENCES `dossier` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `document_ibfk_2` FOREIGN KEY (`type_document_id`) REFERENCES `type_document` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `dossier`
--
ALTER TABLE `dossier`
  ADD CONSTRAINT `dossier_ibfk_1` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `entretien`
--
ALTER TABLE `entretien`
  ADD CONSTRAINT `entretien_ibfk_1` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicule` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `examen`
--
ALTER TABLE `examen`
  ADD CONSTRAINT `examen_ibfk_1` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `examen_ibfk_2` FOREIGN KEY (`type_examen_id`) REFERENCES `type_examen` (`id`);

--
-- Constraints for table `inscription`
--
ALTER TABLE `inscription`
  ADD CONSTRAINT `inscription_ibfk_1` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`),
  ADD CONSTRAINT `inscription_ibfk_2` FOREIGN KEY (`cin`) REFERENCES `candidat` (`cin`);

--
-- Constraints for table `moniteur_specialite`
--
ALTER TABLE `moniteur_specialite`
  ADD CONSTRAINT `fk_moniteur_specialite_moniteur` FOREIGN KEY (`moniteur_id`) REFERENCES `moniteur` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_moniteur_specialite_type_permis` FOREIGN KEY (`type_permis_id`) REFERENCES `type_permis` (`id`);

--
-- Constraints for table `paiement`
--
ALTER TABLE `paiement`
  ADD CONSTRAINT `paiement_ibfk_1` FOREIGN KEY (`inscription_id`) REFERENCES `inscription` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `paiement_ibfk_2` FOREIGN KEY (`id_examen`) REFERENCES `examen` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `paiement_ibfk_3` FOREIGN KEY (`id_candidat`) REFERENCES `candidat` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `plan`
--
ALTER TABLE `plan`
  ADD CONSTRAINT `fk_plan_type_permis` FOREIGN KEY (`type_permis_id`) REFERENCES `type_permis` (`id`);

--
-- Constraints for table `presence_code`
--
ALTER TABLE `presence_code`
  ADD CONSTRAINT `presence_code_ibfk_1` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `presence_code_ibfk_2` FOREIGN KEY (`session_code_id`) REFERENCES `session_code` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `presence_conduite`
--
ALTER TABLE `presence_conduite`
  ADD CONSTRAINT `presence_conduite_ibfk_1` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `presence_conduite_ibfk_2` FOREIGN KEY (`session_conduite_id`) REFERENCES `session_conduite` (`id`) ON DELETE CASCADE;

--
-- Constraints for table `rendez_vous`
--
ALTER TABLE `rendez_vous`
  ADD CONSTRAINT `rendez_vous_ibfk_1` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `rendez_vous_ibfk_2` FOREIGN KEY (`session_code_id`) REFERENCES `session_code` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `rendez_vous_ibfk_3` FOREIGN KEY (`session_conduite_id`) REFERENCES `session_conduite` (`id`) ON DELETE SET NULL;

-- Constraints for table `seance`
--
ALTER TABLE `seance`
  ADD CONSTRAINT `fk_seance_candidat` FOREIGN KEY (`candidat_id`) REFERENCES `candidat` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_seance_moniteur` FOREIGN KEY (`moniteur_id`) REFERENCES `moniteur` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `fk_seance_vehicule` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicule` (`id`) ON DELETE SET NULL;

--
-- Constraints for table `session_code`
--
ALTER TABLE `session_code`
  ADD CONSTRAINT `session_code_ibfk_1` FOREIGN KEY (`moniteur_id`) REFERENCES `moniteur` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `session_code_ibfk_2` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`);

--
-- Constraints for table `session_conduite`
--
ALTER TABLE `session_conduite`
  ADD CONSTRAINT `session_conduite_ibfk_1` FOREIGN KEY (`moniteur_id`) REFERENCES `moniteur` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `session_conduite_ibfk_2` FOREIGN KEY (`vehicule_id`) REFERENCES `vehicule` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `session_conduite_ibfk_3` FOREIGN KEY (`plan_id`) REFERENCES `plan` (`id`);

--
-- Constraints for table `vehicule`
--
ALTER TABLE `vehicule`
  ADD CONSTRAINT `vehicule_ibfk_1` FOREIGN KEY (`type_permis_id`) REFERENCES `type_permis` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;