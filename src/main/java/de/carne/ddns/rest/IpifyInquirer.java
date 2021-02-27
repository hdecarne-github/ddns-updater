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

import org.eclipse.jdt.annotation.Nullable;

import de.carne.ddns.Inquirer;

/**
 * <a href="https://www.ipify.org/">www.ipify.org</a> based {@linkplain Inquirer}.
 */
public class IpifyInquirer extends RestInquirer {

	private static final URI IPV4_SSL_URI = URI.create("https://api.ipify.org");
	private static final URI IPV6_SSL_URI = URI.create("https://api6.ipify.org");

	private static final URI IPV4_NOSSL_URI = URI.create("http://api.ipify.org");
	private static final URI IPV6_NOSSL_URI = URI.create("http://api6.ipify.org");

	private final boolean ssl;

	/**
	 * Constructs a new {@linkplain IpifyInquirer} instance.
	 */
	public IpifyInquirer() {
		this(true);
	}

	/**
	 * Constructs a new {@linkplain IpifyInquirer} instance.
	 *
	 * @param ssl whether to use ssl based access or not.
	 */
	public IpifyInquirer(boolean ssl) {
		this.ssl = ssl;
	}

	@Override
	@Nullable
	protected URI getIPv4Uri() {
		return (this.ssl ? IPV4_SSL_URI : IPV4_NOSSL_URI);
	}

	@Override
	@Nullable
	protected URI getIPv6Uri() {
		return (this.ssl ? IPV6_SSL_URI : IPV6_NOSSL_URI);
	}

	@Override
	protected String decodeResponse(String response) throws IOException {
		return response;
	}

}
