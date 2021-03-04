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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.mockito.ArgumentMatchers;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import de.carne.ddns.test.MergerMock;
import de.carne.test.mock.ScopedMockInstance;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.Route53ClientBuilder;
import software.amazon.awssdk.services.route53.model.Change;
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
 * Mock for Route53 DNS service.
 */
public final class Route53ClientMockInstance
		extends ScopedMockInstance<MockedStatic<Route53Client>, Route53ClientBuilder> implements MergerMock {

	private static final String TEST_ZONE_ID = "1234";
	private static final String TEST_ZONE_NAME = TEST_DOMAIN + ".";
	private static final String TEST_RECORD_NAME = TEST_HOST + ".";

	@Nullable
	private String aRecord = TEST_A_RECORD_OLD;
	@Nullable
	private String aaaaRecord = TEST_AAAA_RECORD_OLD;

	/**
	 * Constructs a new {@linkplain Route53ClientMockInstance} instance.
	 */
	public Route53ClientMockInstance() {
		super(Route53ClientMockInstance::initialize, Mockito.spy(Route53Client.builder()));
		setupMock();
	}

	private static MockedStatic<Route53Client> initialize(Route53ClientBuilder instance) {
		MockedStatic<Route53Client> mock = Mockito.mockStatic(Route53Client.class);

		mock.when(Route53Client::builder).thenReturn(instance);
		return mock;
	}

	private void setupMock() {
		Route53Client client = Mockito.mock(Route53Client.class);

		Mockito.when(client.listHostedZones()).thenReturn(buildListHostedZonesResponse());

		Mockito.when(client.listResourceRecordSets((ListResourceRecordSetsRequest) ArgumentMatchers.any()))
				.thenAnswer(invocation -> buildListResourceRecordSetsResponse());

		Mockito.when(client.changeResourceRecordSets((ChangeResourceRecordSetsRequest) ArgumentMatchers.any()))
				.thenAnswer(invocation -> buildChangeResourceRecordSetsResponse(
						(ChangeResourceRecordSetsRequest) Objects.requireNonNull(invocation.getArguments()[0])));

		Route53ClientBuilder builder = get();

		Mockito.doReturn(client).when(builder).build();
	}

	private ListHostedZonesResponse buildListHostedZonesResponse() {
		return ListHostedZonesResponse.builder()
				.hostedZones(HostedZone.builder().id(TEST_ZONE_ID).name(TEST_ZONE_NAME).build())
				.isTruncated(Boolean.FALSE).build();
	}

	private ListResourceRecordSetsResponse buildListResourceRecordSetsResponse() {
		ListResourceRecordSetsResponse.Builder builder = ListResourceRecordSetsResponse.builder();
		List<ResourceRecordSet> resourceRecordSets = new ArrayList<>();

		if (this.aRecord != null) {
			resourceRecordSets.add(ResourceRecordSet.builder().type(RRType.A).name(TEST_RECORD_NAME)
					.resourceRecords(ResourceRecord.builder().value(this.aRecord).build()).build());
		}
		if (this.aaaaRecord != null) {
			resourceRecordSets.add(ResourceRecordSet.builder().type(RRType.AAAA).name(TEST_RECORD_NAME)
					.resourceRecords(ResourceRecord.builder().value(this.aaaaRecord).build()).build());
		}
		builder.resourceRecordSets(resourceRecordSets);
		return builder.isTruncated(Boolean.FALSE).build();
	}

	private ChangeResourceRecordSetsResponse buildChangeResourceRecordSetsResponse(
			ChangeResourceRecordSetsRequest request) {
		for (Change change : request.changeBatch().changes()) {
			ResourceRecordSet rrs = change.resourceRecordSet();

			switch (rrs.type()) {
			case A:
				this.aRecord = rrs.resourceRecords().iterator().next().value();
				break;
			case AAAA:
				this.aaaaRecord = rrs.resourceRecords().iterator().next().value();
				break;
			default:
				// nothing to do here
			}
		}
		return ChangeResourceRecordSetsResponse.builder()
				.changeInfo(ChangeInfo.builder().id(TEST_ZONE_ID).status(ChangeStatus.PENDING).build()).build();
	}

	@Override
	public void setARecord(@Nullable String aRecord) {
		this.aRecord = aRecord;
	}

	@Override
	@Nullable
	public String getARecord() {
		return this.aRecord;
	}

	@Override
	public void setAAAARecord(@Nullable String aaaaRecord) {
		this.aaaaRecord = aaaaRecord;
	}

	@Override
	@Nullable
	public String getAAAARecord() {
		return this.aaaaRecord;
	}

}
