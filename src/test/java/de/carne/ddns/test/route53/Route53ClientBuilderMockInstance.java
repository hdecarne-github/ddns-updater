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
package de.carne.ddns.test.route53;

import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.carne.test.mock.ScopedMockInstance;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.Route53ClientBuilder;

final class Route53ClientBuilderMockInstance
		extends ScopedMockInstance<MockedStatic<Route53Client>, Route53ClientBuilder> {

	Route53ClientBuilderMockInstance() {
		super(Route53ClientBuilderMockInstance::initialize, Mockito.spy(Route53Client.builder()));
	}

	private static MockedStatic<Route53Client> initialize(Route53ClientBuilder instance) {
		MockedStatic<Route53Client> mock = Mockito.mockStatic(Route53Client.class);

		mock.when(Route53Client::builder).thenReturn(instance);
		return mock;
	}

}
