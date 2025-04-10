package com.zufang.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.protocol.ProtocolVersion;
import com.fasterxml.jackson.core.JsonParser;

import java.time.Duration;

/**
 * Redis配置类
 */
@Configuration
public class RedisConfig {
    
    /**
     * 配置Redis连接工厂
     * 注释掉此方法，使用application.yml中的配置
     */
    /*
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 创建Redis配置
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName("115.120.221.163");
        redisConfig.setPort(6379);
        redisConfig.setPassword("123456");
        redisConfig.setDatabase(0);
        
        // 创建连接池配置
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(8);
        poolConfig.setMaxIdle(8);
        poolConfig.setMinIdle(0);
        poolConfig.setMaxWait(Duration.ofSeconds(3));
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        
        // 创建客户端资源
        ClientResources clientResources = DefaultClientResources.builder()
                .ioThreadPoolSize(4)
                .computationThreadPoolSize(4)
                .build();
        
        // 创建客户端选项
        ClientOptions clientOptions = ClientOptions.builder()
                .autoReconnect(true)
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
                .protocolVersion(ProtocolVersion.RESP2)
                .build();
        
        // 创建Lettuce连接池配置
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(3))
                .clientOptions(clientOptions)
                .clientResources(clientResources)
                .poolConfig(poolConfig)
                .readFrom(ReadFrom.MASTER)
                .build();
        
        // 创建Lettuce连接工厂
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
        factory.setShareNativeConnection(false);
        factory.afterPropertiesSet();
        return factory;
    }
    */
    
    /**
     * 配置RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        
        // 创建ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        
        // 允许使用未带引号的字段名
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        
        // 添加JavaTimeModule以支持Java 8日期/时间类型
        objectMapper.registerModule(new JavaTimeModule());
        
        // 禁用默认的日期时间格式化
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会抛出异常
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        
        // 使用新的方式设置ObjectMapper - 创建一个带有配置好的ObjectMapper的新序列化器
        jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
        
        // 使用StringRedisSerializer来序列化和反序列化redis的key值
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        
        // value序列化方式采用jackson
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
} 