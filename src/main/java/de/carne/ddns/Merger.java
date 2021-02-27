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

/**
 * Interface for merging DNS records.
 */
public interface Merger {

	/**
	 * Prepares a merge by setting the host name to update.
	 *
	 * @param credentials the credentials to use.
	 * @param host the host to update.
	 * @throws IOException if an I/O error occurs.
	 */
	void prepare(Credentials credentials, String host) throws IOException;

	/**
	 * Merges the IPv4 address for the host set during the initial {@linkplain #prepare(Credentials,String)} call.
	 *
	 * @param address the IPv4 address to merge.
	 * @throws IOException if an I/O error occurs.
	 */
	void mergeInet4Address(@Nullable Inet4Address address) throws IOException;

	/**
	 * Merges the IPv6 address for the host set during the initial {@linkplain #prepare(Credentials,String)} call.
	 *
	 * @param address the IPv6 address to merge.
	 * @throws IOException if an I/O error occurs.
	 */
	void mergeInet6Address(@Nullable Inet6Address address) throws IOException;

	/**
	 * Commits the changes batched up during any call to {@linkplain #mergeInet4Address(Inet4Address)} or
	 * {@linkplain #mergeInet6Address(Inet6Address)}.
	 *
	 * @param pretend whether to really apply the necessary changes or to only log them.
	 * @throws IOException if an I/O error occurs.
	 */
	void commit(boolean pretend) throws IOException;

}
