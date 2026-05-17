package com.freightos.admin.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties("admin")
public class AdminUserProperties {

    private List<UserEntry> users = new ArrayList<>();

    public List<UserEntry> getUsers() { return users; }

    public void setUsers(List<UserEntry> users) { this.users = users; }

    public static class UserEntry {
        private String username;
        private String password;
        private List<String> roles = new ArrayList<>();

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
    }
}
