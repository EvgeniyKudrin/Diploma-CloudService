package ru.netology.mapper;

import org.mapstruct.Mapper;
import ru.netology.dto.FileInfoDto;
import ru.netology.entity.File;

@Mapper(componentModel = "spring")
public interface FileInfoMapper {

    FileInfoDto fileToFileInfoDto(File file);
    File fileInfoDTOToFile(FileInfoDto fileInfoDTO);
}
