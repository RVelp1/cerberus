package com.nike.cerberus.event.filter;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import com.nike.cerberus.util.SdbAccessRequest;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditLoggingFilterTest {

  AuditLoggingFilter auditLoggingFilter;
  HttpServletRequest request;
  HttpServletResponse response;
  FilterChain filterChain;

  @Mock private Authentication authentication;
  private SdbAccessRequest sdbAccessRequest;
  private AuditLoggingFilterDetails auditLoggingFilterDetails;
  private BuildProperties buildProperties;
  private ApplicationEventPublisher applicationEventPublisher;

  @Before
  public void setUp() {
    initMocks(this);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    filterChain = mock(FilterChain.class);
    Mockito.when(authentication.getPrincipal()).thenReturn("principal");
    SecurityContextHolder.getContext().setAuthentication(authentication);
    sdbAccessRequest = mock(SdbAccessRequest.class);
    auditLoggingFilterDetails = mock(AuditLoggingFilterDetails.class);
    buildProperties = mock(BuildProperties.class);
    applicationEventPublisher = mock(ApplicationEventPublisher.class);
    auditLoggingFilter =
        new AuditLoggingFilter(
            sdbAccessRequest,
            auditLoggingFilterDetails,
            buildProperties,
            applicationEventPublisher);
  }

  @Test
  public void testShouldNotFilter() {
    Mockito.when(request.getServletPath()).thenReturn("/dashboard/**");
    assertTrue(auditLoggingFilter.shouldNotFilter(request));
  }

  @Test
  public void testDoFilterInternal() {
    try {
      doNothing().when(filterChain).doFilter(request, response);
      when(request.getMethod()).thenReturn("GET");
      when(request.getServletPath()).thenReturn("path");
      mockxFwdAndVersion();
      when(auditLoggingFilterDetails.getSdbNameSlug()).thenReturn("sdbNameSlug");
      doNothing().when(applicationEventPublisher).publishEvent(ApplicationEvent.class);
      auditLoggingFilter.doFilterInternal(request, response, filterChain);
      assertNotNull(SecurityContextHolder.getContext());
    } catch (Exception exception) {
      assertNull(exception);
    }
  }

  private void mockxFwdAndVersion() {
    Mockito.when(response.getStatus()).thenReturn(200);
    Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("102.0.0.1");
    Mockito.when(request.getHeader("X-Cerberus-Client")).thenReturn("1.0");
    Mockito.when(buildProperties.getVersion()).thenReturn("1.0");
  }

  @Test
  public void testDoFilterInternal_pass_sdbslug_sdbAccessRequest() {
    try {
      doNothing().when(filterChain).doFilter(request, response);
      Mockito.when(request.getMethod()).thenReturn("GET");
      Mockito.when(request.getServletPath()).thenReturn("path");
      mockxFwdAndVersion();
      Mockito.when(sdbAccessRequest.getSdbSlug()).thenReturn("sdbNameSlug");
      doNothing().when(applicationEventPublisher).publishEvent(ApplicationEvent.class);
      auditLoggingFilter.doFilterInternal(request, response, filterChain);
      assertNotNull(SecurityContextHolder.getContext());
    } catch (Exception exception) {
      assertNull(exception);
    }
  }

  @Test
  public void testDoFilterInternal_actionpassed_auditloggingfilterdetails() {
    try {
      doNothing().when(filterChain).doFilter(request, response);
      Mockito.when(auditLoggingFilterDetails.getAction())
          .thenReturn("principal" + " " + "GET" + "path");
      mockxFwdAndVersion();
      Mockito.when(auditLoggingFilterDetails.getSdbNameSlug()).thenReturn("sdbNameSlug");
      doNothing().when(applicationEventPublisher).publishEvent(ApplicationEvent.class);
      auditLoggingFilter.doFilterInternal(request, response, filterChain);
      assertNotNull(SecurityContextHolder.getContext());
    } catch (Exception exception) {
      assertNull(exception);
    }
  }

  @After
  public void clearContext() {
    SecurityContextHolder.clearContext();
  }
}
