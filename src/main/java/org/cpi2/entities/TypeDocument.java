package org.cpi2.entities;

public enum TypeDocument {
    CIN,
    PERMIS,
    CERTIFICAT_MEDICAL,
    PHOTO,
    PROOF_OF_RESIDENCE,
    AUTRE;

    public static TypeDocument getTypeDocument(String type) {
        switch (type) {
            case "CIN":
                return CIN;
            case "PERMIS":
                return PERMIS;
            case "CERTIFICAT_MEDICAL":
                return CERTIFICAT_MEDICAL;
            case "PHOTO":
                return PHOTO;
            case "PROOF_OF_RESIDENCE":
                return PROOF_OF_RESIDENCE;
            case "AUTRE":
                return AUTRE;
            default:
                return null;
        }
    }
}
