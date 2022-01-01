/*
 * Copyright (c) 2018-2022 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.ddns.test;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.ddns.Merger;

/**
 * Common interface for {@linkplain Merger} mocks.
 */
public interface MergerMock {

	/**
	 * Test domain.
	 */
	String TEST_DOMAIN = "domain.tld";

	/**
	 * Test host.
	 */
	String TEST_HOST = "host." + TEST_DOMAIN;

	/**
	 * Initial A record.
	 */
	String TEST_A_RECORD_OLD = "1.1.1.1";

	/**
	 * Initial AAAA record.
	 */
	String TEST_A_RECORD_NEW = "1.2.3.4";

	/**
	 * Updated A record.
	 */
	String TEST_AAAA_RECORD_OLD = "1:1:1:1:1:1:1:1";

	/**
	 * Updated AAAA record.
	 */
	String TEST_AAAA_RECORD_NEW = "1:2:3:4:c:d:e:f";

	/**
	 * Sets the mock's A record.
	 *
	 * @param aRecord the A record to set (may be {@code null}).
	 */
	void setARecord(@Nullable String aRecord);

	/**
	 * Gets the mock's A record.
	 *
	 * @return the mock's A record (may be {@code null}).
	 */
	@Nullable
	String getARecord();

	/**
	 * Sets the mock's AAAA record.
	 *
	 * @param aaaaRecord the AAAA record to set (may be {@code null}).
	 */
	void setAAAARecord(@Nullable String aaaaRecord);

	/**
	 * Gets the mock's AAAA record.
	 *
	 * @return the mock's AAAA record (may be {@code null}).
	 */
	@Nullable
	String getAAAARecord();

}
