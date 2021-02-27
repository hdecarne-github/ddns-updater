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

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.carne.ddns.rest.IpMeInquirer;
import de.carne.ddns.rest.IpifyInquirer;
import de.carne.ddns.util.CombinedInquirer;
import de.carne.util.Exceptions;

/**
 * Test {@linkplain Inquirer} implementations.
 */
class InquirerTest {

	@Nullable
	private static Inet4Address testIPv4Address = null;
	@Nullable
	private static Inet6Address testIPv6Address = null;

	@BeforeAll
	static void getMyAddress() {
		Inquirer inquirer = new CombinedInquirer();

		try {
			testIPv4Address = inquirer.queryIPv4Address();
		} catch (IOException e) {
			Exceptions.ignore(e);
		}
		try {
			testIPv6Address = inquirer.queryIPv6Address();
		} catch (IOException e) {
			Exceptions.ignore(e);
		}
	}

	@Test
	void testCombinedInquirer() {
		Inquirer inquirer = new CombinedInquirer();

		testInquirer(inquirer);
	}

	@Test
	void testIpMeInquirerDefault() {
		Inquirer inquirer = new IpMeInquirer();

		testInquirer(inquirer);
	}

	@Test
	void testIpMeInquirerNoSSL() {
		Inquirer inquirer = new IpMeInquirer(false);

		testInquirer(inquirer);
	}

	@Test
	void testIpifyInquirerDefault() {
		Inquirer inquirer = new IpifyInquirer();

		testInquirer(inquirer);
	}

	@Test
	void testIpifyInquirerNoSSL() {
		Inquirer inquirer = new IpifyInquirer(false);

		testInquirer(inquirer);
	}

	private void testInquirer(Inquirer inquirer) {
		try {
			Inet4Address ipv4Address = inquirer.queryIPv4Address();

			Assertions.assertEquals(testIPv4Address, ipv4Address);
		} catch (IOException e) {
			Exceptions.ignore(e);
		}

		try {
			Inet6Address ipv6Address = inquirer.queryIPv6Address();

			Assertions.assertEquals(testIPv6Address, ipv6Address);
		} catch (IOException e) {
			Exceptions.ignore(e);
		}
	}

}
