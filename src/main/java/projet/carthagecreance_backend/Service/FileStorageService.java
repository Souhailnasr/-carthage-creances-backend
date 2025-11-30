package projet.carthagecreance_backend.Service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service pour gérer le stockage des fichiers uploadés
 */
public interface FileStorageService {
    
    /**
     * Sauvegarde un fichier et retourne l'URL
     * @param file Le fichier à sauvegarder
     * @param subdirectory Le sous-répertoire (ex: "huissier/documents")
     * @return L'URL du fichier sauvegardé
     */
    String storeFile(MultipartFile file, String subdirectory);
    
    /**
     * Charge un fichier
     * @param fileName Le nom du fichier
     * @param subdirectory Le sous-répertoire
     * @return La ressource du fichier
     */
    Resource loadFile(String fileName, String subdirectory);
    
    /**
     * Supprime un fichier
     * @param fileName Le nom du fichier
     * @param subdirectory Le sous-répertoire
     */
    void deleteFile(String fileName, String subdirectory);
    
    /**
     * Valide un fichier (taille et type)
     * @param file Le fichier à valider
     * @throws IllegalArgumentException si le fichier est invalide
     */
    void validateFile(MultipartFile file);
    
    /**
     * Valide si un fichier est un PDF valide (pour compatibilité avec DossierController)
     * @param file Le fichier à valider
     * @return true si le fichier est un PDF valide
     */
    boolean isValidPdfFile(MultipartFile file);
    
    /**
     * Sauvegarde un fichier et retourne le chemin (pour compatibilité avec DossierController)
     * @param file Le fichier à sauvegarder
     * @param subdirectory Le sous-répertoire
     * @return Le chemin du fichier sauvegardé
     */
    String saveFile(MultipartFile file, String subdirectory);
    
    /**
     * Supprime un fichier par son chemin complet (pour compatibilité avec DossierController)
     * @param filePath Le chemin complet du fichier
     */
    void deleteFile(String filePath);
}
