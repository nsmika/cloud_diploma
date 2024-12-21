package ru.netology.controller;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import ru.netology.dto.FileDto;
import ru.netology.entity.FileEntity;
import ru.netology.security.JwtTokenProvider;
import ru.netology.service.FileService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class FileControllerTest {

    @Mock
    private FileService fileService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private FileController fileController;

    FileControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFiles_ShouldReturnFileList() {
        // Arrange
        String token = "validToken";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn("user@test.org");

        List<FileEntity> mockFiles = List.of(
                new FileEntity("file1.txt", "/path/file1.txt", 123L)
        );
        when(fileService.getFiles(3)).thenReturn(mockFiles);

        // Act
        ResponseEntity<List<FileDto>> response = fileController.getFiles("Bearer " + token, 3);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals("file1.txt", response.getBody().get(0).getFilename());
    }
}
