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
        log.info("Received token: {}", token);

        if (!jwtTokenProvider.validateToken(token)) {
            log.warn("Invalid token");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        log.info("Fetching files for user: {}", username);

        return ResponseEntity.ok(fileService.getFiles(limit));
    }

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(@RequestParam("filename") String filename,
                                        @RequestParam("file") MultipartFile file) {
        fileService.uploadFile(filename, file);
        return ResponseEntity.ok("File uploaded successfully");
    }

    @GetMapping("/file")
    public ResponseEntity<Resource> downloadFile(@RequestParam("filename") String filename) {
        Resource resource = fileService.downloadFile(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(@RequestParam("filename") String filename) {
        fileService.deleteFile(filename);
        return ResponseEntity.ok("File deleted successfully");
    }

    @PutMapping("/file")
    public ResponseEntity<?> updateFile(@RequestParam("filename") String filename,
                                        @RequestBody Map<String, String> body) {
        String newFilename = body.get("filename");
        fileService.updateFile(filename, newFilename);
        return ResponseEntity.ok("File renamed successfully");
    }
}
