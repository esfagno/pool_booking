package com.poolapp.pool.util;

import com.poolapp.pool.model.User;
import com.poolapp.pool.model.enums.RoleType;
import com.poolapp.pool.security.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDetailsImpl userDetails) {
            return userDetails.getUser();
        }
        throw new IllegalStateException(ErrorMessages.USER_NOT_FOUND);
    }

    public boolean isCurrentUserAdmin() {
        return getCurrentUser().getRole().getName() == RoleType.ADMIN;
    }
}
