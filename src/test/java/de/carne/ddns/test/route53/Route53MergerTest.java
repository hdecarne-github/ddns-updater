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

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.carne.ddns.route53.Route53Merger;
import de.carne.ddns.test.DummyCredentials;
import de.carne.ddns.test.MergerMock;

/**
 * Test {@linkplain Route53Merger} class.
 */
class Route53MergerTest {

	private static final Route53ClientMockInstance MOCK_INSTANCE = new Route53ClientMockInstance();

	@AfterAll
	static void releaseMock() throws Exception {
		MOCK_INSTANCE.close();
	}

	@Test
	void testUpdatePretend() throws IOException {
		MOCK_INSTANCE.setARecord(MergerMock.TEST_A_RECORD_OLD);
		MOCK_INSTANCE.setAAAARecord(MergerMock.TEST_AAAA_RECORD_OLD);

		Route53Merger merger = new Route53Merger();

		merger.prepare(new DummyCredentials(), MergerMock.TEST_HOST);
		merger.mergeIPv4Address((Inet4Address) InetAddress.getByName(MergerMock.TEST_A_RECORD_NEW));
		merger.mergeIPv6Address((Inet6Address) InetAddress.getByName(MergerMock.TEST_AAAA_RECORD_NEW));
		merger.commit(true);

		Assertions.assertEquals(MergerMock.TEST_A_RECORD_OLD, MOCK_INSTANCE.getARecord());
		Assertions.assertEquals(MergerMock.TEST_AAAA_RECORD_OLD, MOCK_INSTANCE.getAAAARecord());
	}

	@Test
	void testUpdateChange() throws IOException {
		MOCK_INSTANCE.setARecord(MergerMock.TEST_A_RECORD_OLD);
		MOCK_INSTANCE.setAAAARecord(MergerMock.TEST_AAAA_RECORD_OLD);

		Route53Merger merger = new Route53Merger();

		merger.prepare(new DummyCredentials(), MergerMock.TEST_HOST);
		merger.mergeIPv4Address((Inet4Address) InetAddress.getByName(MergerMock.TEST_A_RECORD_NEW));
		merger.mergeIPv6Address((Inet6Address) InetAddress.getByName(MergerMock.TEST_AAAA_RECORD_NEW));
		merger.commit(false);

		Assertions.assertEquals(MergerMock.TEST_A_RECORD_NEW, MOCK_INSTANCE.getARecord());
		Assertions.assertEquals(MergerMock.TEST_AAAA_RECORD_NEW, MOCK_INSTANCE.getAAAARecord());
	}

	@Test
	void testUpdateChangeNoIPv4() throws IOException {
		MOCK_INSTANCE.setARecord(null);
		MOCK_INSTANCE.setAAAARecord(MergerMock.TEST_AAAA_RECORD_OLD);

		Route53Merger merger = new Route53Merger();

		merger.prepare(new DummyCredentials(), MergerMock.TEST_HOST);
		merger.mergeIPv4Address((Inet4Address) InetAddress.getByName(MergerMock.TEST_A_RECORD_NEW));
		merger.mergeIPv6Address((Inet6Address) InetAddress.getByName(MergerMock.TEST_AAAA_RECORD_NEW));
		merger.commit(false);

		Assertions.assertNull(MOCK_INSTANCE.getARecord());
		Assertions.assertEquals(MergerMock.TEST_AAAA_RECORD_NEW, MOCK_INSTANCE.getAAAARecord());
	}

	@Test
	void testUpdateChangeNoIPv6() throws IOException {
		MOCK_INSTANCE.setARecord(MergerMock.TEST_A_RECORD_OLD);
		MOCK_INSTANCE.setAAAARecord(null);

		Route53Merger merger = new Route53Merger();

		merger.prepare(new DummyCredentials(), MergerMock.TEST_HOST);
		merger.mergeIPv4Address((Inet4Address) InetAddress.getByName(MergerMock.TEST_A_RECORD_NEW));
		merger.mergeIPv6Address((Inet6Address) InetAddress.getByName(MergerMock.TEST_AAAA_RECORD_NEW));
		merger.commit(false);

		Assertions.assertEquals(MergerMock.TEST_A_RECORD_NEW, MOCK_INSTANCE.getARecord());
		Assertions.assertNull(MOCK_INSTANCE.getAAAARecord());
	}

	@Test
	void testUpdateChangeNoRecords() throws IOException {
		MOCK_INSTANCE.setARecord(null);
		MOCK_INSTANCE.setAAAARecord(null);

		Route53Merger merger = new Route53Merger();

		merger.prepare(new DummyCredentials(), MergerMock.TEST_HOST);
		merger.mergeIPv4Address((Inet4Address) InetAddress.getByName(MergerMock.TEST_A_RECORD_NEW));
		merger.mergeIPv6Address((Inet6Address) InetAddress.getByName(MergerMock.TEST_AAAA_RECORD_NEW));
		merger.commit(false);

		Assertions.assertNull(MOCK_INSTANCE.getARecord());
		Assertions.assertNull(MOCK_INSTANCE.getAAAARecord());
	}

	@Test
	void testUpdateChangeNoUpdated() throws IOException {
		MOCK_INSTANCE.setARecord(MergerMock.TEST_A_RECORD_OLD);
		MOCK_INSTANCE.setAAAARecord(MergerMock.TEST_AAAA_RECORD_OLD);

		Route53Merger merger = new Route53Merger();

		merger.prepare(new DummyCredentials(), MergerMock.TEST_HOST);
		merger.commit(false);

		Assertions.assertEquals(MergerMock.TEST_A_RECORD_OLD, MOCK_INSTANCE.getARecord());
		Assertions.assertEquals(MergerMock.TEST_AAAA_RECORD_OLD, MOCK_INSTANCE.getAAAARecord());
	}

}
