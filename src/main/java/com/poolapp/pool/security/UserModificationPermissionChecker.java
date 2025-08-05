package com.poolapp.pool.security;

import com.poolapp.pool.dto.UpdateUserDTO;
import com.poolapp.pool.model.User;
import com.poolapp.pool.util.SecurityUtil;
import com.poolapp.pool.util.exception.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("userModificationChecker")
public class UserModificationPermissionChecker {

    public boolean canModify(UpdateUserDTO dto) {
        try {
            User currentUser = SecurityUtil.getCurrentUser();
            boolean isAdmin = SecurityUtil.isCurrentUserAdmin();
            boolean isChangingRole = dto.getRoleType() != null;
            boolean isModifyingSelf = dto.getEmail().equals(currentUser.getEmail());

            if (isChangingRole && !isAdmin) {
                log.warn(ErrorMessages.ACCESS_DENIED_NOT_ADMIN_CHANGING_ROLE, currentUser.getId());
                return false;
            }

            if (isChangingRole && isModifyingSelf) {
                log.warn(ErrorMessages.ACCESS_DENIED_ADMIN_MODIFYING_SELF_ROLE, currentUser.getId());
                return false;
            }

            if (!isChangingRole && !isModifyingSelf && !isAdmin) {
                log.warn(ErrorMessages.ACCESS_DENIED_USER_MODIFYING_OTHER, currentUser.getId());
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
