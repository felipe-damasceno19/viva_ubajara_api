package io.github.parqueubajara.api.controller;

import io.github.parqueubajara.api.dto.request.ApprovalUpdateDTO;
import io.github.parqueubajara.api.dto.request.ReviewRequestDTO;
import io.github.parqueubajara.api.dto.response.ReviewResponseDTO;
import io.github.parqueubajara.api.model.SystemUser;
import io.github.parqueubajara.api.model.enums.ApprovalStatus;
import io.github.parqueubajara.api.service.ReviewService;
import io.github.parqueubajara.api.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ReviewSecurityTest.TestConfiguration.class)
class ReviewSecurityTest {

    @Autowired private ReviewController controller;
    @Autowired private ReviewService reviewService;
    @Autowired private UserService userService;

    @BeforeEach
    void resetMocks() {
        reset(reviewService, userService);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void publicReadsNeedNoAuthentication() {
        UUID spotId = UUID.randomUUID();

        assertEquals(200, controller.listApproved(spotId, PageRequest.of(0, 5)).getStatusCode().value());
        assertEquals(200, controller.summary(spotId).getStatusCode().value());
    }

    @Test
    void saveAndMineRefuseAnonymous() {
        UUID spotId = UUID.randomUUID();

        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> controller.save(spotId, new ReviewRequestDTO(5, null), null));
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> controller.mine(spotId, null));
        verifyNoInteractions(reviewService);
    }

    @Test
    void saveUsesIdentityFromSecurityContext() {
        Authentication auth = login("user@example.com", "ROLE_USER");
        SystemUser user = new SystemUser();
        UUID spotId = UUID.randomUUID();
        ReviewRequestDTO dto = new ReviewRequestDTO(4, null);
        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(reviewService.upsert(user, spotId, dto)).thenReturn(response());

        var saved = controller.save(spotId, dto, auth);

        assertEquals(200, saved.getStatusCode().value());
        verify(reviewService).upsert(user, spotId, dto);
    }

    @Test
    void mineReturns204WhenThereIsNoReviewAnd200WhenThereIs() {
        Authentication auth = login("user@example.com", "ROLE_USER");
        SystemUser user = new SystemUser();
        UUID spotId = UUID.randomUUID();
        when(userService.findByEmail("user@example.com")).thenReturn(user);
        when(reviewService.findMine(user, spotId)).thenReturn(Optional.empty());

        assertEquals(204, controller.mine(spotId, auth).getStatusCode().value());

        when(reviewService.findMine(user, spotId)).thenReturn(Optional.of(response()));
        assertEquals(200, controller.mine(spotId, auth).getStatusCode().value());
    }

    @Test
    void moderationRoutesRefuseCommonUser() {
        login("user@example.com", "ROLE_USER");
        UUID id = UUID.randomUUID();

        assertThrows(AccessDeniedException.class, () -> controller.moderation(null, PageRequest.of(0, 10)));
        assertThrows(AccessDeniedException.class,
                () -> controller.updateApprovalStatus(id, new ApprovalUpdateDTO(ApprovalStatus.APPROVED)));
        assertThrows(AccessDeniedException.class, () -> controller.delete(id));
        verifyNoInteractions(reviewService);
    }

    @Test
    void moderationRoutesRefuseCompanyUser() {
        login("empresa@example.com", "ROLE_EMPRESA");

        assertThrows(AccessDeniedException.class, () -> controller.delete(UUID.randomUUID()));
        verifyNoInteractions(reviewService);
    }

    @Test
    void moderationRoutesAllowAdmin() {
        login("admin@example.com", "ROLE_ADMIN");
        UUID id = UUID.randomUUID();

        controller.moderation(ApprovalStatus.PENDING, PageRequest.of(0, 10));
        assertEquals(204, controller.updateApprovalStatus(id, new ApprovalUpdateDTO(ApprovalStatus.REJECTED))
                .getStatusCode().value());
        assertEquals(204, controller.delete(id).getStatusCode().value());

        verify(reviewService).listForModeration(ApprovalStatus.PENDING, PageRequest.of(0, 10));
        verify(reviewService).updateStatus(id, ApprovalStatus.REJECTED);
        verify(reviewService).delete(id);
    }

    private ReviewResponseDTO response() {
        return new ReviewResponseDTO(UUID.randomUUID(), 4, null, "Pessoa T.", null, ApprovalStatus.PENDING, null);
    }

    private Authentication login(String email, String role) {
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                email, null, List.of(new SimpleGrantedAuthority(role)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return authentication;
    }

    @Configuration
    @EnableMethodSecurity
    static class TestConfiguration {

        @Bean
        ReviewService reviewService() {
            return mock(ReviewService.class);
        }

        @Bean
        UserService userService() {
            return mock(UserService.class);
        }

        @Bean
        ReviewController reviewController(ReviewService reviewService, UserService userService) {
            return new ReviewController(reviewService, userService);
        }
    }
}
