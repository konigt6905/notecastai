package com.notecastai.common.util;

import com.notecastai.common.exception.BusinessException;

/**
 * Verifies that the current user owns a given resource.
 * Uses {@link UserContext} to get the current user's database ID.
 */
public final class OwnershipVerifier {

    private OwnershipVerifier() {
    }

    /**
     * Throws FORBIDDEN if the given owner ID does not match the current user.
     *
     * @param ownerId the database ID of the resource owner
     *                (e.g. entity.getUser().getId() or entity.getCreatedBy())
     */
    public static void verify(Long ownerId) {
        Long currentUserId = UserContext.require();
        if (!currentUserId.equals(ownerId)) {
            throw BusinessException.of(BusinessException.BusinessCode.FORBIDDEN);
        }
    }
}
