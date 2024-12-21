package ru.netology.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.dto.FileDto;
import ru.netology.entity.FileEntity;
import ru.netology.security.JwtTokenProvider;
import ru.netology.service.FileService;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping()
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/list")
    public ResponseEntity<List<FileDto>> getFiles(@RequestHeader("auth-token") String authHeader, @RequestParam int limit) {
        String token = authHeader.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid token");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        log.info("Fetching files for user: {}", username);

        List<FileEntity> files = fileService.getFiles(limit);

        List<FileDto> fileDto = files.stream()
                .map(file -> new FileDto(file.getFilename(), file.getFilesize()))
                .toList();

        return ResponseEntity.ok(fileDto);
    }

    @PostMapping("/file")
    public ResponseEntity<FileDto> uploadFile(@RequestParam("filename") String filename, @RequestParam("file") MultipartFile file) {
        FileEntity fileEntity = fileService.uploadFile(filename, file);

        FileDto fileDto = new FileDto(fileEntity.getFilename(), fileEntity.getFilesize());
        return ResponseEntity.ok(fileDto);
    }


    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String filename) {
        Resource resource = fileService.downloadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @DeleteMapping("/file")
    public ResponseEntity<String> deleteFile(@RequestParam("filename") String filename) {
        boolean deleted = fileService.deleteFile(filename);
        if (deleted) {
            return ResponseEntity.ok("File deleted successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }
    }

    @PutMapping("/file")
    public ResponseEntity<FileDto> updateFile(@RequestParam("filename") String filename, @RequestBody Map<String, String> body) {
        String newFilename = body.get("filename");
        FileEntity updatedFile = fileService.updateFile(filename, newFilename);

        FileDto fileDto = new FileDto(updatedFile.getFilename(), updatedFile.getFilesize());
        return ResponseEntity.ok(fileDto);
    }
}
