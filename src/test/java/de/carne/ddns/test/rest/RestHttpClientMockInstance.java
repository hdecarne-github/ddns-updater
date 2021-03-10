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
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.regex.Pattern;

import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import de.carne.ddns.Inquirer;
import de.carne.ddns.test.MergerMock;
import de.carne.test.mock.net.http.HttpClientMockInstance;
import de.carne.util.Exceptions;

/**
 * Mock for REST based {@linkplain Inquirer} backends.
 */
public class RestHttpClientMockInstance extends HttpClientMockInstance {

	/**
	 * The test IPv4 address returned by the backend.
	 */
	public static final String TEST_IPV4 = MergerMock.TEST_A_RECORD_NEW;

	/**
	 * The test IPv6 address returned by the backend.
	 */
	public static final String TEST_IPV6 = MergerMock.TEST_AAAA_RECORD_NEW;

	private static final Pattern IPV6TEST_IPV4_URI_PATTERN = Pattern
			.compile("^https?://v4\\.ipv6-test\\.com/api/myip.php");
	private static final Pattern IPV6TEST_IPV6_URI_PATTERN = Pattern
			.compile("^https?://v6\\.ipv6-test\\.com/api/myip.php");

	private static final String IPV6TEST_IPV4_RESPONSE = TEST_IPV4;
	private static final String IPV6TEST_IPV6_RESPONSE = TEST_IPV6;

	private static final Pattern IPME_IPV4_URI_PATTERN = Pattern.compile("^https?\\://ip4only\\.me/api/");
	private static final Pattern IPME_IPV6_URI_PATTERN = Pattern.compile("^https?\\://ip6only\\.me/api/");

	private static final String IPME_IPV4_RESPONSE = "IPv4," + TEST_IPV4
			+ ",Remaining fields reserved for future use,,,";
	private static final String IPME_IPV6_RESPONSE = "IPv6," + TEST_IPV6
			+ ",Remaining fields reserved for future use,,,";

	private static final Pattern IPIFY_IPV4_URI_PATTERN = Pattern.compile("^https?://api\\.ipify\\.org");
	private static final Pattern IPIFY_IPV6_URI_PATTERN = Pattern.compile("^https?://api6\\.ipify\\.org");

	private static final String IPIFY_IPV4_RESPONSE = TEST_IPV4;
	private static final String IPIFY_IPV6_RESPONSE = TEST_IPV6;

	/**
	 * Constructs a new {@linkplain RestHttpClientMockInstance} instance.
	 */
	public RestHttpClientMockInstance() {
		setupMock();
	}

	private void setupMock() {
		try {
			HttpClient httpClient = get();

			@SuppressWarnings("unchecked") HttpResponse<String> ipv6testIPv4Response = Mockito.mock(HttpResponse.class);

			Mockito.when(ipv6testIPv4Response.body()).thenReturn(IPV6TEST_IPV4_RESPONSE);
			Mockito.doReturn(ipv6testIPv4Response).when(httpClient)
					.send(HttpClientMockInstance.requestUriMatches(IPV6TEST_IPV4_URI_PATTERN), ArgumentMatchers.any());

			@SuppressWarnings("unchecked") HttpResponse<String> ipv6testIPv6Response = Mockito.mock(HttpResponse.class);

			Mockito.when(ipv6testIPv6Response.body()).thenReturn(IPV6TEST_IPV6_RESPONSE);
			Mockito.doReturn(ipv6testIPv6Response).when(httpClient)
					.send(HttpClientMockInstance.requestUriMatches(IPV6TEST_IPV6_URI_PATTERN), ArgumentMatchers.any());

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
		} catch (IOException | InterruptedException e) {
			throw Exceptions.toRuntime(e);
		}
	}

}
