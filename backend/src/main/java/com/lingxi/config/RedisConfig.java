package com.lingxi.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置：String Key + JSON Value。
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper objectMapper = new ObjectMapper();
        // 使用 ALL/ANY 可见性，允许序列化没有 getter 的字段（如 Lombok DTO 的私有字段）。
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // Redis 的 value 类型是 Object，必须写入类型信息才能在反序列化时还原成原始类型（如 List、实体类）；
        // 仅反序列化受信任的缓存数据，避免默认类型机制被恶意 payload 利用。
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        // 支持 LocalDateTime 等 Java 8 时间类型，与业务实体中的时间字段保持一致。
        objectMapper.registerModule(new JavaTimeModule());
        jsonSerializer.setObjectMapper(objectMapper);

        // Key 使用纯字符串便于在 redis-cli 中排查；Value 使用 JSON 便于跨语言读取和调试。
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        // 手动构建的 RedisTemplate 需要显式初始化，否则部分序列化器配置不会生效。
        template.afterPropertiesSet();
        return template;
    }
}
