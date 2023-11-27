package com.epam.esm.model;

import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser {
    private Long id;
    private String email;
    private Provider provider;
    private Role role;
}