package kongju.pickmeal.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;

public class RedisConfigTest {
    @Test
    void should_create_redis_template() {
        RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);

        RedisConfig redisConfig = new RedisConfig();

        RedisTemplate<String, String> redisTemplate = redisConfig.redisTemplate(connectionFactory);

        assertThat(redisTemplate.getConnectionFactory()).isSameAs(connectionFactory);

        assertThat(redisTemplate.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);

        assertThat(redisTemplate.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
    }
}
