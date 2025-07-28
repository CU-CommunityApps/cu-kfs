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

import static java.util.Map.entry;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountingPeriod;
import org.kuali.kfs.coa.businessobject.BalanceType;
import org.kuali.kfs.coa.businessobject.Chart;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.ObjectType;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.businessobject.ProjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coa.identity.OrgReviewRole;
import org.kuali.kfs.coreservice.impl.namespace.Namespace;
import org.kuali.kfs.coreservice.impl.parameter.Parameter;
import org.kuali.kfs.kew.api.doctype.RoutePath;
import org.kuali.kfs.kew.doctype.bo.DocumentType;
import org.kuali.kfs.kew.rule.bo.RuleAttribute;
import org.kuali.kfs.kim.api.role.RoleMembership;
import org.kuali.kfs.kim.impl.common.attribute.KimAttribute;
import org.kuali.kfs.kim.impl.common.delegate.DelegateMember;
import org.kuali.kfs.kim.impl.common.delegate.DelegateType;
import org.kuali.kfs.kim.impl.group.Group;
import org.kuali.kfs.kim.impl.group.GroupMember;
import org.kuali.kfs.kim.impl.identity.CodedAttribute;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kim.impl.permission.Permission;
import org.kuali.kfs.kim.impl.permission.PermissionTemplate;
import org.kuali.kfs.kim.impl.responsibility.Responsibility;
import org.kuali.kfs.kim.impl.responsibility.ResponsibilityTemplate;
import org.kuali.kfs.kim.impl.role.Role;
import org.kuali.kfs.kim.impl.role.RoleMember;
import org.kuali.kfs.kim.impl.role.RoleResponsibility;
import org.kuali.kfs.kim.impl.type.KimType;
import org.kuali.kfs.krad.maintenance.MaintenanceUtils;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.HomeOrigination;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.service.MenuService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
@EnableCaching
@Profile("cache-redis")
public class CacheConfiguration {
    
    /*
     * Cornell Customization 
     * -- add more cache names 
     */
    @Bean
    public Set<String> cacheNamesWithDefaultTtl() {
        final Set<String> cornellCacheNamesWithDefaultTtl = Set.of(
                MaintenanceUtils.LOCKING_ID_CACHE_NAME
        );
        return Stream.of(staticCacheNamesWithDefaultTtl(), cornellCacheNamesWithDefaultTtl)
                .flatMap(Set::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * Used in CSU overlay
     *
     * @return Set of cache names for caches that use the default TTL
     * <p>
     * Note: Both the collection of cache names using the default TTL and the collection of cache names with custom
     * TTL values are used when setting up the RedisCacheManager and the resulting set of caches is a superset, so an
     * individual cache name only needs to be included in one (staticCacheNamesWithDefaultTtl) or the other
     * (staticCacheNamesWithCustomTtl) but not both.
     */
    public static Set<String> staticCacheNamesWithDefaultTtl() {
        return Set.of(
                CodedAttribute.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                DelegateType.CACHE_NAME,
                Group.CACHE_NAME,
                GroupMember.CACHE_NAME,
                KimAttribute.CACHE_NAME,
                KimType.CACHE_NAME,
                Permission.CACHE_NAME,
                PermissionTemplate.CACHE_NAME,
                Person.CACHE_NAME,
                Responsibility.CACHE_NAME,
                ResponsibilityTemplate.CACHE_NAME,
                Role.CACHE_NAME,
                RoleMember.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleResponsibility.CACHE_NAME
        );
    }

    @Bean
    public Map<String, Duration> cacheNamesWithCustomTtl() {
        return staticCacheNamesWithCustomTtl();
    }

    /**
     * Used in CSU overlay
     *
     * @return Map of cache names and custom TTL Duration for caches that have a TTL value different from the default
     * specified in the redis.default.ttl property
     * <p>
     * Note: Both the collection of cache names using the default TTL and the collection of cache names with custom
     * TTL values are used when setting up the RedisCacheManager and the resulting set of caches is a superset, so an
     * individual cache name only needs to be included in one (staticCacheNamesWithDefaultTtl) or the other
     * (staticCacheNamesWithCustomTtl) but not both.
     */
    public static Map<String, Duration> staticCacheNamesWithCustomTtl() {
        return Map.ofEntries(
                entry(Account.CACHE_NAME, Duration.ZERO),
                entry(AccountingPeriod.CACHE_NAME, Duration.ZERO),
                entry(BalanceType.CACHE_NAME, Duration.ZERO),
                entry(Bank.CACHE_NAME, Duration.ZERO),
                entry(BatchFile.CACHE_NAME, Duration.ZERO),
                entry(Chart.CACHE_NAME, Duration.ZERO),
                entry(DocumentType.CACHE_NAME, Duration.ofSeconds(3600L)),
                entry(HomeOrigination.CACHE_NAME, Duration.ZERO),
                entry(MenuService.MENU_LINKS_CACHE_NAME, Duration.ofSeconds(86760L)),
                entry(Namespace.CACHE_NAME, Duration.ofSeconds(3600L)),
                entry(ObjectCode.CACHE_NAME, Duration.ZERO),
                entry(ObjectType.CACHE_NAME, Duration.ZERO),
                entry(Organization.CACHE_NAME, Duration.ZERO),
                entry(OrgReviewRole.CACHE_NAME, Duration.ZERO),
                entry(Parameter.CACHE_NAME, Duration.ofSeconds(3600L)),
                entry(ProjectCode.CACHE_NAME, Duration.ZERO),
                entry(RoutePath.CACHE_NAME, Duration.ofSeconds(3600L)),
                entry(RuleAttribute.CACHE_NAME, Duration.ofSeconds(3600L)),
                entry(SubAccount.CACHE_NAME, Duration.ZERO),
                entry(SystemOptions.CACHE_NAME, Duration.ZERO),
                entry(UniversityDate.CACHE_NAME, Duration.ZERO)
        );
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

        final LettuceConnectionFactory lettuceConnectionFactory =
                new LettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration);

        return lettuceConnectionFactory;
    }

    // Note: Not sure exactly why, but Qualifier annotation is required in order for cacheNamesWithDefaultTtl to be
    // setup before this method is called.
    /*
     * CU Customization:
     * 
     * -- Added a method argument for injecting a common cache name prefix.
     * -- Removed the call to "disableKeyPrefix()" when building the cache configuration.
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
        final RedisCacheConfiguration cacheDefaults = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisDefaultTtl))
                .prefixCacheNameWith(environmentPrefix);

        final Map<String, RedisCacheConfiguration> cacheConfigurations = cacheNamesWithCustomTtl.keySet()
                .stream()
                .collect(Collectors.toMap(key -> key,
                        key -> cacheDefaults.entryTtl(cacheNamesWithCustomTtl.get(key)),
                        (configuration, cacheConfiguration) -> cacheConfiguration));

        final RedisCacheManager redisCacheManager = RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheDefaults)
                .initialCacheNames(cacheNamesWithDefaultTtl)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();

        return redisCacheManager;
    }
}
