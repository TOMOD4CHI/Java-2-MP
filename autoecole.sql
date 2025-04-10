-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Apr 05, 2025 at 01:31 AM
-- Server version: 10.4.32-MariaDB
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

INSERT INTO `auto_ecole` (`id`, `nom`, `adresse`, `logo_path`, `telephone`, `email`, `username`, `password`, `created_at`) VALUES
(1, 'Auto École Exemplaire', 'Avenue des Conducteurs 123, Ville', 'logos/default_logo.png', '12345678', 'contact@autoecole.com', 'user', 'pass', '2025-03-22 13:11:27');

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

INSERT INTO `candidat` (`id`, `nom`, `prenom`, `cin`, `adresse`, `telephone`, `email`, `date_naissance`, `type_permis`, `created_at`) VALUES
(13, 'test', 'ben test', '78787878', 'fsdfsdfsdf', '45784521', 'fsfsfd@dfd.df', '2025-03-27', NULL, '2025-03-27 19:26:08');

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

INSERT INTO `document` (`id`, `dossier_id`, `type_document_id`, `nom_fichier`, `chemin_fichier`, `date_upload`) VALUES
(4, 4, 2, 'Goated Panel.jpg', 'uploads/documents/4/8428d5ed-4405-4517-941e-e7f2bdc8bf4b.jpg', '2025-03-27 19:43:22');

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

INSERT INTO `dossier` (`id`, `candidat_id`, `statut`, `date_creation`, `notes`, `created_at`) VALUES
(4, 13, 'En attente', NULL, NULL, '2025-03-27 19:26:08');

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
  `entretien_suivant` date NOT NULL,
  `maintenance` int(1) NOT NULL DEFAULT 0,
  `kilometrage` int(11) NOT NULL,
  `facture_path` varchar(255) DEFAULT NULL,
  `statut` varchar(50) DEFAULT 'Planifié',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

INSERT INTO `inscription` (`id`, `cin`, `plan_id`, `statut`, `statut_paiement`, `cycle_paiement`, `date_inscription`, `date_paiement_suivant`, `created_at`) VALUES
(6, '78787878', 3, 'En Cours', 'paid', 'Totale', '2025-03-27', '2025-04-27', '2025-03-27 19:58:07');

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
  `reference` varchar(100) DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `paiement`
--

INSERT INTO `paiement` (`id`, `id_candidat`, `inscription_id`, `id_examen`, `type_paiement`, `montant`, `date_paiement`, `mode_paiement`, `reference`, `notes`, `created_at`) VALUES
(2, 13, 6, NULL, 'Totale', 349.99, '2025-04-03', 'CARTE_BANCAIRE', NULL, 'Test', '2025-04-03 20:18:14');

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

INSERT INTO `plan` (`id`, `libelle`, `description`, `type_permis_id`, `prix`, `duree`, `heures_code`, `heures_conduite`, `actif`, `created_at`) VALUES
(1, 'Basic Motorcycle Course', 'Fundamental motorcycle training for beginners', 1, 199.99, 2, 10, 10, 1, '2025-03-23 22:54:51'),
(3, 'Basic Car Driving Course', 'Essential car driving skills for new drivers', 2, 349.99, 3, 10, 10, 1, '2025-03-23 22:54:51'),
(7, 'Heavy Truck License Course', 'Complete training for heavy goods vehicle license', 3, 899.99, 4, 10, 10, 1, '2025-03-23 22:54:51');

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
  `prix` decimal(10,2) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

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

INSERT INTO `type_document` (`id`, `libelle`, `description`, `obligatoire`, `created_at`) VALUES
(1, 'CIN', 'Carte d\'identité nationale', 1, '2025-03-23 22:49:32'),
(2, 'PERMIS_A', 'Permis de conduire d\'un Moto', 0, '2025-03-23 22:49:32'),
(3, 'CERTIFICAT_MEDICAL', 'Certificat d\'aptitude médicale', 1, '2025-03-23 22:49:32'),
(4, 'PHOTO', 'Photo d\'identité', 1, '2025-03-23 22:49:32'),
(5, 'PROOF_OF_RESIDENCE', 'Justificatif de domicile', 1, '2025-03-23 22:49:32'),
(6, 'AUTRE', 'Autre document', 0, '2025-03-23 22:49:32'),
(7, 'PERMIS_B', 'Permis de Counduite d\'une Voiture', 1, '2025-03-27 19:33:03'),
(8, 'PERMIS_C', 'Permis de counduite d\'un Camion', 1, '2025-03-27 19:33:36');

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

INSERT INTO `type_examen` (`id`, `libelle`, `description`, `cout`, `created_at`) VALUES
(1, 'Code', 'Examen du code de la route', 100.00, '2025-03-22 13:11:28'),
(2, 'Circuit', 'Examen de conduite en circuit fermé', 150.00, '2025-03-22 13:11:28'),
(3, 'Créneaux', 'Examen des manœuvres de stationnement', 100.00, '2025-03-22 13:11:28'),
(4, 'Conduite', 'Examen de conduite en circulation', 200.00, '2025-03-22 13:11:28');

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

INSERT INTO `type_permis` (`id`, `code`, `libelle`, `description`, `age_minimum`, `created_at`) VALUES
(1, 'A', 'Moto', 'Permis pour conduire une moto', 18, '2025-03-22 13:11:28'),
(2, 'B', 'Voiture', 'Permis pour conduire une voiture', 18, '2025-03-22 13:11:28'),
(3, 'C', 'Camion', 'Permis pour conduire un camion', 21, '2025-03-22 13:11:28');

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
  `kilometrage_prochain_entretien` int(11) NOT NULL,
  `date_prochain_entretien` date DEFAULT NULL,
  `date_derniere_visite_technique` date DEFAULT NULL,
  `date_prochaine_visite_technique` date DEFAULT NULL,
  `date_expiration_assurance` date DEFAULT NULL,
  `statut` varchar(20) DEFAULT 'Disponible',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `vehicule`
--

INSERT INTO `vehicule` (`id`, `immatriculation`, `type_permis_id`, `marque`, `modele`, `annee`, `date_mise_service`, `kilometrage_total`, `kilometrage_prochain_entretien`, `date_prochain_entretien`, `date_derniere_visite_technique`, `date_prochaine_visite_technique`, `date_expiration_assurance`, `statut`, `created_at`) VALUES
(2, '4564655', 2, 'TOYOTA', 'Supra', NULL, '2025-04-02', 120, 0, NULL, NULL, NULL, NULL, 'Disponible', '2025-04-04 23:29:43');

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
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT for table `document`
--
ALTER TABLE `document`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `dossier`
--
ALTER TABLE `dossier`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- AUTO_INCREMENT for table `entretien`
--
ALTER TABLE `entretien`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `examen`
--
ALTER TABLE `examen`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `inscription`
--
ALTER TABLE `inscription`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `moniteur`
--
ALTER TABLE `moniteur`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `moniteur_specialite`
--
ALTER TABLE `moniteur_specialite`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `paiement`
--
ALTER TABLE `paiement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `plan`
--
ALTER TABLE `plan`
  MODIFY `id` int(10) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `presence_code`
--
ALTER TABLE `presence_code`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

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
-- AUTO_INCREMENT for table `seance`
--
ALTER TABLE `seance`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `session_code`
--
ALTER TABLE `session_code`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

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

--
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
