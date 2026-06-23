package com.banque.abc.tpe.config;

import com.banque.abc.tpe.controller.AuthController;
import com.banque.abc.tpe.security.CustomUserDetailsService;
import com.banque.abc.tpe.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WebSocketAuthChannelInterceptor implements ChannelInterceptor {

    private static final String NOTIFICATION_DESTINATION = "/user/queue/notifications";

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticate(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            if (accessor.getUser() == null) {
                throw new MessageDeliveryException("Connexion WebSocket non authentifiee");
            }
            if (!NOTIFICATION_DESTINATION.equals(accessor.getDestination())) {
                throw new MessageDeliveryException("Abonnement WebSocket non autorise");
            }
        }

        return message;
    }

    private void authenticate(StompHeaderAccessor accessor) {
        String token = getTokenFromCookie(accessor);
        if (!StringUtils.hasText(token)) {
            token = getTokenFromAuthorizationHeader(accessor);
        }
        if (!StringUtils.hasText(token)) {
            throw new MessageDeliveryException("Jeton JWT WebSocket manquant");
        }
        if (!tokenProvider.validateToken(token)) {
            throw new MessageDeliveryException("Jeton JWT WebSocket invalide");
        }

        Long userId = tokenProvider.getUserIdFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserById(userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        accessor.setUser(authentication);
    }

    private String getTokenFromAuthorizationHeader(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }

    private String getTokenFromCookie(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes == null) {
            return null;
        }

        Object cookieHeader = sessionAttributes.get(WebSocketConfig.COOKIE_HEADER_SESSION_ATTRIBUTE);
        if (!(cookieHeader instanceof String cookies) || !StringUtils.hasText(cookies)) {
            return null;
        }

        for (String cookie : cookies.split(";")) {
            String[] parts = cookie.trim().split("=", 2);
            if (parts.length == 2 && AuthController.ACCESS_TOKEN_COOKIE.equals(parts[0])) {
                return parts[1];
            }
        }
        return null;
    }
}
