package io.stonk.auth.dto;

public class RegisterRequest {

    private String username;
    private String email;
    private String password;
    private String role; // "USER" or "ADMIN"

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }
}