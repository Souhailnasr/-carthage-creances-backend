package projet.carthagecreance_backend.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    
    private static final String UPLOAD_DIR = "uploads/dossiers/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String PDF_EXTENSION = "pdf";
    
    /**
     * Sauvegarde un fichier PDF dans le dossier uploads/dossiers/
     * @param file Le fichier à sauvegarder
     * @param prefix Le préfixe pour le nom du fichier (ex: "contrat", "pouvoir")
     * @return Le chemin relatif du fichier sauvegardé
     * @throws IOException Si une erreur survient lors de la sauvegarde
     * @throws IllegalArgumentException Si le fichier n'est pas valide
     */
    public String saveFile(MultipartFile file, String prefix) throws IOException {
        // Validation du fichier
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier ne peut pas être vide");
        }
        
        if (!isValidPdfFile(file)) {
            throw new IllegalArgumentException("Seuls les fichiers PDF sont autorisés");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("La taille du fichier ne peut pas dépasser 10MB");
        }
        
        // Créer le dossier s'il n'existe pas
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        String uniqueFilename = prefix + "_" + UUID.randomUUID().toString() + "." + extension;
        
        // Sauvegarder le fichier
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Retourner le chemin relatif
        return UPLOAD_DIR + uniqueFilename;
    }
    
    /**
     * Supprime un fichier du système
     * @param filePath Le chemin du fichier à supprimer
     * @throws IOException Si une erreur survient lors de la suppression
     */
    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
    
    /**
     * Valide qu'un fichier est bien un PDF
     * @param file Le fichier à valider
     * @return true si le fichier est un PDF valide, false sinon
     */
    public boolean isValidPdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType != null && contentType.equals(PDF_CONTENT_TYPE)) {
            return true;
        }
        
        // Vérifier l'extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = FilenameUtils.getExtension(originalFilename).toLowerCase();
            return PDF_EXTENSION.equals(extension);
        }
        
        return false;
    }
    
    /**
     * Vérifie si un fichier existe
     * @param filePath Le chemin du fichier
     * @return true si le fichier existe, false sinon
     */
    public boolean fileExists(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        Path path = Paths.get(filePath);
        return Files.exists(path);
    }
    
    /**
     * Obtient la taille d'un fichier
     * @param filePath Le chemin du fichier
     * @return La taille du fichier en octets, -1 si le fichier n'existe pas
     */
    public long getFileSize(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return -1;
        }
        
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.size(path);
            }
        } catch (IOException e) {
            // Log l'erreur si nécessaire
        }
        
        return -1;
    }
}
