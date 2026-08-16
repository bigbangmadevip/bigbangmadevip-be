package com.thevip.global;

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
    void 상한_이내면_세션을_그대로_두고_쿠키는_직접_내려주지_않는다() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession(false)).thenReturn(session);
        when(session.getAttribute("SESSION_CREATED_AT")).thenReturn(Instant.now().minus(10, ChronoUnit.DAYS));

        filter.doFilter(request, response, chain);

        verify(response, never()).addHeader(anyString(), anyString());
        verify(session, never()).invalidate();
        verify(chain).doFilter(request, response);
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
