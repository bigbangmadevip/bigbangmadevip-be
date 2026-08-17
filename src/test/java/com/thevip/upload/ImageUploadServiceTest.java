package com.thevip.upload;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.thevip.global.exception.BusinessException;
import com.thevip.upload.service.ImageUploadService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

class ImageUploadServiceTest {

    @Test
    void 이미지를_업로드하면_S3에_저장하고_CDN_URL을_반환한다() {
        S3Client s3Client = mock(S3Client.class);
        ImageUploadService service = newService(s3Client);

        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[] {1, 2, 3});
        String url = service.upload(file);

        assertThat(url).startsWith("https://cdn.example.com/uploads/");
        assertThat(url).endsWith(".png");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(software.amazon.awssdk.core.sync.RequestBody.class));
    }

    @Test
    void 지원하지_않는_형식이면_예외가_발생한다() {
        ImageUploadService service = newService(mock(S3Client.class));
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[] {1});

        assertThatThrownBy(() -> service.upload(file)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 빈_파일이면_예외가_발생한다() {
        ImageUploadService service = newService(mock(S3Client.class));
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> service.upload(file)).isInstanceOf(BusinessException.class);
    }

    private ImageUploadService newService(S3Client s3Client) {
        ImageUploadService service = new ImageUploadService(s3Client);
        ReflectionTestUtils.setField(service, "bucket", "test-bucket");
        ReflectionTestUtils.setField(service, "cdnDomain", "cdn.example.com");
        return service;
    }
}
