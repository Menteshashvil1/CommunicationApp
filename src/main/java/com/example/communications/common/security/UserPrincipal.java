package com.example.communications.common.security;

import com.example.communications.user.model.User;

public record UserPrincipal(
        Long id,
        String email,
        String displayName
)
{
        public static UserPrincipal from(User user){
            return new UserPrincipal
                    (
                            user.getId(),
                            user.getEmail(),
                            user.getDisplayName()
                    );
        }
}
