package com.thevip.link;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.thevip.global.exception.BusinessException;
import com.thevip.link.entity.ShortLink;
import com.thevip.link.repository.LinkClickRepository;
import com.thevip.link.service.ShortLinkService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LinkRedirectTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ShortLinkService shortLinkService;

    @Autowired
    LinkClickRepository linkClickRepository;

    @Test
    void 단축링크_접속시_브릿지페이지가_나오고_클릭이_기록된다() throws Exception {
        String melonUrl = "melonapp://play?menuid=0&ctype=1&cid=1234567,7654321";
        ShortLink link = shortLinkService.create(melonUrl, "멜론 총공 리스트");
        long clicksBefore = linkClickRepository.count();

        mockMvc.perform(get("/s/" + link.getShortKey()))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("멜론 총공 리스트")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("cid=1234567,7654321")));

        assertThat(linkClickRepository.count()).isEqualTo(clicksBefore + 1);
    }

    @Test
    void 없는_키로_접속하면_404() throws Exception {
        mockMvc.perform(get("/s/nokey1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 허용되지_않은_스킴은_생성이_거부된다() {
        assertThatThrownBy(() -> shortLinkService.create("javascript:alert(1)", "악성"))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void 키는_6자리_영숫자로_생성된다() {
        ShortLink link = shortLinkService.create("https://example.com", null);
        assertThat(link.getShortKey()).matches("[a-zA-Z0-9]{6}");
    }
}
