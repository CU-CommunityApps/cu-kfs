/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

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
import org.kuali.kfs.kim.impl.identity.entity.Entity;
import org.kuali.kfs.kim.impl.identity.name.EntityName;
import org.kuali.kfs.kim.impl.identity.principal.Principal;
import org.kuali.kfs.kim.impl.identity.privacy.EntityPrivacyPreferences;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

import edu.cornell.kfs.sys.cache.ClientTrackingLettuceConnectionFactory;
import edu.cornell.kfs.sys.cache.CuEhCacheCacheManager;
import edu.cornell.kfs.sys.cache.EhcacheEventListenerForUpdatingRedis;
import edu.cornell.kfs.sys.cache.RedisEventListenerLazyInitProxy;

/*
 * Cornell Customizations:
 * -- Updated caching to use an Ehcache-and-Redis hybrid setup.
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public Set<String> cacheNames() {
        return Set.of(
                Account.CACHE_NAME,
                AccountingPeriod.CACHE_NAME,
                BalanceType.CACHE_NAME,
                Bank.CACHE_NAME,
                BatchFile.CACHE_NAME,
                Chart.CACHE_NAME,
                CodedAttribute.CACHE_NAME,
                DelegateMember.CACHE_NAME,
                DelegateType.CACHE_NAME,
                DocumentType.CACHE_NAME,
                Entity.CACHE_NAME,
                EntityName.CACHE_NAME,
                EntityPrivacyPreferences.CACHE_NAME,
                Group.CACHE_NAME,
                GroupMember.CACHE_NAME,
                HomeOrigination.CACHE_NAME,
                KimAttribute.CACHE_NAME,
                KimType.CACHE_NAME,
                MenuService.MENU_LINKS_CACHE_NAME,
                Namespace.CACHE_NAME,
                ObjectCode.CACHE_NAME,
                ObjectType.CACHE_NAME,
                Organization.CACHE_NAME,
                OrgReviewRole.CACHE_NAME,
                Parameter.CACHE_NAME,
                Permission.CACHE_NAME,
                PermissionTemplate.CACHE_NAME,
                Principal.CACHE_NAME,
                ProjectCode.CACHE_NAME,
                Responsibility.CACHE_NAME,
                ResponsibilityTemplate.CACHE_NAME,
                Role.CACHE_NAME,
                RoleMember.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleResponsibility.CACHE_NAME,
                RoutePath.CACHE_NAME,
                RuleAttribute.CACHE_NAME,
                SubAccount.CACHE_NAME,
                SystemOptions.CACHE_NAME,
                UniversityDate.CACHE_NAME,
                MaintenanceUtils.LOCKING_ID_CACHE_NAME
        );
    }

    @Bean
    public Set<String> cachesIgnoringRedisEvents() {
        return Set.of(
                BatchFile.CACHE_NAME
        );
    }

    @Bean
    public Set<String> cachesToClearOnRedisConnectionChange() {
        return Set.of(
                Parameter.CACHE_NAME,
                Entity.CACHE_NAME,
                EntityName.CACHE_NAME,
                EntityPrivacyPreferences.CACHE_NAME,
                Group.CACHE_NAME,
                GroupMember.CACHE_NAME,
                Principal.CACHE_NAME,
                Permission.CACHE_NAME,
                PermissionTemplate.CACHE_NAME,
                Responsibility.CACHE_NAME,
                ResponsibilityTemplate.CACHE_NAME,
                Role.CACHE_NAME,
                RoleMember.CACHE_NAME,
                RoleMembership.CACHE_NAME,
                RoleResponsibility.CACHE_NAME
        );
    }

    @Bean
    public Map<String, Duration> cacheExpires() {
        // These caches have a TTL value different from the default specified in the redis.default.ttl property
        // Cornell Note: We are not actively using this Map in our customized caching setup.
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
    public RedisEventListenerLazyInitProxy redisEventListener() {
        return new RedisEventListenerLazyInitProxy();
    }

    @Bean
    public RedisConnectionFactory connectionFactory(
            @Value("${redis.auth.token.password}") final String redisAuthTokenPassword,
            @Value("${redis.host}") final String redisHost,
            @Value("${redis.port}") final int redisPort,
            @Value("${redis.use.ssl}") final boolean redisUseSsl,
            RedisEventListenerLazyInitProxy redisEventListener
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

        final ClientTrackingLettuceConnectionFactory lettuceConnectionFactory =
                new ClientTrackingLettuceConnectionFactory(redisStandaloneConfiguration, lettuceClientConfiguration,
                        redisEventListener);

        return lettuceConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        redisTemplate.setValueSerializer(new StringRedisSerializer(StandardCharsets.UTF_8));
        return redisTemplate;
    }

    @Bean
    public EhcacheEventListenerForUpdatingRedis ehcacheEventListener(
            RedisTemplate<String, String> redisTemplate
    ) {
        EhcacheEventListenerForUpdatingRedis ehcacheEventListener = new EhcacheEventListenerForUpdatingRedis(
                redisTemplate);
        return ehcacheEventListener;
    }

    @Bean
    public CuEhCacheCacheManager cacheManager(
            @Value("${kfs.ehcache.config.location}") Resource ehcacheConfigLocation,
            @Value("${redis.default.ttl}") final Long redisDefaultTtl,
            RedisEventListenerLazyInitProxy redisEventListener,
            EhcacheEventListenerForUpdatingRedis ehcacheEventListener
    ) {
        EhCacheManagerFactoryBean ehcacheManagerFactory = new EhCacheManagerFactoryBean();
        ehcacheManagerFactory.setConfigLocation(ehcacheConfigLocation);
        ehcacheManagerFactory.afterPropertiesSet();
        
        CuEhCacheCacheManager cacheManager = new CuEhCacheCacheManager();
        cacheManager.setCacheManager(ehcacheManagerFactory.getObject());
        cacheManager.setRedisEventListenerProxy(redisEventListener);
        cacheManager.setEhcacheEventListener(ehcacheEventListener);
        cacheManager.setCacheNames(cacheNames());
        cacheManager.setCachesIgnoringRedisEvents(cachesIgnoringRedisEvents());
        cacheManager.setCachesToClearOnRedisConnectionChange(cachesToClearOnRedisConnectionChange());
        cacheManager.setDefaultTimeToLive(redisDefaultTtl);
        
        return cacheManager;
    }

}