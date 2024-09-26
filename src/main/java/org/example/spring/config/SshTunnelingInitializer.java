package org.example.spring.config;

import static java.lang.System.*;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import jakarta.annotation.PreDestroy;
import jakarta.validation.constraints.NotNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Validated
@Profile("dev")
@Setter
public class SshTunnelingInitializer {

	@NotNull
	@Value("${ssh.remote_jump_host}")
	private String remoteJumpHost;
	@NotNull
	@Value("${ssh.user}")
	private String user;
	@NotNull
	@Value("${ssh.ssh_port}")
	private int sshPort;
	@NotNull
	@Value("${ssh.private_key}")
	private String privateKey;
	@NotNull
	@Value("${ssh.database_url}")
	private String databaseUrl;
	@NotNull
	@Value("${ssh.database_port}")
	private int databasePort;

	private Session session;

	@PreDestroy
	public void closeSSH() {
		if (session.isConnected())
			session.disconnect();
	}

	public Integer buildSshConnection() {

		Integer forwardedPort = null;

		try {
			log.info("{}@{}:{}:{} with privateKey", user, remoteJumpHost, sshPort, databasePort);

			log.info("start ssh tunneling..");
			JSch jSch = new JSch();

			log.info("creating ssh session");
			jSch.addIdentity(privateKey);  // 개인키
			session = jSch.getSession(user, remoteJumpHost, sshPort);  // 세션 설정
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			log.info("complete creating ssh session");

			log.info("start connecting ssh connection");
			session.connect();  // ssh 연결
			log.info("success connecting ssh connection ");

			// 로컬pc의 남는 포트 하나와 원격 접속한 pc의 db포트 연결
			log.info("start forwarding");
			forwardedPort = session.setPortForwardingL(0, databaseUrl, databasePort);
			log.info("successfully connected to database");

		} catch (Exception e) {
			log.error("fail to make ssh tunneling");
			this.closeSSH();
			e.printStackTrace();
			exit(1);
		}

		return forwardedPort;
	}
}