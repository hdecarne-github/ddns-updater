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
package de.carne.ddns.rest;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import de.carne.ddns.Inquirer;
import de.carne.util.Strings;

/**
 * <a href="http://ip6.me/">ip6.me</a> based {@linkplain Inquirer}.
 */
public class IpMeInquirer extends RestInquirer {

	private static final URI IPV4_SSL_URI = URI.create("https://ip4only.me/api/");
	private static final URI IPV6_SSL_URI = URI.create("https://ip6only.me/api/");

	private static final URI IPV4_NOSSL_URI = URI.create("http://ip4only.me/api/");
	private static final URI IPV6_NOSSL_URI = URI.create("http://ip6only.me/api/");

	private static final String TYPE_IPV4 = "IPv4";
	private static final String TYPE_IPV6 = "IPv6";

	private final URI ipv4Uri;
	private final URI ipv6Uri;

	/**
	 * Constructs a new {@linkplain IpMeInquirer} instance.
	 */
	public IpMeInquirer() {
		this(true);
	}

	/**
	 * Constructs a new {@linkplain IpMeInquirer} instance.
	 *
	 * @param ssl whether to use ssl based access or not.
	 */
	public IpMeInquirer(boolean ssl) {
		this.ipv4Uri = (ssl ? IPV4_SSL_URI : IPV4_NOSSL_URI);
		this.ipv6Uri = (ssl ? IPV6_SSL_URI : IPV6_NOSSL_URI);
	}

	@Override
	@Nullable
	protected URI getIPv4Uri() {
		return this.ipv4Uri;
	}

	@Override
	@Nullable
	protected URI getIPv6Uri() {
		return this.ipv6Uri;
	}

	@Override
	protected String decodeIPv4Response(String response) throws IOException {
		return decodeResponse(response, TYPE_IPV4);
	}

	@Override
	protected String decodeIPv6Response(String response) throws IOException {
		return decodeResponse(response, TYPE_IPV6);
	}

	private String decodeResponse(String response, String type) throws IOException {
		@NonNull String[] responseElements = Strings.split(response, ',', true);

		if (responseElements.length < 2) {
			throw new IOException("Unexpected response: '" + response + "'");
		}

		String responseType = responseElements[0];

		if (!type.equals(responseType)) {
			throw new IOException("Unexepcted response type: '" + responseType + "'");
		}

		return responseElements[1];
	}

}
