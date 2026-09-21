package org.example.demo1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConfig {

    // 用户信息模板：db0
    @Bean
    @Primary
    public StringRedisTemplate userRedisTemplate() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("192.168.88.128");
        config.setPort(6379);
        config.setPassword("Qwer1234@");
        config.setDatabase(0);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
        factory.afterPropertiesSet();
        return new StringRedisTemplate(factory);
    }

    // 验证码专用模板：db1
    @Bean("captchaRedisTemplate")
    public StringRedisTemplate captchaRedisTemplate() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName("192.168.88.128");
        config.setPort(6379);
        config.setPassword("Qwer1234@");
        config.setDatabase(1); //验证码存入db1

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
        factory.afterPropertiesSet();
        return new StringRedisTemplate(factory);
    }
}