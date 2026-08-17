package com.thevip.upload.service;

import com.thevip.global.exception.BusinessException;
import com.thevip.global.exception.ErrorCode;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

    // CloudFront(OAC)로만 읽는 비공개 버킷을 전제로 하므로 ACL은 따로 지정하지 않는다.
    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp",
            "image/gif", "gif");

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.cloudfront.domain}")
    private String cdnDomain;

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "파일이 비어 있습니다.");
        }
        String extension = ALLOWED_CONTENT_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 이미지 형식입니다 (jpeg/png/webp/gif만 가능).");
        }

        String key = "uploads/%s/%s.%s".formatted(
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM")), UUID.randomUUID(), extension);
        try {
            s3Client.putObject(
                    PutObjectRequest.builder().bucket(bucket).key(key).contentType(file.getContentType()).build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "이미지 업로드에 실패했습니다.");
        }

        return "https://" + cdnDomain + "/" + key;
    }
}
