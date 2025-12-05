package org.example.expert.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AdminApiLoggingAspect {

    private final ObjectMapper objectMapper;

    public AdminApiLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController.*(..))")
    private void commentAdminController() {}

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController.*(..))")
    private void userAdminController() {}

    @Around("commentAdminController() || userAdminController()")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {

        HttpServletRequest request = currentRequest();           // 현재 요청 받아오기
        ContentCachingRequestWrapper requestWrapper = (ContentCachingRequestWrapper)request;

        Long userId = (Long) request.getAttribute("userId");  // 요청한 사용자 Id
        String requestUrl = request.getRequestURI();            // 요청 url
        LocalDateTime requestTime = LocalDateTime.now();        // 요청 시각
        String requestBody = requestWrapper.getContentAsString();
        if (requestBody.isEmpty()) {
            requestBody = null;
        }

        log.info("[ADMIN-REQUEST] userId={}, url={}, time={}, body={}",
                userId, requestUrl, requestTime, requestBody);

        Object result = joinPoint.proceed(); // 메서드 실행

        String responseBody = objectMapper.writeValueAsString(result);
        log.info("[ADMIN-RESPONSE] body={}", responseBody);

        return result;
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
