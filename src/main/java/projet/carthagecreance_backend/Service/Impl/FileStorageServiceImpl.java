package projet.carthagecreance_backend.Service.Impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import projet.carthagecreance_backend.Service.FileStorageService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Implémentation du service de stockage de fichiers
 */
@Service
public class FileStorageServiceImpl implements FileStorageService {
    
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    
    @Value("${file.base-url:http://localhost:8089/carthage-creance/api/files}")
    private String baseUrl;
    
    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024; // 20MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "application/pdf",
        "image/jpeg",
        "image/jpg",
        "image/png"
    );
    
    @Override
    public String storeFile(MultipartFile file, String subdirectory) {
        try {
            // Valider le fichier
            validateFile(file);
            
            // Générer un nom unique
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new IllegalArgumentException("Le nom du fichier est requis");
            }
            
            String extension = "";
            int lastDotIndex = originalFilename.lastIndexOf(".");
            if (lastDotIndex > 0) {
                extension = originalFilename.substring(lastDotIndex);
            }
            
            String fileName = UUID.randomUUID().toString() + extension;
            
            // Créer le répertoire si nécessaire
            Path directory = Paths.get(uploadDir, subdirectory);
            Files.createDirectories(directory);
            
            // Sauvegarder le fichier
            Path filePath = directory.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Retourner l'URL
            return baseUrl + "/" + subdirectory + "/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du fichier: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Resource loadFile(String fileName, String subdirectory) {
        try {
            // Sécuriser le nom du fichier (éviter les path traversal)
            Path filePath = Paths.get(uploadDir, subdirectory, fileName).normalize();
            Path uploadPath = Paths.get(uploadDir, subdirectory).normalize();
            
            if (!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("Accès non autorisé au fichier: " + fileName);
            }
            
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non trouvé ou non lisible: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur lors du chargement du fichier: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteFile(String fileName, String subdirectory) {
        try {
            // Sécuriser le nom du fichier
            Path filePath = Paths.get(uploadDir, subdirectory, fileName).normalize();
            Path uploadPath = Paths.get(uploadDir, subdirectory).normalize();
            
            if (!filePath.startsWith(uploadPath)) {
                throw new RuntimeException("Accès non autorisé au fichier: " + fileName);
            }
            
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la suppression du fichier: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier est requis");
        }
        
        // Vérifier la taille
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Le fichier est trop volumineux. Taille maximale : 20MB");
        }
        
        // Vérifier le type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Formats acceptés : PDF, JPEG, PNG");
        }
    }
    
    @Override
    public boolean isValidPdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        return "application/pdf".equals(contentType);
    }
    
    @Override
    public String saveFile(MultipartFile file, String subdirectory) {
        // Utiliser storeFile mais retourner juste le chemin relatif pour compatibilité
        String fullUrl = storeFile(file, subdirectory);
        // Extraire le chemin relatif depuis l'URL
        String baseUrlPrefix = baseUrl + "/" + subdirectory + "/";
        if (fullUrl.startsWith(baseUrlPrefix)) {
            return subdirectory + "/" + fullUrl.substring(baseUrlPrefix.length());
        }
        return fullUrl;
    }
    
    @Override
    public void deleteFile(String filePath) {
        // Parser le chemin pour extraire le sous-répertoire et le nom de fichier
        if (filePath == null || filePath.isEmpty()) {
            return;
        }
        
        // Si le chemin contient un slash, extraire le sous-répertoire et le nom de fichier
        int lastSlashIndex = filePath.lastIndexOf("/");
        if (lastSlashIndex > 0) {
            String subdirectory = filePath.substring(0, lastSlashIndex);
            String fileName = filePath.substring(lastSlashIndex + 1);
            deleteFile(fileName, subdirectory);
        } else {
            // Si pas de slash, chercher dans le répertoire racine
            deleteFile(filePath, "");
        }
    }
}

