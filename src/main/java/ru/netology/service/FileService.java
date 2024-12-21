package ru.netology.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.entity.FileEntity;
import ru.netology.exception.FileNotFoundException;
import ru.netology.exception.FileStorageException;
import ru.netology.repository.FileRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Slf4j
@Service
public class FileService {

    private final FileRepository fileRepository;
    private final Path storagePath;

    @Autowired
    public FileService(FileRepository fileRepository, @Value("${file.storage.location}") String storageLocation) {
        this.fileRepository = fileRepository;
        this.storagePath = Paths.get(storageLocation);
        initStorage();
    }

    private void initStorage() {
        log.info("Attempting to create storage");
        try {
            Files.createDirectories(storagePath);
            log.info("Storage successfully created");
        } catch (IOException e) {
            log.error("Could not create storage directory");
            throw new FileStorageException("Could not create storage directory", e);
        }
    }

    public List<FileEntity> getFiles(int limit) {
        log.info("Fetching list of files with limit: {}", limit);
        return fileRepository.findAll().stream()
                .limit(limit)
                .toList();
    }

    public FileEntity uploadFile(String filename, MultipartFile file) {
        log.info("Attempting to upload file: {} (size: {} bytes)", filename, file.getSize());
        try {
            Path destination = storagePath.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            FileEntity fileEntity = new FileEntity(filename, destination.toString(), file.getSize());
            log.info("File '{}' successfully uploaded to '{}'", filename, destination);
            return fileRepository.save(fileEntity);
        } catch (IOException e) {
            log.error("Failed to upload file: {}", filename, e);
            throw new FileStorageException("Failed to upload file: " + filename, e);
        }
    }

    public Resource downloadFile(String filename) {
        log.info("Attempting to download file: {}", filename);
        try {
            Path filePath = storagePath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                log.info("File '{}' successfully downloaded", filename);
                return resource;
            } else {
                log.warn("File '{}' not found or not readable", filename);
                throw new FileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            log.error("Error reading file: {}", filename, e);
            throw new FileStorageException("Error reading file: " + filename, e);
        }
    }

    public boolean deleteFile(String filename) {
        log.info("Attempting to delete file: {}", filename);
        return fileRepository.findByFilename(filename)
                .map(file -> {
                    fileRepository.delete(file);
                    log.info("File '{}' successfully delete", file);
                    return true;
                })
                .orElse(false);
    }

    public FileEntity updateFile(String filename, String newFilename) {
        log.info("Attempting to rename file '{}' to '{}'", filename, newFilename);

        FileEntity fileEntity = fileRepository.findByFilename(filename)
                .orElseThrow(() -> {
                    log.warn("File '{}' not found during rename", filename);
                    return new FileNotFoundException("File not found: " + filename);
                });

        Path sourcePath = Paths.get(fileEntity.getFilepath());
        Path targetPath = storagePath.resolve(newFilename);

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            fileEntity.setFilename(newFilename);
            fileEntity.setFilepath(targetPath.toString());
            log.info("File '{}' successfully renamed to '{}'", filename, newFilename);
            return fileRepository.save(fileEntity);
        } catch (IOException e) {
            log.error("Failed to rename file '{}' to '{}'", filename, newFilename, e);
            throw new FileStorageException("Error renaming file: " + filename, e);
        }
    }
}
