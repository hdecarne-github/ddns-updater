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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.ddns.Inquirer;
import de.carne.util.Check;
import de.carne.util.Strings;
import de.carne.util.logging.Log;

abstract class RestInquirer implements Inquirer {

	private static final Log LOG = new Log();

	// Does not have to be 100% exact, but should be sufficient to keep unexpected input out
	private static final Pattern PATTERN_IPV4 = Pattern.compile("^([0-9]{1,3}\\.){3}[0-9]{1,3}$");
	private static final Pattern PATTERN_IPV6 = Pattern.compile("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$");

	@Override
	@Nullable
	public Inet4Address queryIPv4Address() throws IOException {
		Inet4Address address = null;
		URI uri = getIPv4Uri();

		if (uri != null) {
			String response = queryUri(uri);
			String decoded = decodeIPv4Response(response);

			if (!PATTERN_IPV4.matcher(decoded).matches()) {
				throw new IOException("Invalid IPv4 address: '" + decoded + "'");
			}
			address = Check.isInstanceOf(getAddress(decoded), Inet4Address.class);
		}
		return address;
	}

	@Override
	@Nullable
	public Inet6Address queryIPv6Address() throws IOException {
		Inet6Address address = null;
		URI uri = getIPv6Uri();

		if (uri != null) {
			String response = queryUri(uri);
			String decoded = decodeIPv6Response(response);

			if (!PATTERN_IPV6.matcher(decoded).matches()) {
				throw new IOException("Invalid IPv6 address: '" + decoded + "'");
			}
			address = Check.isInstanceOf(getAddress(decoded), Inet6Address.class);
		}
		return address;
	}

	@Nullable
	protected abstract URI getIPv4Uri();

	@Nullable
	protected abstract URI getIPv6Uri();

	protected String decodeIPv4Response(String response) throws IOException {
		return decodeResponse(response);
	}

	protected String decodeIPv6Response(String response) throws IOException {
		return decodeResponse(response);
	}

	@SuppressWarnings({ "unused", "java:S1130" })
	protected String decodeResponse(String response) throws IOException {
		return response;
	}

	private String queryUri(URI uri) throws IOException {
		LOG.info("Inquiring URI {0}...", uri);

		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder(uri).build();
		String response;

		try {
			response = Strings.safe(client.send(request, BodyHandlers.ofString()).body());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IOException("Inquiry request was interrupted", e);
		}

		if (LOG.isDebugLoggable()) {
			LOG.debug("Received response: ''{0}''", Strings.encode(response));
		}
		return response;
	}

	private InetAddress getAddress(String addressString) throws IOException {
		InetAddress address;

		try {
			address = InetAddress.getByName(addressString);
		} catch (UnknownHostException e) {
			throw new IOException("Invalid address: '" + addressString + "'", e);
		}

		LOG.info("Inquiry result: {0}", address.getHostAddress());

		return address;
	}

}
