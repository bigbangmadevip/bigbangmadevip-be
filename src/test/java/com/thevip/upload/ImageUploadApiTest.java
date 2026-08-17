package com.thevip.upload;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest
@AutoConfigureMockMvc
class ImageUploadApiTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void 지원하지_않는_형식이면_400이_반환된다() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", new byte[] {1});

        mockMvc.perform(multipart("/api/v1/admin/images")
                        .file(file)
                        .with(loginAs("MUSIC_ADMIN"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("C001"));
    }

    @Test
    void 일반_유저는_접근할_수_없다() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.png", "image/png", new byte[] {1});

        mockMvc.perform(multipart("/api/v1/admin/images")
                        .file(file)
                        .with(loginAs("USER"))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isForbidden());
    }

    private RequestPostProcessor loginAs(String role) {
        ClientRegistration kakao = ClientRegistration.withRegistrationId("kakao")
                .clientId("test")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/kakao")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("id")
                .build();
        DefaultOAuth2User principal = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_" + role)),
                Map.of("id", 1L),
                "id");
        return SecurityMockMvcRequestPostProcessors.oauth2Login()
                .clientRegistration(kakao)
                .oauth2User(principal);
    }
}
