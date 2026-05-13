package io.github.parqueubajara.api.security;


import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityService {

    private final UserService userService;

    public SystemUser getUserLogged() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        Object principal = auth.getPrincipal();

        if (auth instanceof CustomAuthentication customAuth) {
            return customAuth.getUser();
        }

        if (principal instanceof SystemUser user) {
            return user;
        }

        String email = null;
        if (principal instanceof UserDetails details) {
            email = details.getUsername();
        } else if (principal instanceof String strEmail) {
            email = strEmail;
        }

        if (email != null) {
            return userService.findByEmail(email);
        }

        return null;
    }
}