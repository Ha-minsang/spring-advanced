package org.example.expert.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminApiLoggingAspectTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @InjectMocks
    private AdminApiLoggingAspect adminApiLoggingAspect;

    @AfterEach
    void reset() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("관리자 API 로깅 AOP 정상 동작")
    void logAdminApi_ShouldLogAdminApi() throws Throwable {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        requestWrapper.setAttribute("userId", 1L);
        ServletRequestAttributes attributes = new ServletRequestAttributes(requestWrapper);
        RequestContextHolder.setRequestAttributes(attributes);

        Object proceedResult = new Object();

        given(joinPoint.proceed()).willReturn(proceedResult);
        given(objectMapper.writeValueAsString(proceedResult)).willReturn("resultToJson");

        // when
        Object result = adminApiLoggingAspect.logAdminApi(joinPoint);

        // then
        assertSame(proceedResult, result);
    }
}
