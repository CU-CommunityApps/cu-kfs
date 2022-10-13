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
package org.kuali.kfs.kns.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Optional;

/*
 * KFSPTS-22552 - fix issue with Jagger - eShop integration
 */
public class CookieUtils {

    public static final String AUTH_TOKEN_COOKIE_NAME = "authToken";
    public static final String FIN_AUTH_TOKEN_COOKIE_NAME = "financialsAuthToken";

    public Cookie createCookie(HttpServletRequest request, String name, String value, String path) {
        Cookie cookie = new Cookie(name, value);
        /*
         * KFSPTS-22552
         * Original base code was cookie.setSecure(request.isSecure()); 
         */
        cookie.setSecure(true);
        cookie.setPath(path);
        return cookie;
    }

    public Cookie createFinancialsAuthCookie(HttpServletRequest request, String tokenValue, String path) {
        Cookie financialsAuthCookie = createCookie(request, FIN_AUTH_TOKEN_COOKIE_NAME, tokenValue, path);
        return financialsAuthCookie;
    }

    public Cookie createExpiringCookie(HttpServletRequest request, String name, String path) {
        Cookie emptyCookie = createCookie(request, name, "", path);
        emptyCookie.setMaxAge(0);
        return emptyCookie;
    }

    public Cookie createExpiringFinanicialsAuthCookie(HttpServletRequest httpRequest) {
        Cookie financialsAuthCookie =
                createExpiringCookie(httpRequest, FIN_AUTH_TOKEN_COOKIE_NAME, httpRequest.getContextPath());
        return financialsAuthCookie;
    }

    public Cookie createExpiringAuthCookie(HttpServletRequest httpRequest) {
        Cookie cookie = createExpiringCookie(httpRequest, AUTH_TOKEN_COOKIE_NAME, "/");
        cookie.setDomain("kuali.co");
        return cookie;
    }

    public Optional<String> getFinancialsAuthToken(HttpServletRequest httpRequest) {
        return getCookieValue(httpRequest, FIN_AUTH_TOKEN_COOKIE_NAME);
    }

    public Optional<String> getCoreAuthToken(HttpServletRequest httpRequest) {
        return getCookieValue(httpRequest, AUTH_TOKEN_COOKIE_NAME);
    }

    public Optional<String> getCookieValue(HttpServletRequest httpRequest, String name) {
        Cookie[] cookies = httpRequest.getCookies();
        return cookies != null ? Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name))
                .findFirst()
                .map(Cookie::getValue) : Optional.empty();
    }
}
