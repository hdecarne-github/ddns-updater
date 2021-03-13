## About DDNS Updater
ddns-updater is a command line tool to update an [AWS/Route53](https://aws.amazon.com/de/route53/) zone with the current IP addresses (IPv4 and/or IPv6) of the running host.
Depending on the actual command line the tool:
1. Determines the IP addresses of the running host.
2. Updates the given host in the AWS/Route53 zone if it is no longer up-to-date. Every 24h an update is forced automatically. 

#### Installation & usage:
A Java SE 11 Runtime Environment is required to run ddns-updater.
Download the latest version from the project's [releases page](https://github.com/hdecarne/ddns-updater/releases/latest) and simply extract it to a folder of your choice.
The archive contains a single executable Jar as well as a folder with the license information. Invoke the application via the command

```
java -jar ddns-updater-boot-<version>.jar [command line arguments]
```

The application command line supports the following options:
```
ddns-updater-boot-<version> [--verbose|--debug] [--quiet] [--credentials file] [--noipv4] [--noipv6] [--force] --host host

--verbose
	Enable verbose logging.
--debug
	Enable debug logging.
--quiet
	Only emit messages in case of warnings or failure
--credentials file
	Use <file> to read credentials (see below).
	Defaults to ~/.de.carne.ddns/credentials.conf.
--noipv4
	Do not detect and update IPv4 address.
--noipv6
	Do not detect and update IPv6 address.
--force
	Forces an update (even if the zone is considered up-to-date).
--pretend
	Run but do not apply any DNS changes.
--host host
	The host name to update.
```

### Credentials file
See [example](https://raw.githubusercontent.com/hdecarne/ddns-updater/master/src/test/resources/credentials.conf).

### Changelog:
See [CHANGELOG.md](https://raw.githubusercontent.com/hdecarne/ddns-updater/master/CHANGELOG.md).


### License
This project is subject to the the GNU General Public License version 3 or later version.
See LICENSE information for details.
