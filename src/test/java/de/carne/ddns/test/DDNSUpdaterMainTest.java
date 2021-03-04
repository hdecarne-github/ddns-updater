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
package de.carne.ddns.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import de.carne.ddns.DDNSUpdaterMain;
import de.carne.ddns.test.route53.Route53ClientMockInstance;

/**
 * Test {@linkplain DDNSUpdaterMainTest} class.
 */
class DDNSUpdaterMainTest {

	private static final Route53ClientMockInstance MERGER_MOCK_INSTANCE = new Route53ClientMockInstance();

	@AfterAll
	static void releaseMock() throws Exception {
		MERGER_MOCK_INSTANCE.close();
	}

	@Test
	void testUpdateChange() {
		DDNSUpdaterMain main = new DDNSUpdaterMain();

		main.run(new String[] { "--credentials", "./src/test/resources/credentials.conf", "--host",
				MergerMock.TEST_HOST });
	}

}
