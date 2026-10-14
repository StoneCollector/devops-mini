package com.cbms.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            String uri = request.getRequestURI();
            String query = request.getQueryString();
            String fullTarget = (query != null && !query.isEmpty()) ? uri + "?" + query : uri;

            boolean isBookingAction = uri.startsWith("/events/book") || uri.startsWith("/book") || uri.startsWith("/bookings");
            String redirectParam = URLEncoder.encode(fullTarget, StandardCharsets.UTF_8);

            String redirectUrl = "/login?" + (isBookingAction ? "bookingPrompt=true&" : "") + "redirect=" + redirectParam;
            response.sendRedirect(redirectUrl);
            return false;
        }
        return true;
    }
}
