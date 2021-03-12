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
package de.carne.ddns.test.rest;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.ddns.Inquirer;
import de.carne.ddns.rest.IpMeInquirer;
import de.carne.ddns.rest.IpifyInquirer;
import de.carne.ddns.rest.Ipv6testInquirer;
import de.carne.ddns.util.CombinedInquirer;
import de.carne.util.Exceptions;

/**
 * Test {@linkplain Inquirer} implementations.
 */
class RestInquirerTest {

	private static final RestHttpClientMockInstance REST_HTTP_CLIENT_MOCK_INSTANCE = new RestHttpClientMockInstance();

	@AfterAll
	static void releaseMock() {
		REST_HTTP_CLIENT_MOCK_INSTANCE.close();
	}

	@Test
	void testCombinedInquirer() {
		Inquirer inquirer = new CombinedInquirer();

		testInquirer(inquirer);
	}

	@Test
	void testIpv6testInquirerDefault() {
		Inquirer inquirer = new Ipv6testInquirer();

		testInquirer(inquirer);
	}

	@Test
	void testIpMeInquirerDefault() {
		Inquirer inquirer = new IpMeInquirer();

		testInquirer(inquirer);
	}

	@Test
	void testIpifyInquirerDefault() {
		Inquirer inquirer = new IpifyInquirer();

		testInquirer(inquirer);
	}

	private void testInquirer(Inquirer inquirer) {
		try {
			Inet4Address ipv4Address = inquirer.queryIPv4Address();

			Assertions.assertNotNull(ipv4Address);
			Assertions.assertEquals(RestHttpClientMockInstance.TEST_IPV4, ipv4Address.getHostAddress());
		} catch (IOException e) {
			Exceptions.ignore(e);
		}

		try {
			Inet6Address ipv6Address = inquirer.queryIPv6Address();

			Assertions.assertNotNull(ipv6Address);
			Assertions.assertEquals(RestHttpClientMockInstance.TEST_IPV6, ipv6Address.getHostAddress());
		} catch (IOException e) {
			Exceptions.ignore(e);
		}
	}

}
