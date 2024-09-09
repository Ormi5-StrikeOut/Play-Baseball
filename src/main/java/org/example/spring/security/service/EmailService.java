package org.example.spring.security.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.example.spring.security.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final JwtUtils jwtUtils;

    @Value("${app.base-url}")
    private String baseUrl;

    @Autowired
    public EmailService(JavaMailSender mailSender, JwtUtils jwtUtils) {
        this.mailSender = mailSender;
        this.jwtUtils = jwtUtils;
    }

    public void sendVerificationEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증");
        message.setText("다음 링크를 클릭하여 이메일을 인증하세요: " +
            baseUrl + "/api/members/verify-email?token=" + token);
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("비밀번호 재설정");
        message.setText("다음 링크를 클릭하여 비밀번호를 재설정하세요: " +
            baseUrl + "/api/members/reset-password?token=" + token);
        mailSender.send(message);
    }

    public String generateEmailToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtUtils.getEmailVerifyExpiration()))
            .signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

}
