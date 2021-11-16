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
package org.kuali.kfs.core.impl.cache;

import org.kuali.kfs.core.api.cache.CacheManagerRegistry;
import org.springframework.cache.CacheManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple class that holds a global registry to the cache managers.
 */
/* Cornell Customization: backport redis*/
public final class CacheManagerRegistryImpl implements CacheManagerRegistry {

    private static final List<CacheManager> CACHE_MANAGERS = new CopyOnWriteArrayList<>();
    private static final Map<String, CacheManager> CACHE_MANAGER_MAP = new ConcurrentHashMap<>();

    public void setCacheManager(CacheManager c) {
        if (c == null) {
            throw new IllegalArgumentException("c is null");
        }

        CACHE_MANAGERS.add(c);

        //keep map as well
        for (String cacheName : c.getCacheNames()) {
            CACHE_MANAGER_MAP.put(cacheName, c);
        }
    }

    @Override
    public List<CacheManager> getCacheManagers() {
        return Collections.unmodifiableList(CACHE_MANAGERS);
    }

    @Override
    public CacheManager getCacheManagerByCacheName(String cacheName) {
        CacheManager cm = CACHE_MANAGER_MAP.get(cacheName);
        if (cm != null) {
            return cm;
        }
        throw new IllegalArgumentException("Cache not found : " + cacheName);
    }
}
