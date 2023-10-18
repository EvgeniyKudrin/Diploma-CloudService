package ru.netology.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.dto.FileDto;
import ru.netology.dto.FileInfoDto;
import ru.netology.mapper.FileInfoMapper;
import ru.netology.service.CloudService;
import ru.netology.service.FileManager;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CloudController {

    private final CloudService cloudService;
    private final FileManager fileManager;
    private final FileInfoMapper mapper;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void uploadFile(@RequestParam String filename, @RequestBody MultipartFile file) {
        cloudService.saveFile(filename, file);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void deleteFile(@RequestParam String filename) {
        cloudService.deleteFile(filename);
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public FileDto downloadFile(@RequestParam String filename) {
        String hash = cloudService.downloadFile(filename).getHash();
        Resource fileContent = fileManager.downloadFile(hash);
        return FileDto.builder()
                .hash(hash)
                .file(fileContent.toString())
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/file")
    @ResponseStatus(HttpStatus.OK)
    public void editFileName(@RequestParam String filename, @RequestBody Map<String, String> newName) {
        cloudService.editFilename(filename, newName.get("filename"));
    }

    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    @GetMapping("/list")
    @ResponseStatus(HttpStatus.OK)
    public List<FileInfoDto> getFiles(@RequestParam int limit) {
        return cloudService.getFiles(limit).stream().map(mapper::fileToFileInfoDto).toList();
    }
}
