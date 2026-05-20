package io.github.parqueubajara.api.security;

import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SocialLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AuthService authService;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;

    @Value("${app.frontend.redirect-url}")
    private String frontendRedirectUrl;

    public SocialLoginSuccessHandler(
            @Lazy AuthService authService,
            CustomUserDetailsService customUserDetailsService,
            JwtService jwtService) {
        this.authService = authService;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtService = jwtService;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request
            , HttpServletResponse response
            , Authentication authentication) throws ServletException, IOException {
        OAuth2AuthenticationToken auth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = auth2AuthenticationToken.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        SystemUser user = authService.processSocialLogin(email, firstName, lastName);


        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        authentication = new CustomAuthentication(user);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String targetUrl = frontendRedirectUrl + "?token=" + token;

        response.sendRedirect(targetUrl);
    }
}
