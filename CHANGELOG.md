## Changelog:
This is the latest release of the ddns-updater tool capable of performing dynamic DNS (DDNS) updates.
The following address detection mechanisms are supported:
* Interface based (examining the addresses of the running host's interfaces)
* UPnP based (querying the local network's router for the external IPv4 address)
* Web based (querying one or more web based services to determine the running host's ip addresses)
The following DNS backends are supported:
* AWS/Route53 (updating AWS/Route53 zone information)
* Web (invoking a web based service to update DNS)

This software may be modified and distributed under the terms
of the MIT license.  See the LICENSE file for details.

### v0.2.1 (2023-03-18)
* Auto-detect TTY and use colored output if applicable
* Update dependencies

### v0.2.0 (2023-02-28)
* Release of completely rewritten Go based version
* LICENSE changed to MIT license
* Address finder mechanisms: Interface, UPnP, Web
* DNS update mechanisms: AWS/Route53, Web

### v0.1.1 (2021-03-13)
* Initial release
* ipv6-test.com address detection support
* Only use SSL services for address detection

### v0.1.0-beta1 (2021-03-07)
* Initial release
* AWS/Route53 DNS backend support
* ipify.org address detection support
* ip4/6.me address detection support