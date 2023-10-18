package ru.netology.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.entity.File;
import ru.netology.exception.FileProcessingException;
import ru.netology.exception.IncorrectInputDataException;
import ru.netology.repository.CloudRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CloudServiceImpl implements CloudService {

    private final CloudRepository cloudRepository;
    private final FileManager fileManager;


    @Override
    public void saveFile(String filename, MultipartFile file) {
        try {
            log.info("Checking the existence of file {}", filename);
            if (cloudRepository.findByFilename(filename).isPresent()) {
                throw new IncorrectInputDataException(String.format("File with name %s already exists", filename));
            }

            if (file == null) {
                throw new IncorrectInputDataException("File is not attached to request");
            }

            File uploadedFile = createFileInfo(filename, file);

            log.info("Saving file info of {} to database..", filename);
            cloudRepository.save(uploadedFile);
            log.info("File info of {} saved to database", filename);

            log.info("Uploading file {} to storage..", filename);
            fileManager.uploadFile(file.getBytes(), uploadedFile.getHash(), filename);
            log.info("File {} uploaded to storage", filename);

        } catch (IOException ex) {
            throw new FileProcessingException(ex.getMessage());
        }
    }

    @Override
    public void deleteFile(String filename) {
        File fileToDelete = getExistingFile(filename);

        try {
            log.info("Deleting file info of {} from database", filename);
            cloudRepository.delete(fileToDelete);
            log.info("File info of {} deleted from database", filename);

            log.info("Deleting file {} from storage..", filename);
            fileManager.deleteFile(fileToDelete.getHash());
            log.info("file {} deleted from storage", filename);

        } catch (Exception ex) {
            throw new FileProcessingException(ex.getMessage());
        }
    }

    @Override
    public File downloadFile(String filename) {
        File file = getExistingFile(filename);

        try {
            log.info("Downloading file {} from storage..", filename);
            log.info("File {} downloaded from storage", filename);

            return file;

        } catch (Exception ex) {
            throw new FileProcessingException(ex.getMessage());
        }
    }

    @Override
    public void editFilename(String filename, String newName) {
        File file = getExistingFile(filename);
        file.setFilename(newName);
        cloudRepository.save(file);
    }

    @Override
    public List<File> getFiles(int limit) {
        log.info("Getting the file list..");
        return cloudRepository.findAll(Pageable.ofSize(limit)).toList();
    }

    private File createFileInfo(String filename, MultipartFile file) throws IOException {
        LocalDateTime createdTime = LocalDateTime.now();

        String hash = UUID.nameUUIDFromBytes(
                ArrayUtils.addAll(file.getBytes(), createdTime.toString().getBytes())).toString();

        return File.builder()
                .hash(hash)
                .filename(filename)
                .size(file.getSize())
                .createdTime(createdTime)
                .build();
    }

    private File getExistingFile(String filename) {
        return cloudRepository.findByFilename(filename).orElseThrow(
                () -> new IncorrectInputDataException(String.format("File with name %s does not exist", filename)));
    }
}
