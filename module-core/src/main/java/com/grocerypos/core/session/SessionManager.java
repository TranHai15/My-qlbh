package com.grocerypos.core.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

/**
 * Quản lý phiên làm việc hiện tại của ứng dụng (Singleton).
 * Tuân thủ thiết kế trong SKILL-core.md.
 */
public class SessionManager {
    private static final Logger log = LoggerFactory.getLogger(SessionManager.class);
    private static final SessionManager INSTANCE = new SessionManager();
    
    private UserSession currentSession;

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public void login(UserSession session) {
        this.currentSession = session;
        log.info("Người dùng '{}' ({}) đã đăng nhập.", session.getUsername(), session.getRole());
    }

    public void logout() {
        if (currentSession != null) {
            log.info("Người dùng '{}' đã đăng xuất.", currentSession.getUsername());
        }
        this.currentSession = null;
    }

    public Optional<UserSession> getCurrentSession() {
        return Optional.ofNullable(currentSession);
    }

    public Optional<String> getCurrentUsername() {
        return getCurrentSession().map(UserSession::getUsername);
    }

    public boolean isLoggedIn() {
        return currentSession != null;
    }

    public boolean isAdmin() {
        return getCurrentSession()
                .map(s -> s.getRole() == UserRole.ADMIN)
                .orElse(false);
    }
}
