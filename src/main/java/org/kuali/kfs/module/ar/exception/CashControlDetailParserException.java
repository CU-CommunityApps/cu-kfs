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
package org.kuali.kfs.module.ar.exception;

public class CashControlDetailParserException extends RuntimeException {
    private final String errorKey;
    private final String[] errorParameters;

    /**
     * Constructs an CashControlDetailParserException instance.
     *
     * @param message         error message
     * @param errorKey        key to an error message
     * @param errorParameters error message parameters
     */
    public CashControlDetailParserException(
            final String message,
            final String errorKey,
            final String... errorParameters
    ) {
        super(message);
        this.errorKey = errorKey;
        this.errorParameters = errorParameters;
    }

    public String getErrorKey() {
        return errorKey;
    }

    public String[] getErrorParameters() {
        return errorParameters;
    }
}