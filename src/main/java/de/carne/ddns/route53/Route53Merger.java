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
package de.carne.ddns.route53;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.Nullable;

import de.carne.ddns.Credentials;
import de.carne.ddns.Merger;
import de.carne.util.logging.Log;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.Change;
import software.amazon.awssdk.services.route53.model.ChangeAction;
import software.amazon.awssdk.services.route53.model.ChangeBatch;
import software.amazon.awssdk.services.route53.model.ChangeInfo;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ChangeResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.HostedZone;
import software.amazon.awssdk.services.route53.model.ListHostedZonesRequest;
import software.amazon.awssdk.services.route53.model.ListHostedZonesResponse;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsResponse;
import software.amazon.awssdk.services.route53.model.RRType;
import software.amazon.awssdk.services.route53.model.ResourceRecord;
import software.amazon.awssdk.services.route53.model.ResourceRecordSet;

/**
 * AWS/Route53 based {@linkplain Merger}.
 */
public class Route53Merger implements Merger {

	private static final Log LOG = new Log();

	@Nullable
	private Credentials credentials = null;
	@Nullable
	private String recordName = null;
	@Nullable
	private Inet4Address inet4Address = null;
	@Nullable
	private Inet6Address inet6Address = null;

	@Override
	public void prepare(@SuppressWarnings("hiding") Credentials credentials, String host) throws IOException {
		this.credentials = credentials;
		this.recordName = host + ".";
	}

	@Override
	public void mergeIPv4Address(@Nullable Inet4Address address) throws IOException {
		this.inet4Address = address;
	}

	@Override
	public void mergeIPv6Address(@Nullable Inet6Address address) throws IOException {
		this.inet6Address = address;
	}

	@Override
	public void commit(boolean pretend) throws IOException {
		if (this.inet4Address != null || this.inet6Address != null) {
			String checkedRecordName = this.recordName;

			if (checkedRecordName == null) {
				throw new IllegalArgumentException("Host not set (prepare has to be called prior to update)");
			}
			try (Route53Client client = buildClient()) {
				HostedZone zone = lookupZone(client, checkedRecordName);
				Map<RRType, ResourceRecordSet> resourceRecordSets = lookupResourceRecordSets(client, zone,
						checkedRecordName);
				List<Change> changes = new LinkedList<>();

				prepareChange(changes, resourceRecordSets, RRType.A, this.inet4Address);
				prepareChange(changes, resourceRecordSets, RRType.AAAA, this.inet6Address);
				if (!changes.isEmpty()) {
					updateRecordSets(client, zone, changes, pretend);
				}
			} catch (SdkException e) {
				throw new IOException("Route53 access failed", e);
			}
		}
	}

	private void prepareChange(List<Change> changes, Map<RRType, ResourceRecordSet> currentRRSets, RRType type,
			@Nullable InetAddress address) {
		ResourceRecordSet currentRRSet = currentRRSets.get(type);

		if (currentRRSet != null) {
			if (address != null) {
				ResourceRecord changeRR = ResourceRecord.builder().value(address.getHostAddress()).build();
				ResourceRecordSet changeRRSet = ResourceRecordSet.builder().type(currentRRSet.type())
						.name(currentRRSet.name()).ttl(currentRRSet.ttl()).resourceRecords(changeRR).build();
				Change change = Change.builder().action(ChangeAction.UPSERT).resourceRecordSet(changeRRSet).build();

				changes.add(change);
			} else {
				LOG.warning("Ignoring {0} record update due to missing update", type);
			}
		} else if (address != null) {
			LOG.warning("Ignoring {0} record update due to missing current record", type);
		}
	}

