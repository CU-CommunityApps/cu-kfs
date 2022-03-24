/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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

import java.io.InputStream;
import java.util.List;

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
import org.kuali.kfs.integration.ar.AccountsReceivableCustomerInvoiceDetail;
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
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.businessobject.HomeOrigination;
import org.kuali.kfs.sys.businessobject.SystemOptions;
import org.kuali.kfs.sys.businessobject.UniversityDate;
import org.kuali.kfs.sys.service.MenuService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import edu.cornell.kfs.sys.cache.CuRedisCacheManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import net.sf.ehcache.config.ConfigurationFactory;

/*
 * Cornell Customization: backport redis and customize as needed to improve performance
 */
@Configuration
@EnableCaching
public class CacheConfiguration {

    @Bean
    public List<String> cacheNames() {
        return List.of(
                Account.CACHE_NAME ,
                AccountingPeriod.CACHE_NAME,
                AccountsReceivableCustomerInvoiceDetail.CACHE_NAME,
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
                OrgReviewRole.CACHE_NAME,
                Organization.CACHE_NAME,
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
                UniversityDate.CACHE_NAME
        );
    }

    @Bean
    public List<String> localCachesToClearOnListenerReset() {
        return List.of(
                Parameter.CACHE_NAME,
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
    public List<String> cachesIgnoringRedisEvents() {
        return List.of(
                BatchFile.CACHE_NAME
        );
    }

    @Bean
    public RedisClient redisClient(
            @Value("${redis.auth.token.password}") String redisAuthTokenPassword,
            @Value("${redis.host}") String redisHost,
            @Value("${redis.port}") int redisPort,
            @Value("${redis.use.ssl}") boolean redisUseSsl
    ) {
        RedisURI redisURI = RedisURI.Builder.redis(redisHost, redisPort)
                .withPassword(redisAuthTokenPassword.toCharArray())
                .withSsl(redisUseSsl)
                .build();
        RedisClient redisClient = RedisClient.create(redisURI);
        return redisClient;
    }

    @Bean
    public CuRedisCacheManager cacheManager(
            @Value("${redis.default.ttl}") Long redisDefaultTtl,
            @Value("${kfs.ehcache.config.location}") Resource configLocation,
            RedisClient redisClient
    ) throws Exception {
        net.sf.ehcache.config.Configuration ehCacheConfiguration;
        try (InputStream configLocationStream = configLocation.getInputStream()) {
            ehCacheConfiguration = ConfigurationFactory.parseConfiguration(configLocationStream);
        }
        
        net.sf.ehcache.CacheManager localCacheManager = net.sf.ehcache.CacheManager.create(ehCacheConfiguration);
        
        CuRedisCacheManager cacheManager = new CuRedisCacheManager();
        cacheManager.setCacheNames(cacheNames());
        cacheManager.setLocalCachesToClearOnListenerReset(localCachesToClearOnListenerReset());
        cacheManager.setCachesIgnoringRedisEvents(cachesIgnoringRedisEvents());
        cacheManager.setDefaultTimeToLiveInSeconds(redisDefaultTtl);
        cacheManager.setLocalCacheManager(localCacheManager);
        cacheManager.setRedisClient(redisClient);
        return cacheManager;
    }

}
