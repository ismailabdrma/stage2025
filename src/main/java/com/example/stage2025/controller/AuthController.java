package com.example.stage2025.controller;

import com.example.stage2025.entity.Admin;
import com.example.stage2025.entity.Client;
import com.example.stage2025.entity.User;
import com.example.stage2025.enums.Role;
import com.example.stage2025.repository.UserRepository;
import com.example.stage2025.security.JwtUtils;
import com.example.stage2025.service.OtpService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authManager;
    private final OtpService otpService;

    /** --- 1. Registration --- */
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        if (userRepo.existsByUsernameOrEmail(req.username(), req.email())) {
            return ResponseEntity.badRequest().body("Username or email already exists");
        }

        User user = (req.role() == Role.ADMIN) ? new Admin() : new Client();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(encoder.encode(req.password()));
        user.setRole(req.role());
        user.setActive(false); // require email verification

        userRepo.save(user);

        // Send OTP for email verification (type = "REGISTER")
        otpService.sendOtp(user.getEmail(), user.getId(), "REGISTER");

        return ResponseEntity.ok("User registered. Please verify your email with the OTP sent.");
    }

    /** --- 2. Verify Email OTP (activate account) --- */
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody OtpVerifyRequest req) {
        Optional<User> userOpt = userRepo.findByEmail(req.email());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("Invalid email");
        User user = userOpt.get();
        if (otpService.verifyOtp(user.getEmail(), "REGISTER", req.otp())) {
            user.setActive(true);
            userRepo.save(user);
            return ResponseEntity.ok("Email verified. You may now log in.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }
    }

    /** --- 3. Login + 2FA (OTP sent after password is correct) --- */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(req.identifier(), req.password());
        authManager.authenticate(authToken);

        User user = userRepo.findByUsernameOrEmail(req.identifier(), req.identifier())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isActive())
            return ResponseEntity.badRequest().body("Account not activated.");

        // Send OTP for login (type = "LOGIN")
        otpService.sendOtp(user.getEmail(), user.getId(), "LOGIN");
        return ResponseEntity.ok("OTP sent to your email. Please verify to complete login.");
    }

    /** --- 4. Complete Login with OTP (return JWT) --- */
    @PostMapping("/login/verify-otp")
    public ResponseEntity<?> loginVerifyOtp(@RequestBody OtpVerifyRequest req) {
        Optional<User> userOpt = userRepo.findByEmail(req.email());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("Invalid email");
        User user = userOpt.get();

        if (otpService.verifyOtp(user.getEmail(), "LOGIN", req.otp())) {
            String token = jwtUtils.generateToken(user);
            return ResponseEntity.ok(new JwtResponse(token, user.getUsername(), user.getEmail(), user.getRole().name()));
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }
    }

    /** --- 5. Resend OTP (for login or registration verification) --- */
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest req) {
        Optional<User> userOpt = userRepo.findByEmail(req.email());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");
        User user = userOpt.get();
        // type = "REGISTER" or "LOGIN"
        otpService.sendOtp(user.getEmail(), user.getId(), req.type());
        return ResponseEntity.ok("OTP resent to email.");
    }

    /** --- 6. Forgot Password: request reset (send OTP) --- */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody EmailRequest req) {
        Optional<User> userOpt = userRepo.findByEmail(req.email());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");
        User user = userOpt.get();
        // type = "FORGOT"
        otpService.sendOtp(user.getEmail(), user.getId(), "FORGOT");
        return ResponseEntity.ok("OTP sent for password reset.");
    }

    /** --- 7. Reset Password with OTP --- */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        Optional<User> userOpt = userRepo.findByEmail(req.email());
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");
        User user = userOpt.get();
        if (otpService.verifyOtp(user.getEmail(), "FORGOT", req.otp())) {
            user.setPassword(encoder.encode(req.newPassword()));
            userRepo.save(user);
            return ResponseEntity.ok("Password reset successful.");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP.");
        }
    }

    // --- DTOs ---
    public record SignupRequest(String username, String email, String password , Role role) {}
    public record LoginRequest(String identifier, String password) {}
    public record JwtResponse(String token, String username, String email, String role) {}
    public record OtpVerifyRequest(String email, String otp) {}
    public record ResendOtpRequest(String email, String type) {} // type = "REGISTER", "LOGIN", etc.
    public record EmailRequest(String email) {}
    public record ResetPasswordRequest(String email, String otp, String newPassword) {}
}
