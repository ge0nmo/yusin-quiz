package com.cpa.yusin.quiz.file.service;

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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetUrlRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final S3Client s3Client;       // V2 업로드 클라이언트
    private final S3Presigner s3Presigner; // V2 프리사인 클라이언트
    private final FileMapper fileMapper;
    private final FileRepository fileRepository;
    private final FilenameGenerator filenameGenerator;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.prefix}")
    private String prefix;

    @Transactional
    @Override
    public String save(MultipartFile file)
    {
        String uniqueFilename = filenameGenerator.createStoreFileName(file.getOriginalFilename());
        String objectKey = prefix + "/" + uniqueFilename;

        try {
            // [V2] MultipartFile 업로드
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // URL 추출
            String url = s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(objectKey).build()).toString();

            File fileDomain = fileMapper.toFileDomain(url, uniqueFilename, file);
            fileRepository.save(fileDomain);

            return url;

        } catch (IOException e) {
            throw new FileException(ExceptionMessage.INVALID_DATA);
        }
    }

    // [V2] Base64 바이트 배열 업로드
    @Override
    public String saveByteArray(byte[] fileData, String filename, String contentType) {
        String objectKey = prefix + "/" + filename;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(fileData));

            return s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucket).key(objectKey).build()).toString();

        } catch (Exception e) {
            log.error("S3 Byte Upload Fail", e);
            throw new FileException(ExceptionMessage.INVALID_DATA);
        }
    }

    // [V2] Presigned URL 생성
    @Override
    public String generatePresignedUrl(String objectKey) {
        try {
            // 요청 객체 생성 (유효기간 60분)
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(60))
                    .getObjectRequest(req -> req.bucket(bucket).key(objectKey))
                    .build();

            // URL 발급
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("Presigned URL 발급 실패: key={}", objectKey, e);
            return "";
        }
    }
}