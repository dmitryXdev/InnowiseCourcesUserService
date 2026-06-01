package com.innowise.userservice.httpfilter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPrincipal {
    private Long id;
    private String role;
}
