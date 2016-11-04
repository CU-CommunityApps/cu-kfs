/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.xmlObjects;

public interface PaymentWorksAddress {

	public String getId();

	public void setId(String id);

	public String getName();

	public void setName(String name);

	public String getStreet1();

	public void setStreet1(String street1);

	public String getStreet2();

	public void setStreet2(String street2);

	public String getCity();

	public void setCity(String city);

	public String getState();

	public void setState(String state);

	public String getCountry();

	public void setCountry(String country);

	public String getZipcode();

	public void setZipcode(String zipcode);

}
