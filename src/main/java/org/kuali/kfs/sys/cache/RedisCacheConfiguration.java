/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.cache;

import org.kuali.kfs.krad.maintenance.MaintenanceUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Redis is our default caching implementation -- it is high-performance and can be used in a distributed environment.
 */
@Configuration
@Profile("cache-redis | !cache-map")
public class RedisCacheConfiguration {

    /*
     * Cornell Customization 
     * -- add more cache names 
     */
    @Bean
    public Set<String> cacheNamesWithDefaultTtl() {
        final Set<String> cornellCacheNamesWithDefaultTtl = Set.of(
                MaintenanceUtils.LOCKING_ID_CACHE_NAME
        );
        return Stream.of(SharedCacheConfiguration.staticCacheNamesWithDefaultTtl(), cornellCacheNamesWithDefaultTtl)
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Bean
    public Map<String, Duration> cacheNamesWithCustomTtl() {
        return SharedCacheConfiguration.staticCacheNamesWithCustomTtl();
    }

    @Bean
    public RedisConnectionFactory connectionFactory(
            @Value("${redis.auth.token.password}") final String redisAuthTokenPassword,
            @Value("${redis.host}") final String redisHost,
            @Value("${redis.port}") final int redisPort,
            @Value("${redis.use.ssl}") final boolean redisUseSsl
    ) {
        final RedisStandaloneConfiguration redisStandaloneConfiguration =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        redisStandaloneConfiguration.setPassword(redisAuthTokenPassword);

        final LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceClientConfigurationBuilder =
                LettuceClientConfiguration.builder();
        if (redisUseSsl) {
            lettuceClientConfigurationBuilder.useSsl();
        }
        final LettuceClientConfiguration lettuceClientConfiguration = lettuceClientConfigurationBuilder.build();

        return new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);
    }

    // Note: Not sure exactly why, but Qualifier annotation is required in order for cacheNamesWithDefaultTtl to be
    // setup before this method is called.
    /*
     * CU Customization:
     * 
     * -- Added a method argument for injecting a common cache name prefix.
     * -- Added a call to "prefixCacheNameWith()" when building the cache configuration.
     */
    @Bean
    public RedisCacheManager cacheManager(
            @Value("${redis.default.ttl}") final Long redisDefaultTtl,
            @Value("${cu.redis.cache.environment.prefix}") final String environmentPrefix,
            @Qualifier("cacheNamesWithDefaultTtl") final Set<String> cacheNamesWithDefaultTtl,
            final Map<String, Duration> cacheNamesWithCustomTtl,
            final RedisConnectionFactory connectionFactory
    ) {
        final org.springframework.data.redis.cache.RedisCacheConfiguration
                cacheDefaults = org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisDefaultTtl))
                .prefixCacheNameWith(environmentPrefix);

        final Map<String, org.springframework.data.redis.cache.RedisCacheConfiguration> cacheConfigurations = cacheNamesWithCustomTtl.keySet()
                .stream()
                .collect(Collectors.toMap(key -> key,
                        key -> cacheDefaults.entryTtl(cacheNamesWithCustomTtl.get(key)),
                        (configuration, cacheConfiguration) -> cacheConfiguration));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheDefaults)
                .initialCacheNames(cacheNamesWithDefaultTtl)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
