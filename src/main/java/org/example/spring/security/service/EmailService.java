package org.example.spring.security.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import org.example.spring.security.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final JwtUtils jwtUtils;

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
            "http://localhost:8080/api/members/verify-email?token=" + token);
        mailSender.send(message);
    }


    public String generateEmailVerificationToken(String email) {
        return Jwts.builder()
            .setSubject(email)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtUtils.getEmailVerifyExpiration()))
            .signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
}
