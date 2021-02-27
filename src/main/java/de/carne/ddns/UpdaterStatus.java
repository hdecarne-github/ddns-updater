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
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.util.Strings;
import de.carne.util.logging.Log;

final class UpdaterStatus {

	private static final Log LOG = new Log();

	private static final String KEY_LASTUPDATETS = ".lastUpdate";
	private static final String KEY_INET4ADDRESS = ".inet4Adress";
	private static final String KEY_INET6ADDRESS = ".inet6Adress";

	private final Preferences preferences;
	private final String host;
	private long lastUpdateTS;
	@Nullable
	private String updateInet4Address = null;
	@Nullable
	private String updateInet6Address = null;

	public UpdaterStatus(String host) {
		this.preferences = Preferences.userNodeForPackage(UpdaterStatus.class);
		this.host = host;
		this.lastUpdateTS = this.preferences.getLong(this.host + KEY_LASTUPDATETS, 0l);
	}

	public boolean isUpdateRequired(@Nullable Inet4Address currentInet4Address,
			@Nullable Inet6Address currentInet6Address, long forceTimeout) {
		boolean updateRequired = false;

		if ((System.currentTimeMillis() - this.lastUpdateTS) >= forceTimeout) {
			LOG.notice("Last update is outdated; forcing update");

			updateRequired = true;
		}
		if (currentInet4Address != null) {
			this.updateInet4Address = currentInet4Address.getHostAddress();

			String lastUpdateInet4Address = Strings.safe(this.preferences.get(this.host + KEY_INET4ADDRESS, null));

			if (!lastUpdateInet4Address.equals(this.updateInet4Address)) {
				LOG.notice("IPv4 has changed; triggering update");

				updateRequired = true;
			}
		}
		if (currentInet6Address != null) {
			this.updateInet6Address = currentInet6Address.getHostAddress();

			String lastUpdateInet6Address = Strings.safe(this.preferences.get(this.host + KEY_INET6ADDRESS, null));

			if (!lastUpdateInet6Address.equals(this.updateInet6Address)) {
				LOG.notice("IPv6 has changed; triggering update");

				updateRequired = true;
			}
		}
		if (!updateRequired) {
			LOG.notice("DNS up-to-date; no update required");
		}
		return updateRequired;
	}

	public void updateAndFlush() throws IOException {
		this.lastUpdateTS = System.currentTimeMillis();
		this.preferences.putLong(this.host + KEY_LASTUPDATETS, this.lastUpdateTS);
		if (this.updateInet4Address != null) {
			this.preferences.put(this.host + KEY_INET4ADDRESS, this.updateInet4Address);
		}
		if (this.updateInet6Address != null) {
			this.preferences.put(this.host + KEY_INET6ADDRESS, this.updateInet6Address);
		}
		try {
			this.preferences.flush();
		} catch (BackingStoreException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

}
