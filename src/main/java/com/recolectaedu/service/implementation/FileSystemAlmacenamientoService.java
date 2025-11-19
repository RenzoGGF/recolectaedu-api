package com.recolectaedu.service.implementation;

import com.recolectaedu.exception.AlmacenamientoException;
import com.recolectaedu.service.IAlmacenamientoService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileSystemAlmacenamientoService implements IAlmacenamientoService {

    @Value("${storage.location:uploads}")
    private String storageLocation;

    private Path rootLocation;

    private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "doc", "docx");

    @PostConstruct
    public void init() {
        this.rootLocation = Paths.get(storageLocation);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new AlmacenamientoException("No se pudo inicializar el directorio de almacenamiento", e);
        }
    }

    @Override
    public String almacenar(MultipartFile archivo) {
        if (archivo.isEmpty()) {
            throw new AlmacenamientoException("No se puede almacenar un archivo vacío.");
        }

        // Validar tamaño
        if (archivo.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new AlmacenamientoException("El archivo excede el tamaño máximo permitido de 20 MB.");
        }

        // Validar extensión
        String extension = StringUtils.getFilenameExtension(archivo.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new AlmacenamientoException("Extensión de archivo no permitida. Solo se aceptan: " + ALLOWED_EXTENSIONS);
        }

        // Generar nombre único y almacenar
        String nombreArchivoOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
        String nombreArchivoUnico = UUID.randomUUID().toString() + "_" + nombreArchivoOriginal;

        try (InputStream inputStream = archivo.getInputStream()) {
            Path destinationFile = this.rootLocation.resolve(Paths.get(nombreArchivoUnico)).normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new AlmacenamientoException("No se puede almacenar el archivo fuera del directorio actual.");
            }
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return nombreArchivoUnico;
        } catch (IOException e) {
            throw new AlmacenamientoException("Error al almacenar el archivo " + nombreArchivoOriginal, e);
        }
    }

    @Override
    public void eliminar(String nombreArchivo) {
        try {
            Path file = rootLocation.resolve(nombreArchivo);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new AlmacenamientoException("Error al eliminar el archivo", e);
        }
    }

    @Override
    public Resource cargarComoRecurso(String nombreArchivo) {
        try {
            Path file = rootLocation.resolve(nombreArchivo).normalize();
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new AlmacenamientoException("No se puede leer el archivo: " + nombreArchivo);
            }
        } catch (MalformedURLException e) {
            throw new AlmacenamientoException("Error al cargar el archivo: " + nombreArchivo, e);
        }
    }
}
