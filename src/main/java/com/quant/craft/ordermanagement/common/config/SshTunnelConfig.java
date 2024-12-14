package com.quant.craft.ordermanagement.common.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class SshTunnelConfig {

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "spring.datasource")
    public static class DataSourceProperties {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "ssh.tunnel")
    public static class SshTunnelProperties {
        private String username;
        private String host;
        private String privateKeyPath;
        private int port;
        private String dbHost;
        private int dbPort;
        private int localPort;
    }

    @Bean
    public Session sshSession(SshTunnelProperties properties) throws Exception {
        JSch jsch = new JSch();
        jsch.addIdentity(properties.getPrivateKeyPath());

        Session session = jsch.getSession(
                properties.getUsername(),
                properties.getHost(),
                properties.getPort()
        );

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();

        session.setPortForwardingL(
                properties.getLocalPort(),
                properties.getDbHost(),
                properties.getDbPort()
        );

        return session;
    }

    @Bean
    @DependsOn("sshSession")
    public DataSource dataSource(DataSourceProperties properties, SshTunnelProperties sshProperties) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setDriverClassName(properties.getDriverClassName());
        // 원래 URL의 포트를 SSH 터널링된 로컬 포트로 변경
        String jdbcUrl = properties.getUrl().replace(
                ":" + sshProperties.getDbPort(),
                ":" + sshProperties.getLocalPort()
        );
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        return dataSource;
    }

}