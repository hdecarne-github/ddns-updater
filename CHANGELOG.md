## Changelog:
This is the initial release of ddns-updater.
Only currently supported DNS backend is AWS/Route53.
For IP address detection the services [ipv6-test.com](https://ipv6-test.com/), [ipify.org](https://www.ipify.org/) and [ip4.me](https://ip4.me/) are used.  

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

### v0.1.1 (2021-03-13)
* Initial release
* ipv6-test.com address detection support
* Only use SSL services for address detection

### v0.1.0-beta1 (2021-03-07)
* Initial release
* AWS/Route53 DNS backend support
* ipify.org address detection support
* ip4/6.me address detection support