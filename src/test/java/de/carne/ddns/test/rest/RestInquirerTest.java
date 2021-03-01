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
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import de.carne.ddns.Inquirer;
import de.carne.ddns.rest.IpMeInquirer;
import de.carne.ddns.rest.IpifyInquirer;
import de.carne.ddns.util.CombinedInquirer;
import de.carne.test.mock.net.http.HttpClientMockInstance;
import de.carne.util.Exceptions;

/**
 * Test {@linkplain Inquirer} implementations.
 */
class RestInquirerTest {

	private static final HttpClientMockInstance HTTP_CLIENT_MOCK_INSTANCE = new HttpClientMockInstance();

	private static final String TEST_IPV4 = "1.2.3.4";
	private static final String TEST_IPV6 = "1:2:3:4:c:d:e:f";

	private static final Pattern IPME_IPV4_URI_PATTERN = Pattern.compile("^https?\\://ip4only\\.me/api/");
	private static final Pattern IPME_IPV6_URI_PATTERN = Pattern.compile("^https?\\://ip6only\\.me/api/");

	private static final String IPME_IPV4_RESPONSE = "IPv4,1.2.3.4,Remaining fields reserved for future use,,,";
	private static final String IPME_IPV6_RESPONSE = "IPv6,1:2:3:4:c:d:e:f,Remaining fields reserved for future use,,,";

	private static final Pattern IPIFY_IPV4_URI_PATTERN = Pattern.compile("^https?://api\\.ipify\\.org");
	private static final Pattern IPIFY_IPV6_URI_PATTERN = Pattern.compile("^https?://api6\\.ipify\\.org");

	private static final String IPIFY_IPV4_RESPONSE = TEST_IPV4;
	private static final String IPIFY_IPV6_RESPONSE = TEST_IPV6;

	@BeforeAll
	static void setupMock() throws IOException, InterruptedException {
		HttpClient httpClient = HTTP_CLIENT_MOCK_INSTANCE.get();

		@SuppressWarnings("unchecked") HttpResponse<String> ipmeIPv4Response = Mockito.mock(HttpResponse.class);

		Mockito.when(ipmeIPv4Response.body()).thenReturn(IPME_IPV4_RESPONSE);
		Mockito.doReturn(ipmeIPv4Response).when(httpClient)
				.send(HttpClientMockInstance.requestUriMatches(IPME_IPV4_URI_PATTERN), ArgumentMatchers.any());

		@SuppressWarnings("unchecked") HttpResponse<String> ipmeIPv6Response = Mockito.mock(HttpResponse.class);

		Mockito.when(ipmeIPv6Response.body()).thenReturn(IPME_IPV6_RESPONSE);
		Mockito.doReturn(ipmeIPv6Response).when(httpClient)
				.send(HttpClientMockInstance.requestUriMatches(IPME_IPV6_URI_PATTERN), ArgumentMatchers.any());

		@SuppressWarnings("unchecked") HttpResponse<String> ipifyIPv4Response = Mockito.mock(HttpResponse.class);

		Mockito.when(ipifyIPv4Response.body()).thenReturn(IPIFY_IPV4_RESPONSE);
		Mockito.doReturn(ipifyIPv4Response).when(httpClient)
				.send(HttpClientMockInstance.requestUriMatches(IPIFY_IPV4_URI_PATTERN), ArgumentMatchers.any());

		@SuppressWarnings("unchecked") HttpResponse<String> ipifyIPv6Response = Mockito.mock(HttpResponse.class);

		Mockito.when(ipifyIPv6Response.body()).thenReturn(IPIFY_IPV6_RESPONSE);
		Mockito.doReturn(ipifyIPv6Response).when(httpClient)
				.send(HttpClientMockInstance.requestUriMatches(IPIFY_IPV6_URI_PATTERN), ArgumentMatchers.any());
	}

	@AfterAll
	static void release() {
		HTTP_CLIENT_MOCK_INSTANCE.close();
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

			Assertions.assertNotNull(ipv4Address);
			Assertions.assertEquals(TEST_IPV4, ipv4Address.getHostAddress());
		} catch (IOException e) {
			Exceptions.ignore(e);
		}

		try {
			Inet6Address ipv6Address = inquirer.queryIPv6Address();

			Assertions.assertNotNull(ipv6Address);
			Assertions.assertEquals(TEST_IPV6, ipv6Address.getHostAddress());
		} catch (IOException e) {
			Exceptions.ignore(e);
		}
	}

}
