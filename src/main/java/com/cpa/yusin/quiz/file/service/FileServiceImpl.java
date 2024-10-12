package com.cpa.yusin.quiz.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.cpa.yusin.quiz.file.controller.dto.response.FileResponse;
import com.cpa.yusin.quiz.file.controller.port.FileService;
import com.cpa.yusin.quiz.file.domain.File;
import com.cpa.yusin.quiz.file.mapper.FileMapper;
import com.cpa.yusin.quiz.file.service.port.FileRepository;
import com.cpa.yusin.quiz.global.exception.ExceptionMessage;
import com.cpa.yusin.quiz.global.exception.FileException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService
{
    private final AmazonS3 amazonS3;
    private final FileMapper fileMapper;
    private final FileRepository fileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.prefix}")
    private String prefix;

    @Transactional
    @Override
    public FileResponse save(MultipartFile file)
    {
        String uniqueFilename = getUniqueFilename();

        String url = updateFileToS3(uniqueFilename, file);
        log.info("url = {}", url);

        File fileDomain = fileMapper.toFileDomain(url, uniqueFilename, file);

        fileDomain = fileRepository.save(fileDomain);

        return fileMapper.domainToFileResponse(fileDomain);
    }

    private String updateFileToS3(String uniqueFilename, MultipartFile file)
    {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            String objectFilename = getObjectFileName(uniqueFilename + file.getContentType());

            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucket, objectFilename, file.getInputStream(), metadata);

            return String.valueOf(amazonS3.getUrl(bucket, objectFilename));

        } catch (IOException e) {
            throw new FileException(ExceptionMessage.INVALID_DATA);
        }
    }

    private String extractType(String filename)
    {
        if (!StringUtils.hasLength(filename)) {
            throw new FileException(ExceptionMessage.INVALID_DATA);
        }

        int location = filename.lastIndexOf('.');
        String fileType = filename.substring(location + 1);
        log.info("file type = {}", fileType);
        return fileType;
    }

    private String getUniqueFilename()
    {
        return UUID.randomUUID().toString();
    }

    private String getObjectFileName(String uniqueFilename)
    {
        return prefix + "/" + uniqueFilename;
    }
}
