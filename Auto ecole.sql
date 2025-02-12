-- Structure de la base de données pour auto-école

-- Table des types énumérés
CREATE TABLE type_permis (
    id INT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL
);

CREATE TABLE type_document (
    id INT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL
);
-- Fs5t status table useless ig
CREATE TABLE type_examen (
    id INT PRIMARY KEY,
    libelle VARCHAR(50) NOT NULL
);

-- Table pour l'auto-école
CREATE TABLE auto_ecole (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(100) NOT NULL,
    adresse TEXT NOT NULL,
    logo_path VARCHAR(255),
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table pour les moniteurs
CREATE TABLE moniteur (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    cin VARCHAR(20) UNIQUE NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(100) UNIQUE,
    date_embauche DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Table de spécialités des moniteurs
CREATE TABLE moniteur_specialite (
    moniteur_id INT,
    type_permis_id INT,
    PRIMARY KEY (moniteur_id, type_permis_id),
    FOREIGN KEY (moniteur_id) REFERENCES moniteur(id),
    FOREIGN KEY (type_permis_id) REFERENCES type_permis(id)
);

-- Table pour les candidats
CREATE TABLE candidat (
    id INT PRIMARY KEY AUTO_INCREMENT,
    nom VARCHAR(50) NOT NULL,
    prenom VARCHAR(50) NOT NULL,
    cin VARCHAR(20) UNIQUE NOT NULL,
    adresse TEXT,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(100) UNIQUE,
    --idk ig zeyda hedhi lena fl inscription tnjm tjibha + kol candidate 3andou akther mn type 
    --type_permis_id INT NOT NULL,
    --date_inscription DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (type_permis_id) REFERENCES type_permis(id)
);

-- Table pour les dossiers
CREATE TABLE dossier (
    id INT PRIMARY KEY AUTO_INCREMENT,
    candidat_id INT UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidat_id) REFERENCES candidat(id)
);

-- Table pour les documents
CREATE TABLE document (
    id INT PRIMARY KEY AUTO_INCREMENT,
    dossier_id INT NOT NULL,
    type_document_id INT NOT NULL,
    nom_fichier VARCHAR(255) NOT NULL,
    chemin_fichier VARCHAR(255) NOT NULL,
    date_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (dossier_id) REFERENCES dossier(id),
    FOREIGN KEY (type_document_id) REFERENCES type_document(id)
);

-- Table pour les véhicules
CREATE TABLE vehicule (
    id INT PRIMARY KEY AUTO_INCREMENT,
    immatriculation VARCHAR(20) UNIQUE NOT NULL,
    type_permis_id INT NOT NULL,
    marque VARCHAR(50) NOT NULL,
    modele VARCHAR(50) NOT NULL,
    date_mise_service DATE NOT NULL,
    kilometrage_total INT NOT NULL DEFAULT 0,
    kilometrage_prochain_entretien INT NOT NULL,
    date_derniere_visite_technique DATE,
    date_prochaine_visite_technique DATE,
    date_expiration_assurance DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (type_permis_id) REFERENCES type_permis(id)
);

-- Table pour l'entretien des véhicules
CREATE TABLE entretien (
    id INT PRIMARY KEY AUTO_INCREMENT,
    vehicule_id INT NOT NULL,
    date_entretien DATE NOT NULL,
    type_entretien VARCHAR(100) NOT NULL,
    description TEXT,
    cout DECIMAL(10,2) NOT NULL,
    kilometrage INT NOT NULL,
    facture_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicule_id) REFERENCES vehicule(id)
);

-- Table pour les sessions de code
CREATE TABLE session_code (
    id INT PRIMARY KEY AUTO_INCREMENT,
    date_session DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    moniteur_id INT NOT NULL,
    prix DECIMAL(10,2) NOT NULL,
    statut_id VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (moniteur_id) REFERENCES moniteur(id),
);

-- Table pour les sessions de conduite
CREATE TABLE session_conduite (
    id INT PRIMARY KEY AUTO_INCREMENT,
    date_session DATE NOT NULL,
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    moniteur_id INT NOT NULL,
    vehicule_id INT NOT NULL,
    point_rencontre_lat DECIMAL(10,8),
    point_rencontre_lon DECIMAL(11,8),
    kilometres_parcourus INT,
    prix DECIMAL(10,2) NOT NULL,
    statut_id   VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (moniteur_id) REFERENCES moniteur(id),
    FOREIGN KEY (vehicule_id) REFERENCES vehicule(id),
    FOREIGN KEY (statut_id) REFERENCES statut_session(id)
);

-- Table pour les rendez-vous
CREATE TABLE rendez_vous (
    id INT PRIMARY KEY AUTO_INCREMENT,
    candidat_id INT NOT NULL,
    --session_code_id INT,
    session_conduite_id INT,
    confirme BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidat_id) REFERENCES candidat(id),
    --FOREIGN KEY (session_code_id) REFERENCES session_code(id),
    FOREIGN KEY (session_conduite_id) REFERENCES session_conduite(id),
    CHECK (session_code_id IS NOT NULL OR session_conduite_id IS NOT NULL)
);

-- Table pour les examens
CREATE TABLE examen (
    id INT PRIMARY KEY AUTO_INCREMENT,
    candidat_id INT NOT NULL,
    type_examen_id INT NOT NULL,
    date_examen DATE NOT NULL,
    frais DECIMAL(10,2) NOT NULL,
    resultat BOOLEAN,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidat_id) REFERENCES candidat(id),
    FOREIGN KEY (type_examen_id) REFERENCES type_examen(id)
);

-- Table pour les paiements
CREATE TABLE paiement (
    id INT PRIMARY KEY AUTO_INCREMENT,
    candidat_id INT NOT NULL,
    montant DECIMAL(10,2) NOT NULL,
    date_paiement DATE NOT NULL,
    type_paiement VARCHAR(50) NOT NULL, -- 'HEURES' ou 'EXAMEN'
    statut_id Varchar(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidat_id) REFERENCES candidat(id),
    FOREIGN KEY (statut_id) REFERENCES statut_paiement(id)
);

-- Insertion des données de base
INSERT INTO type_permis (id, libelle) VALUES
(1, 'A - Moto'),
(2, 'B - Voiture'),
(3, 'C - Camion');

INSERT INTO type_document (id, libelle) VALUES
(1, 'CIN'),
(2, 'Certificat Médical'),
(3, 'Photo'),
(4, 'Autre');

INSERT INTO type_examen (id, libelle) VALUES
(1, 'Code'),
(2, 'Circuit'),
(3, 'Créneaux');
