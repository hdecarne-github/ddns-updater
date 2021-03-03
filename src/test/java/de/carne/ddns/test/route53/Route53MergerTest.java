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
import java.net.UnknownHostException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import de.carne.ddns.route53.Route53Merger;
import de.carne.ddns.test.DummyCredentials;
import de.carne.util.Exceptions;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.Route53ClientBuilder;
import software.amazon.awssdk.services.route53.model.ChangeInfo;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.ChangeStatus;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.ListHostedZonesResponse;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

/**
 * Test {@linkplain Route53Merger} class.
 */
class Route53MergerTest {

	private static final Route53ClientBuilderMockInstance MOCK_INSTANCE = new Route53ClientBuilderMockInstance();

	private static final String TEST_ZONE_ID = "1234";
	private static final String TEST_ZONE_NAME = "domain.tld.";
	private static final String TEST_RECORD_NAME = "test." + TEST_ZONE_NAME;
	private static final String TEST_HOST = "test.domain.tld";

	private static final Inet4Address TEST_IPV4;
	private static final Inet6Address TEST_IPV6;

	static {
		try {
			TEST_IPV4 = (Inet4Address) InetAddress.getByName("1.2.3.4");
			TEST_IPV6 = (Inet6Address) InetAddress.getByName("1:2:3:4:c:d:e:f");
		} catch (UnknownHostException e) {
			throw Exceptions.toRuntime(e);
		}
	}

	@BeforeAll
	static void setupMock() throws Exception {
		Route53Client client = Mockito.mock(Route53Client.class);

		Mockito.when(client.listHostedZones())
				.thenReturn(ListHostedZonesResponse.builder()
						.hostedZones(HostedZone.builder().id(TEST_ZONE_ID).name(TEST_ZONE_NAME).build())
						.isTruncated(Boolean.FALSE).build());

		Mockito.when(client.listResourceRecordSets((ListResourceRecordSetsRequest) ArgumentMatchers.any()))
				.thenReturn(
						ListResourceRecordSetsResponse.builder()
								.resourceRecordSets(
										ResourceRecordSet.builder().type(RRType.A).name(TEST_RECORD_NAME)
												.resourceRecords(ResourceRecord.builder()
														.value(TEST_IPV4.getHostAddress()).build())
												.build(),
										ResourceRecordSet.builder().type(RRType.AAAA).name(TEST_RECORD_NAME)
												.resourceRecords(ResourceRecord.builder()
														.value(TEST_IPV6.getHostAddress()).build())
												.build())
								.isTruncated(Boolean.FALSE).build());

		Mockito.when(client.changeResourceRecordSets((ChangeResourceRecordSetsRequest) ArgumentMatchers.any()))
				.thenReturn(ChangeResourceRecordSetsResponse.builder()
						.changeInfo(ChangeInfo.builder().id(TEST_ZONE_ID).status(ChangeStatus.PENDING).build())
						.build());

		Route53ClientBuilder builder = MOCK_INSTANCE.get();

		Mockito.doReturn(client).when(builder).build();
	}

	@AfterAll
	static void releaseMock() throws Exception {
		MOCK_INSTANCE.close();
	}

	@Test
	void testUpdateChange() throws IOException {
		Route53Merger merger = new Route53Merger();

		merger.prepare(new DummyCredentials(), TEST_HOST);
		merger.mergeIPv4Address(TEST_IPV4);
		merger.mergeIPv6Address(TEST_IPV6);
		merger.commit(false);

		Route53Client client = getClientMock();

		Mockito.verify(client).changeResourceRecordSets((ChangeResourceRecordSetsRequest) ArgumentMatchers.any());
	}

	private Route53Client getClientMock() {
		return MOCK_INSTANCE.get().build();
	}

}
