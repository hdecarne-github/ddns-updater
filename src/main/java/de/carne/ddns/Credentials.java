/*
 * Copyright (c) 2018-2022 Holger de Carne and contributors, All Rights Reserved.
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

import java.util.Optional;

/**
 * Interface providing credential access.
 */
public interface Credentials {

	/**
	 * AWS/Route53 Access Key Id
	 */
	String KEY_ROUTE53_ACCESS_KEY_ID = "route53.accessKeyId";

	/**
	 * AWS/Route53 Secret Access Key
	 */
	String KEY_ROUTE53_SECRET_ACCESS_KEY = "route53.secretAccessKey";

	/**
	 * Gets the credential value for the given key.
	 *
	 * @param key the key to get the credential value for.
	 * @return the found credential value (may be empty).
	 */
	Optional<String> getCredential(String key);

}
