/*
 * Copyright (c) 2018-2021 Holger de Carne and contributors, All Rights Reserved.
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
package de.carne.ddns;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.carne.ddns.rest.IpMeInquirer;
import de.carne.ddns.rest.IpifyInquirer;
import de.carne.ddns.util.CombinedInquirer;
import de.carne.util.Exceptions;
import de.carne.util.Late;

/**
 * Test {@linkplain Inquirer} implementations.
 */
class InquirerTest {

	private static final Late<Inet4Address> IPV4_ADDRESS_HOLDER = new Late<>();
	private static final Late<Inet6Address> IPV6_ADDRESS_HOLDER = new Late<>();

	@BeforeAll
	static void getMyAddress() throws IOException {
		Inquirer inquirer = new CombinedInquirer();

		IPV4_ADDRESS_HOLDER.set(Objects.requireNonNull(inquirer.queryIPv4Address()));
		IPV6_ADDRESS_HOLDER.set(Objects.requireNonNull(inquirer.queryIPv6Address()));
	}

	@Test
	void testCombinedInquirer() throws IOException {
		Inquirer inquirer = new CombinedInquirer();

		testInquirer(inquirer, false);
	}

	@Test
	void testIpMeInquirerDefault() throws IOException {
		Inquirer inquirer = new IpMeInquirer();

		testInquirer(inquirer, true);
	}

	@Test
	void testIpMeInquirerNoSSL() throws IOException {
		Inquirer inquirer = new IpMeInquirer(false);

		testInquirer(inquirer, true);
	}

	@Test
	void testIpifyInquirerDefault() throws IOException {
		Inquirer inquirer = new IpifyInquirer();

		testInquirer(inquirer, true);
	}

	@Test
	void testIpifyInquirerNoSSL() throws IOException {
		Inquirer inquirer = new IpifyInquirer(false);

		testInquirer(inquirer, true);
	}

	private void testInquirer(Inquirer inquirer, boolean ignoreIOError) throws IOException {
		try {
			Inet4Address ipv4Address = inquirer.queryIPv4Address();

			Assertions.assertNotNull(ipv4Address);
			Assertions.assertEquals(IPV4_ADDRESS_HOLDER.get(), ipv4Address);
		} catch (IOException e) {
			if (ignoreIOError) {
				Exceptions.ignore(e);
			} else {
				throw e;
			}
		}

		try {
			Inet6Address ipv6Address = inquirer.queryIPv6Address();

			Assertions.assertNotNull(ipv6Address);
			Assertions.assertEquals(IPV6_ADDRESS_HOLDER.get(), ipv6Address);
		} catch (IOException e) {
			if (ignoreIOError) {
				Exceptions.ignore(e);
			} else {
				throw e;
			}
		}
	}

}
