package ru.netology.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.dto.FileDto;
import ru.netology.entity.FileEntity;
import ru.netology.exception.FileNotFoundException;
import ru.netology.repository.FileRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

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
        try {
            Files.createDirectories(storagePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    public List<FileDto> getFiles(int limit) {
        return fileRepository.findAll().stream()
                .limit(limit)
                .map(file -> new FileDto(file.getFilename(), file.getFilesize()))
                .collect(Collectors.toList());
    }

    public void uploadFile(String filename, MultipartFile file) {
        try {
            Path destination = storagePath.resolve(filename);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

            FileEntity fileEntity = new FileEntity(filename, destination.toString(), file.getSize());
            fileRepository.save(fileEntity);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file " + filename, e);
        }
    }

    public Resource downloadFile(String filename) {
        try {
            Path filePath = storagePath.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error reading file: " + filename, e);
        }
    }

    public void deleteFile(String filename) {
        FileEntity fileEntity = fileRepository.findByFilename(filename)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + filename));

        try {
            Path filePath = Paths.get(fileEntity.getFilepath());
            Files.deleteIfExists(filePath);
            fileRepository.delete(fileEntity);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting file: " + filename, e);
        }
    }

    public void updateFile(String filename, String newFilename) {
        FileEntity fileEntity = fileRepository.findByFilename(filename)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + filename));

        Path sourcePath = Paths.get(fileEntity.getFilepath());
        Path targetPath = storagePath.resolve(newFilename);

        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            fileEntity.setFilename(newFilename);
            fileEntity.setFilepath(targetPath.toString());
            fileRepository.save(fileEntity);
        } catch (IOException e) {
            throw new RuntimeException("Error renaming file: " + filename, e);
        }
    }
}
