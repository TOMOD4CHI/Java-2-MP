package service;

import entitties.AutoEcole;
import repository.AutoEcoleRepository;

public class AutoEcoleService {
    private final AutoEcoleRepository autoEcoleRepo = new AutoEcoleRepository();

    public AutoEcole viewautoEcole() {
        return autoEcoleRepo.findById(0).orElse(null);
    }

    public boolean modifyNom(String nouveauNom) {
        if (nouveauNom != null && !nouveauNom.isEmpty()) {
            AutoEcole autoEcole = autoEcoleRepo.findById(0).orElse(null);
            if (autoEcole != null) {
                autoEcole.setNom(nouveauNom);
                return autoEcoleRepo.update(autoEcole);
            }
        }
        return false;
    }

    public boolean modifyAddress(String nouvelleAdresse) {
        if (nouvelleAdresse != null && !nouvelleAdresse.isEmpty()) {
            AutoEcole autoEcole = autoEcoleRepo.findById(0).orElse(null);
            if (autoEcole != null) {
                autoEcole.setAdresse(nouvelleAdresse);
                return autoEcoleRepo.update(autoEcole);
            }
        }
        return false;
    }
    public boolean modifyTelephone(String nouveauTelephone) {
        if (nouveauTelephone != null && !nouveauTelephone.isEmpty()) {
            AutoEcole autoEcole = autoEcoleRepo.findById(0).orElse(null);
            if (autoEcole != null) {
                autoEcole.setTelephone(nouveauTelephone);
                return autoEcoleRepo.update(autoEcole);
            }
        }
        return false;
    }

    public boolean modifyEmail(String nouveauEmail) {
        if (nouveauEmail != null && !nouveauEmail.isEmpty()) {
            AutoEcole autoEcole = autoEcoleRepo.findById(0).orElse(null);
            if (autoEcole != null) {
                autoEcole.setEmail(nouveauEmail);
                return autoEcoleRepo.update(autoEcole);
            }
        }
        return false;
    }

    public boolean modifyLogoPath(String nouveauLogoPath) {
        if (nouveauLogoPath != null && !nouveauLogoPath.isEmpty()) {
            AutoEcole autoEcole = autoEcoleRepo.findById(0).orElse(null);
            if (autoEcole != null) {
                autoEcole.setLogoPath(nouveauLogoPath);
                return autoEcoleRepo.update(autoEcole);
            }
        }
        return false;
    }
}
