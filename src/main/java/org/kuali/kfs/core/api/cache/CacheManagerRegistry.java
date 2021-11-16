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
package org.kuali.kfs.core.api.cache;

import org.springframework.cache.CacheManager;

import java.util.List;

/**
 * Allows access to a registry of {@link CacheManager} instances that are identified by name.
 *
 */
/* Cornell Customization: backport redis*/
public interface CacheManagerRegistry {

    /**
     * Will return a list of registered cache managers.  Will not return null but may return an empty list.
     *
     * @return a list of cache managers
     */
    List<CacheManager> getCacheManagers();

    /**
     * Gets a cache manager for a given cache name.  Name cannot be null or blank.
     *
     * @param cacheName the  name of a Cache in a CacheManager.
     * @return the CacheManager
     * @throws IllegalArgumentException if the name is null or blank
     */
    CacheManager getCacheManagerByCacheName(String cacheName);
}
