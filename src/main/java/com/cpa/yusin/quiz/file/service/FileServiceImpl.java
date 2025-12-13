package com.cpa.yusin.quiz.file.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService
{
    private final AmazonS3 amazonS3;
    private final FileMapper fileMapper;
    private final FileRepository fileRepository;
    private final FilenameGenerator filenameGenerator;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.prefix}")
    private String prefix;

    @Transactional
    @Override
    public FileResponse save(MultipartFile file)
    {
        String uniqueFilename = filenameGenerator.createStoreFileName(file.getOriginalFilename());
        String url = updateFileToS3(uniqueFilename, file);
        File fileDomain = fileMapper.toFileDomain(url, uniqueFilename, file);
        fileDomain = fileRepository.save(fileDomain);
        return fileMapper.domainToFileResponse(fileDomain);
    }

    // [추가] Base64 이미지 -> S3 업로드
    @Override
    public String saveByteArray(byte[] fileData, String filename, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(fileData.length);

            // 파일명에 prefix 포함
            String objectKey = prefix + "/" + filename;

            // InputStream으로 업로드
            amazonS3.putObject(bucket, objectKey, new ByteArrayInputStream(fileData), metadata);

            return amazonS3.getUrl(bucket, objectKey).toString();
        } catch (Exception e) {
            log.error("S3 Byte Upload Fail", e);
            throw new FileException(ExceptionMessage.INVALID_DATA);
        }
    }

    @Override
    public String generatePresignedUrl(String objectKey) {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 60; // 1시간
        expiration.setTime(expTimeMillis);

        try {
            GeneratePresignedUrlRequest generatePresignedUrlRequest =
                    new GeneratePresignedUrlRequest(bucket, objectKey)
                            .withMethod(HttpMethod.GET)
                            .withExpiration(expiration);

            return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        } catch (Exception e) {
            log.error("Presigned URL 발급 실패: key={}", objectKey, e);
            return "";
        }
    }

    private String updateFileToS3(String uniqueFilename, MultipartFile file)
    {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            String objectFilename = getObjectFileName(uniqueFilename);

            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            amazonS3.putObject(bucket, objectFilename, file.getInputStream(), metadata);

            return String.valueOf(amazonS3.getUrl(bucket, objectFilename));

        } catch (IOException e) {
            throw new FileException(ExceptionMessage.INVALID_DATA);
        }
    }

    private String getObjectFileName(String uniqueFilename)
    {
        return prefix + "/" + uniqueFilename;
    }
}