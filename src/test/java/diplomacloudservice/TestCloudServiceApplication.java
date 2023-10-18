package diplomacloudservice;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import ru.netology.entity.File;
import ru.netology.exception.IncorrectInputDataException;
import ru.netology.repository.CloudRepository;
import ru.netology.service.CloudServiceImpl;
import ru.netology.service.FileManager;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class TestCloudServiceApplication {

    private String mockedFilename;
    private String mockedHash;
    private MockMultipartFile mockedFile;
    private File mockedFileFromDb;
    private Resource mockedResource;
    @Mock
    private CloudRepository cloudRepository;
    @Mock
    private FileManager fileManager;
    @InjectMocks
    private CloudServiceImpl cloudService;

    @BeforeEach
    public void setUp() {
        this.mockedFilename = "Test";
        this.mockedHash = "12345";
        this.mockedFile = new MockMultipartFile("Test", new byte[]{1, 2, 3});
        this.mockedFileFromDb = new File("12345", "Test", 100L, LocalDateTime.now());
        this.mockedResource = new PathResource("/test.txt");
    }

    @Test
    public void testSaveFileWhenExistedFilenameThenThrowEx() {
        when(cloudRepository.findByFilename(mockedFilename)).thenReturn(Optional.of(mockedFileFromDb));

        Exception ex = Assertions.assertThrows(IncorrectInputDataException.class,
                () -> cloudService.saveFile(mockedFilename, mockedFile));

        Truth.assertThat(ex).hasMessageThat().contains("already exists");
    }

    @Test
    public void testSaveFileWhenFileIsNotAttachedThenThrowEx() {
        when(cloudRepository.findByFilename(mockedFilename)).thenReturn(Optional.empty());

        Exception ex = Assertions.assertThrows(IncorrectInputDataException.class,
                () -> cloudService.saveFile(mockedFilename, null));

        Truth.assertThat(ex).hasMessageThat().contains("not attached");
    }

    @Test
    public void testSaveFileWhenCorrectDataThenSaveFile() throws IOException, SQLIntegrityConstraintViolationException {
        when(cloudRepository.findByFilename(mockedFilename)).thenReturn(Optional.empty());

        cloudService.saveFile(mockedFilename, mockedFile);

        verify(fileManager, times(1))
                .uploadFile(eq(mockedFile.getBytes()), anyString(), eq(mockedFilename));
        verify(cloudRepository, times(1)).save(any(File.class));
    }

    @Test
    public void testDeleteFileWhenNotExistingFileThenThrowEx() {
        when(cloudRepository.findByFilename(mockedFilename)).thenReturn(Optional.empty());

        Exception ex = Assertions.assertThrows(IncorrectInputDataException.class,
                () -> cloudService.deleteFile(mockedFilename));

        Truth.assertThat(ex).hasMessageThat().contains("not exist");
    }

    @Test
    public void testDeleteFileWhenCorrectDataThenDeleteFile() throws IOException,
            SQLIntegrityConstraintViolationException {
        when(cloudRepository.findByFilename(mockedFilename)).thenReturn(Optional.of(mockedFileFromDb));

        cloudService.deleteFile(mockedFilename);

        verify(fileManager, times(1)).deleteFile(mockedHash);
        verify(cloudRepository, times(1)).delete(mockedFileFromDb);
    }

    @Test
    public void testDownloadFileWhenNotExistingFileThenThrowEx() {
        when(cloudRepository.findByFilename(mockedFilename)).thenReturn(Optional.empty());

        Exception ex = Assertions.assertThrows(IncorrectInputDataException.class,
                () -> cloudService.downloadFile(mockedFilename));

        Truth.assertThat(ex).hasMessageThat().contains("not exist");
    }

    @Test
    public void testDownloadFileWhenCorrectDataThenReturnFile() throws IOException,
            SQLIntegrityConstraintViolationException {
        when(cloudRepository.findByFilename(mockedFilename)).thenReturn(Optional.of(mockedFileFromDb));

        File actual = cloudService.downloadFile(mockedFilename);

        verify(fileManager, times(1)).downloadFile(mockedHash);

        File expected = mockedFileFromDb;
        Assertions.assertEquals(expected, actual);
    }

    @Test
    public void testEditFilenameWhenCorrectDataThenSaveWithNewName() throws SQLIntegrityConstraintViolationException {
        String newName = "new-name";
        when(cloudRepository.findByFilename(mockedFilename)).thenReturn(Optional.of(mockedFileFromDb));

        cloudService.editFilename(mockedFilename, newName);

        verify(cloudRepository, times(1)).save(mockedFileFromDb);
        Assertions.assertEquals(mockedFileFromDb.getFilename(), newName);
    }
}
