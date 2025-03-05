package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.UserDTO;
import com.agimuseum.magi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id)")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id)")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUserByUsername(username));
    }
}