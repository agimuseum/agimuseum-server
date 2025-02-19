package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.ChangePassword;
import com.agimuseum.magi.dto.MailBody;
import com.agimuseum.magi.model.ForgotPassword;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.ForgotPasswordRepository;
import com.agimuseum.magi.repository.UserRepository;
import com.agimuseum.magi.service.EmailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/forgotPassword")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository, EmailService emailService, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // send mail for email verification
    @PostMapping("/verifyEmail/{username}")
    public ResponseEntity<String> verifyEmail(@PathVariable String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide valid and existing email!"));

        int otp = otpGenerator();

        MailBody mailBody = MailBody.builder()
                .to(username)
                .text("This is the OTP for your Forgot Password request : " + otp)
                .subject("OTP for Forgot Password request")
                .build();

        // Delete existing forgot password
        Optional<ForgotPassword> existingForgotPassword = forgotPasswordRepository.findByUser(user);
        if (existingForgotPassword.isPresent()) {
            // Delete the existing one
            forgotPasswordRepository.deleteByUser(user);
            // Flush to ensure the delete is processed
            forgotPasswordRepository.flush();
        }

        // Create new forgot password
        ForgotPassword fp = ForgotPassword.builder()
                .otp(otp)
                .expirationTime(new Date(System.currentTimeMillis() + 70 * 1000))
                .user(user)
                .build();
        forgotPasswordRepository.save(fp);

        emailService.sendSimpleMessage(mailBody);

        return ResponseEntity.ok("Email sent for verification!");

    }

    @PostMapping("/verifyOtp/{otp}/{username}")
    public ResponseEntity<String> verifyOtp(@PathVariable Integer otp, @PathVariable String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide valid and existing email!"));

        ForgotPassword forgotPassword = forgotPasswordRepository.findByOtpAndUser(otp, user)
                .orElseThrow(() -> new RuntimeException("Invalid OTP for email : " + username));

        if(forgotPassword.getExpirationTime().before(Date.from(Instant.now()))){
            forgotPasswordRepository.deleteById(forgotPassword.getId());
            return new ResponseEntity<>("OTP has expired", HttpStatus.EXPECTATION_FAILED);
        }

        return ResponseEntity.ok("OTP verified");
    }

    @PostMapping("/changePassword/{username}")
    public ResponseEntity<String> changePasswordHandler(@RequestBody ChangePassword changePassword,
                                                        @PathVariable String username){
        if(!Objects.equals(changePassword.password(), changePassword.repeatPassword())){
            return new ResponseEntity<>("Please enter the password again!", HttpStatus.EXPECTATION_FAILED);
        }

        String encodedPassword = passwordEncoder.encode(changePassword.password());
        userRepository.updatePassword(username, encodedPassword);

        return ResponseEntity.ok("Password has been changed!");
    }


    private Integer otpGenerator() {
        Random random = new Random();
        return random.nextInt(100_000, 999_999);
    }

    @GetMapping("/demo")
    public ResponseEntity<String> demo(){
        return ResponseEntity.ok("yo yo yo");
    }

}