	private Route53Client buildClient() {
		Credentials checkedCredentials = this.credentials;
		Optional<String> accessKeyIdCredential = Optional.empty();
		Optional<String> secretAccessKeyCredential = Optional.empty();

		if (checkedCredentials != null) {
			accessKeyIdCredential = checkedCredentials.getCredential(Credentials.KEY_ROUTE53_ACCESS_KEY_ID);
			secretAccessKeyCredential = checkedCredentials.getCredential(Credentials.KEY_ROUTE53_SECRET_ACCESS_KEY);
		}

		String accessKeyId = accessKeyIdCredential
				.orElseThrow(() -> missingCredential(Credentials.KEY_ROUTE53_ACCESS_KEY_ID));
		String secretAccessKey = secretAccessKeyCredential
				.orElseThrow(() -> missingCredential(Credentials.KEY_ROUTE53_SECRET_ACCESS_KEY));
		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider
				.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));

		return Route53Client.builder().region(Region.AWS_GLOBAL).credentialsProvider(credentialsProvider).build();
	}

	private IllegalArgumentException missingCredential(String credentialKey) {
		throw new IllegalArgumentException("Missing credential: " + credentialKey);
	}

	private HostedZone lookupZone(Route53Client client, String host) throws IOException {
		LOG.info("Looking up zone for host ''{0}''...", host);

		ListHostedZonesResponse listResponse = null;
		HostedZone zone = null;

		do {
			if (listResponse == null) {
				listResponse = client.listHostedZones();
			} else {
				ListHostedZonesRequest listRequest = ListHostedZonesRequest.builder().marker(listResponse.nextMarker())
						.build();

				listResponse = client.listHostedZones(listRequest);
			}

			Optional<HostedZone> foundZone = listResponse.hostedZones().stream()
					.filter(listedZone -> host.endsWith("." + listedZone.name())).findFirst();

			if (foundZone.isPresent()) {
				zone = foundZone.get();
			}
		} while (zone == null && listResponse.isTruncated().booleanValue());
		if (zone == null) {
			throw new FileNotFoundException("No zone found for host: " + host);
		}

		LOG.info("Found matching zone ''{0}''", zone.name());

		return zone;
	}

	private Map<RRType, ResourceRecordSet> lookupResourceRecordSets(Route53Client client, HostedZone zone,
			String host) {
		LOG.info("Looking up record sets for host ''{0}''...", host);

		ListResourceRecordSetsRequest listRequest = ListResourceRecordSetsRequest.builder().hostedZoneId(zone.id())
				.startRecordName(host).build();
		ListResourceRecordSetsResponse listResponse = client.listResourceRecordSets(listRequest);

		Map<RRType, ResourceRecordSet> resourceRecordSets = new EnumMap<>(RRType.class);

		for (ResourceRecordSet resourceRecordSet : listResponse.resourceRecordSets()) {
			String name = resourceRecordSet.name();
			RRType type = resourceRecordSet.type();

			if (host.equals(name) && (type == RRType.A || type == RRType.AAAA)) {
				List<ResourceRecord> resourceRecords = resourceRecordSet.resourceRecords();
				int resourceRecordsSize = resourceRecords.size();

				if (resourceRecordsSize == 1) {
					String value = resourceRecords.get(0).value();

					LOG.info(" {0} {1} {2}", name, type, value);

					resourceRecordSets.put(type, resourceRecordSet);
				} else {
					LOG.warning("Ignoring non-singular record set {0} {1} ({2} entries)", name, type,
							resourceRecordsSize);
				}
			}
		}

		LOG.info("{1} record set(s) found for host ''{0}''...", host, resourceRecordSets.size());

		return resourceRecordSets;
	}

	private void updateRecordSets(Route53Client client, HostedZone zone, List<Change> changes, boolean pretend) {
		LOG.notice("Updating {1} record set(s) for zone ''{0}''...", zone.name(), changes.size());

		for (Change change : changes) {
			ResourceRecordSet changeRRSet = change.resourceRecordSet();

			LOG.notice(" {0} {1} {2}", changeRRSet.name(), changeRRSet.type(),
					changeRRSet.resourceRecords().get(0).value());
		}

		if (!pretend) {
			ChangeBatch changeBatch = ChangeBatch.builder().changes(changes).build();
			ChangeResourceRecordSetsRequest changeRequest = ChangeResourceRecordSetsRequest.builder()
					.hostedZoneId(zone.id()).changeBatch(changeBatch).build();

			ChangeResourceRecordSetsResponse changeResponse = client.changeResourceRecordSets(changeRequest);
			ChangeInfo changeInfo = changeResponse.changeInfo();

			LOG.notice("Update(s) applied (change id/status: {0}/{1})", changeInfo.id(), changeInfo.statusAsString());
		} else {
			LOG.notice("Updates not applied while in pretend mode");
		}
	}

}
