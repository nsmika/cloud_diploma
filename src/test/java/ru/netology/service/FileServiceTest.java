package ru.netology.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.entity.FileEntity;
import ru.netology.exception.FileNotFoundException;
import ru.netology.repository.FileRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private MultipartFile multipartFile;

    private FileService fileService;

    private Path tempStoragePath;

    @BeforeEach
    void setup() throws IOException {
        MockitoAnnotations.openMocks(this);

        // Создание временной директории
        tempStoragePath = Files.createTempDirectory("test_storage");

        // Явная инициализация FileService с временной директорией
        fileService = new FileService(fileRepository, tempStoragePath.toString());
    }

    @AfterEach
    void cleanup() throws IOException {
        // Удаление временной директории после тестов
        Files.walk(tempStoragePath)
                .map(Path::toFile)
                .forEach(file -> {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                });
    }

    @Test
    void getFiles_ShouldReturnLimitedFiles() {
        List<FileEntity> mockFiles = List.of(
                new FileEntity("file1.txt", "/path/file1.txt", 123L),
                new FileEntity("file2.txt", "/path/file2.txt", 456L)
        );

        when(fileRepository.findAll()).thenReturn(mockFiles);

        List<FileEntity> result = fileService.getFiles(1);

        assertEquals(1, result.size());
        assertEquals("file1.txt", result.get(0).getFilename());
    }

    @Test
    void uploadFile_ShouldReturnSavedFileEntity() throws IOException {
        String filename = "file1.txt";
        long fileSize = 123L;

        when(multipartFile.getOriginalFilename()).thenReturn(filename);
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getInputStream()).thenReturn(mock(InputStream.class));
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileEntity result = fileService.uploadFile(filename, multipartFile);

        assertEquals(filename, result.getFilename());
        assertEquals(fileSize, result.getFilesize());
        verify(fileRepository).save(any(FileEntity.class));
    }

    @Test
    void deleteFile_ShouldReturnTrue_WhenFileExists() {
        String filename = "file1.txt";
        FileEntity mockFile = new FileEntity(filename, tempStoragePath.resolve(filename).toString(), 123L);

        when(fileRepository.findByFilename(filename)).thenReturn(Optional.of(mockFile));

        boolean result = fileService.deleteFile(filename);

        assertTrue(result);
        verify(fileRepository).delete(mockFile);
    }

    @Test
    void deleteFile_ShouldReturnFalse_WhenFileDoesNotExist() {
        String filename = "nonexistent.txt";

        when(fileRepository.findByFilename(filename)).thenReturn(Optional.empty());

        boolean result = fileService.deleteFile(filename);

        assertFalse(result);
        verify(fileRepository, never()).delete(any());
    }

    @Test
    void updateFile_ShouldRenameAndReturnUpdatedFile() throws IOException {
        String filename = "file1.txt";
        String newFilename = "newFile.txt";

        // Создать файл в временной директории
        Path filePath = tempStoragePath.resolve(filename);
        Files.createFile(filePath);

        FileEntity mockFile = new FileEntity(filename, filePath.toString(), 123L);

        when(fileRepository.findByFilename(filename)).thenReturn(Optional.of(mockFile));
        when(fileRepository.save(any(FileEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FileEntity result = fileService.updateFile(filename, newFilename);

        assertEquals(newFilename, result.getFilename());
        assertTrue(result.getFilepath().endsWith(newFilename));
        assertTrue(Files.exists(tempStoragePath.resolve(newFilename))); // Проверить, что файл переименован
    }


    @Test
    void updateFile_ShouldThrowException_WhenFileDoesNotExist() {
        String filename = "nonexistent.txt";
        String newFilename = "newFile.txt";

        when(fileRepository.findByFilename(filename)).thenReturn(Optional.empty());

        assertThrows(FileNotFoundException.class, () -> fileService.updateFile(filename, newFilename));
    }
}
