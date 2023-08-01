/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.sys.web;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The purpose of this class is to bridge chasm between Spring's XML-based configuration and HttpClient 5's
 * builder-centric creation. Once we move to @Configuration classes instead of XML, this will no longer be necessary.
 */
@SuppressWarnings("PMD.CloseResource")
public final class WebClientFactory {

    private WebClientFactory() {
        // Prevent instantiation
    }

    /**
     * Creates a new WebClient which follows redirects with proper response cookie handling
     * @return The new WebClient instance
     */
    public static WebClient create() {
        final HttpAsyncClientBuilder clientBuilder = HttpAsyncClientBuilder.create();
        final RequestConfig requestConfig = RequestConfig.custom().setRedirectsEnabled(true).build();
        clientBuilder.setDefaultRequestConfig(requestConfig);
        final ClientHttpConnector connector = new HttpComponentsClientHttpConnector(clientBuilder.build());
        return WebClient.builder().clientConnector(connector).build();
    }
}