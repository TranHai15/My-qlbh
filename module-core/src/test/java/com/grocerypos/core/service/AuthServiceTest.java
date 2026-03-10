package com.grocerypos.core.service;

import com.grocerypos.core.exception.ValidationException;
import com.grocerypos.core.repository.AuthRepository;
import com.grocerypos.core.service.impl.AuthServiceImpl;
import com.grocerypos.core.session.SessionManager;
import com.grocerypos.core.session.UserRole;
import com.grocerypos.core.session.UserSession;
import com.grocerypos.core.util.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private AuthService authService;

    @Mock
    private AuthRepository authRepo;

    private final String correctPassword = "adminPassword123";
    private final String hashedPassword = PasswordUtils.hashPassword(correctPassword);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(authRepo);
        SessionManager.getInstance().logout(); // Đảm bảo trạng thái sạch
    }

    @Test
    void login_Success() {
        // Given
        when(authRepo.getAdminPassword()).thenReturn(Optional.of(hashedPassword));

        // When
        UserSession session = authService.login("admin", correctPassword);

        // Then
        assertNotNull(session);
        assertEquals("admin", session.getUsername());
        assertEquals(UserRole.ADMIN, session.getRole());
        assertTrue(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void login_WrongUsername_ShouldThrowException() {
        // When & Then
        assertThrows(ValidationException.class, () -> {
            authService.login("wrongUser", correctPassword);
        });
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void login_WrongPassword_ShouldThrowException() {
        // Given
        when(authRepo.getAdminPassword()).thenReturn(Optional.of(hashedPassword));

        // When & Then
        assertThrows(ValidationException.class, () -> {
            authService.login("admin", "wrongPassword");
        });
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    @Test
    void logout_Success() {
        // Given
        when(authRepo.getAdminPassword()).thenReturn(Optional.of(hashedPassword));
        authService.login("admin", correctPassword);
        assertTrue(SessionManager.getInstance().isLoggedIn());

        // When
        authService.logout();

        // Then
        assertFalse(SessionManager.getInstance().isLoggedIn());
        assertTrue(SessionManager.getInstance().getCurrentSession().isEmpty());
    }
}
