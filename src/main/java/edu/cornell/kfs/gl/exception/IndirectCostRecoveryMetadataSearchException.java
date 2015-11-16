/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2016 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.gl.exception;

/**
 * An exception that is thrown if the search for Indirect Cost Recovery metadata fails.
 * Subclasses Exception rather than RuntimeException so that the Poster process may continue with other items.
 */
public class IndirectCostRecoveryMetadataSearchException extends Exception {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a IndirectCostRecoveryMetadataSearchException instance
   */
  public IndirectCostRecoveryMetadataSearchException() {
    super();
  }

  /**
   * Constructs a IndirectCostRecoveryMetadataSearchException instance, encapsulating a messaging
   * @param msg a helpful message
   */
  public IndirectCostRecoveryMetadataSearchException(String msg) {
    super(msg);
  }
}
