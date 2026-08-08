package com.thevip.global;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thevip.global.security.SessionRenewalFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SessionRenewalFilterTest {

    private final SessionRenewalFilter filter = new SessionRenewalFilter();

    @Test
    void 세션이_없으면_아무것도_하지_않는다() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        when(request.getSession(false)).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(response, never()).addHeader(anyString(), anyString());
        verify(chain).doFilter(request, response);
    }

    @Test
    void 처음_보는_세션이면_생성시각만_기록하고_쿠키는_아직_안_내려준다() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("SESSION_CREATED_AT")).thenReturn(null);

        filter.doFilter(request, response, chain);

        verify(session).setAttribute(org.mockito.ArgumentMatchers.eq("SESSION_CREATED_AT"),
                org.mockito.ArgumentMatchers.any(Instant.class));
        verify(response, never()).addHeader(anyString(), anyString());
    }

    @Test
    void 상한_이내면_60일짜리_쿠키로_갱신한다() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("SESSION_CREATED_AT")).thenReturn(Instant.now().minus(10, ChronoUnit.DAYS));
        when(session.getId()).thenReturn("test-session-id");

        filter.doFilter(request, response, chain);

        ArgumentCaptor<String> headerValue = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(org.mockito.ArgumentMatchers.eq("Set-Cookie"), headerValue.capture());
        assertThat(headerValue.getValue())
                .contains("JSESSIONID=test-session-id")
                .contains("Max-Age=" + (60L * 24 * 60 * 60))
                .contains("SameSite=None");
        verify(session, never()).invalidate();
    }

    @Test
    void 절대_상한_90일을_넘으면_활동이_있어도_세션을_무효화한다() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("SESSION_CREATED_AT")).thenReturn(Instant.now().minus(91, ChronoUnit.DAYS));

        filter.doFilter(request, response, chain);

        verify(session).invalidate();
        verify(response, never()).addHeader(anyString(), anyString());
        verify(chain).doFilter(request, response);
    }
}
