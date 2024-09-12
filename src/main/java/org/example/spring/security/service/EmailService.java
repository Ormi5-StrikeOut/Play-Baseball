package org.example.spring.security.service;

import java.util.Date;

import org.example.spring.security.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * 이메일 관련 기능을 제공하는 서비스 클래스입니다.
 * 이메일 인증, 비밀번호 재설정 등의 이메일 발송 기능을 담당합니다.
 */
@Service
public class EmailService {

	private final JavaMailSender mailSender;
	private final JwtUtils jwtUtils;

	@Value("${app.fe-url}")
	private String feUrl;

	@Autowired
	public EmailService(JavaMailSender mailSender, JwtUtils jwtUtils) {
		this.mailSender = mailSender;
		this.jwtUtils = jwtUtils;
	}

	/**
	 * 이메일 인증을 위한 이메일을 발송합니다.
	 *
	 * @param email 수신자 이메일 주소
	 * @param token 인증 토큰
	 */

	public void sendVerificationEmail(String email, String token) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Play_Baseball 이메일 인증");
		message.setText("다음 링크를 클릭하여 이메일을 인증하세요: " +
			feUrl + "/verify-email?token=" + token);
		mailSender.send(message);
	}

	/**
	 * 비밀번호 재설정을 위한 이메일을 발송합니다.
	 *
	 * @param email 수신자 이메일 주소
	 * @param token 비밀번호 재설정 토큰
	 */
	public void sendPasswordResetEmail(String email, String token) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(email);
		message.setSubject("Play_Baseball 비밀번호 재설정");
		message.setText("다음 링크를 클릭하여 비밀번호를 재설정하세요: " +
			feUrl + "/forgot-password?token=" + token);
		mailSender.send(message);
	}

	/**
	 * 이메일 인증용 토큰을 생성합니다.
	 *
	 * @param email 사용자 이메일 주소
	 * @return 생성된 JWT 토큰
	 */
	public String generateEmailToken(String email) {
		return Jwts.builder()
			.setSubject(email)
			.setIssuedAt(new Date())
			.setExpiration(new Date(System.currentTimeMillis() + jwtUtils.getEmailVerifyExpiration()))
			.signWith(jwtUtils.getSigningKey(), SignatureAlgorithm.HS256)
			.compact();
	}

}
