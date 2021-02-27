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
package de.carne.ddns.util;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.ddns.Inquirer;
import de.carne.ddns.rest.IpMeInquirer;
import de.carne.ddns.rest.IpifyInquirer;
import de.carne.util.logging.Log;

/**
 * {@linkplain Inquirer} implementation invoking a whole set of {@linkplain Inquirer} instances to get an address.
 */
public class CombinedInquirer implements Inquirer {

	private static final Log LOG = new Log();

	private final Inquirer[] inquirers;

	/**
	 * Constructs a new {@linkplain CombinedInquirer} instance.
	 */
	public CombinedInquirer() {
		this(new IpifyInquirer(true), new IpMeInquirer(true), new IpifyInquirer(false), new IpMeInquirer(false));
	}

	/**
	 * Constructs a new {@linkplain CombinedInquirer} instance.
	 *
	 * @param inquirers the set of {@linkplain Inquirer} instances to use.
	 */
	public CombinedInquirer(Inquirer... inquirers) {
		this.inquirers = inquirers;
	}

	@Override
	@Nullable
	public Inet4Address queryIPv4Address() throws IOException {
		Inet4Address address = null;
		IOException exception = null;

		for (Inquirer inquirer : this.inquirers) {
			if (address != null) {
				break;
			}
			try {
				LOG.debug("Invoking Inquirer {0}...", inquirer.getClass().getSimpleName());

				address = inquirer.queryIPv4Address();

				LOG.debug("Inquirer {0} succeeded", inquirer.getClass().getSimpleName());
			} catch (IOException e) {
				LOG.warning(e, "Inquirer {0} failed", inquirer.getClass().getSimpleName());

				if (exception == null) {
					exception = e;
				} else {
					exception.addSuppressed(exception);
				}
			}
		}
		if (address == null && exception != null) {
			throw exception;
		}
		return address;
	}

	@Override
	@Nullable
	public Inet6Address queryIPv6Address() throws IOException {
		Inet6Address address = null;
		IOException exception = null;

		for (Inquirer inquirer : this.inquirers) {
			if (address != null) {
				break;
			}
			try {
				LOG.debug("Invoking Inquirer {0}...", inquirer.getClass().getSimpleName());

				address = inquirer.queryIPv6Address();

				LOG.debug("Inquirer {0} succeeded", inquirer.getClass().getSimpleName());
			} catch (IOException e) {
				LOG.warning(e, "Inquirer {0} failed", inquirer.getClass().getSimpleName());

				if (exception == null) {
					exception = e;
				} else {
					exception.addSuppressed(e);
				}
			}
		}
		if (address == null && exception != null) {
			throw exception;
		}
		return address;
	}

}
