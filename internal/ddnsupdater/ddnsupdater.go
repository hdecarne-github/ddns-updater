// ddnsupdater.go
//
// Copyright (C) 2023 Holger de Carne
//
// This software may be modified and distributed under the terms
// of the MIT license.  See the LICENSE file for details.

package ddnsupdater

import (
	"bytes"
	"fmt"
	"io/ioutil"
	"net"
	"os"
	"time"

	"github.com/BurntSushi/toml"
	"github.com/alecthomas/kong"
	"github.com/hdecarne-github/ddns-updater/internal/logging"
	"github.com/hdecarne-github/ddns-updater/pkg/address"
	"github.com/hdecarne-github/ddns-updater/pkg/address/iface"
	"github.com/hdecarne-github/ddns-updater/pkg/address/upnp"
	"github.com/hdecarne-github/ddns-updater/pkg/address/web"
	"github.com/hdecarne-github/ddns-updater/pkg/dns"
	"github.com/hdecarne-github/ddns-updater/pkg/dns/dummy"
	"github.com/hdecarne-github/ddns-updater/pkg/dns/route53"
	"github.com/rs/zerolog"
)

const defaultConfigFile = "/etc/ddns-updater/ddns-updater.toml"

type ddnsupdater struct {
	Config  string `help:"Use this config file"`
	Pretend bool   `help:"Only show changes, but do not apply them"`
	Verbose bool   `help:"Enable verbose output"`
	Debug   bool   `help:"Enable debug output"`
	logger  zerolog.Logger
}

func (cmd *ddnsupdater) Run() error {
	config, err := cmd.readConfig()
	if err != nil {
		return err
	}
	cmd.applyGlobalConfig(config)
	finders := cmd.evalFinderConfigs(config)
	updaters := cmd.evalUpdaterConfigs(config)
	if len(finders) == 0 {
		cmd.logger.Warn().Msg("No address finders configured")
	} else if len(updaters) == 0 {
		cmd.logger.Warn().Msg("No DNS updaters configured")
	} else {
		ips, err := cmd.findIPs(finders)
		if err != nil {
			return err
		}
		if len(ips) > 0 {
			ips = cmd.normalizeIPs(ips)
			cmd.updateIPs(updaters, ips)
		} else {
			cmd.logger.Warn().Msg("No addresses found, nothing to update")
		}
	}
	return nil
}

func (cmd *ddnsupdater) findIPs(finders []address.Finder) ([]net.IP, error) {
	cmd.logger.Info().Msg("Gathering addresses...")
	found := make([]net.IP, 0)
	for _, finder := range finders {
		ips0, err := finder.Run()
		if err != nil {
			return nil, err
		}
		found = append(found, ips0...)
	}
	return found, nil
}

func (cmd *ddnsupdater) updateIPs(updaters []dns.Updater, ips []net.IP) error {
	cmd.logger.Info().Msg("Updating DNS...")
	for _, updater := range updaters {
		err := updater.Merge(ips, cmd.Pretend)
		if err != nil {
			return err
		}
	}
	return nil
}

func (cmd *ddnsupdater) normalizeIPs(ips []net.IP) []net.IP {
	normalized := make([]net.IP, 0)
	for _, ip := range ips {
		insertAt := 0
		for _, current := range normalized {
			comparison := bytes.Compare(ip, current)
			if comparison == 0 {
				insertAt = -1
				break
			} else if comparison > 0 {
				break
			}
			insertAt = insertAt + 1
		}
		if insertAt >= 0 {
			normalized = append(normalized, nil)
			copy(normalized[insertAt+1:], normalized[insertAt:])
			normalized[insertAt] = ip
		}
	}
	return normalized
}

func (cmd *ddnsupdater) readConfig() (*ddnsupdaterConfig, error) {
	configFile := cmd.Config
	if configFile == "" {
		configFile = defaultConfigFile
	}
	configData, err := ioutil.ReadFile(configFile)
	if err != nil {
		return nil, fmt.Errorf("failed to read config file '%s'\n\tcause: %v", configFile, err)
	}
	var config ddnsupdaterConfig
	err = toml.Unmarshal(configData, &config)
	if err != nil {
		return nil, fmt.Errorf("failed to decode config file '%s'\n\tcause: %v", configFile, err)
	}
	return &config, nil
}

func (cmd *ddnsupdater) applyGlobalConfig(config *ddnsupdaterConfig) {
	if cmd.Debug || config.Global.Debug {
		logging.UpdateRootLogger(logging.NewSimpleConsoleLogger(os.Stdout), zerolog.DebugLevel)
	} else if cmd.Verbose || config.Global.Verbose {
		logging.UpdateRootLogger(logging.NewSimpleConsoleLogger(os.Stdout), zerolog.InfoLevel)
	} else {
		logging.UpdateRootLogger(logging.NewSimpleConsoleLogger(os.Stderr), zerolog.WarnLevel)
	}
}

func (cmd *ddnsupdater) evalFinderConfigs(config *ddnsupdaterConfig) []address.Finder {
	finders := make([]address.Finder, 0)
	if config.IFaceAddressFinder.IsEnabled() {
		finder := iface.NewIFaceFinder(&config.IFaceAddressFinder)
		finders = append(finders, finder)
	}
	if config.UPnPAddressFinder.IsEnabled() {
		finder := upnp.NewUPnPFinder(&config.UPnPAddressFinder)
		finders = append(finders, finder)
	}
	if config.WebAddressFinder.IsEnabled() {
		finder := web.NewWebFinder(&config.WebAddressFinder)
		finders = append(finders, finder)
	}
	return finders
}

func (cmd *ddnsupdater) evalUpdaterConfigs(config *ddnsupdaterConfig) []dns.Updater {
	updaters := make([]dns.Updater, 0)
	if config.DummyUpdaterConfig.IsEnabled(dummy.Name) {
		updater := dummy.NewDummyUpdater(&config.DummyUpdaterConfig)
		updaters = append(updaters, updater)
	}
	if config.Route53UpdaterConfig.IsEnabled(route53.Name) {
		updater := route53.NewRoute53Updater(&config.Route53UpdaterConfig)
		updaters = append(updaters, updater)
	}
	return updaters
}

type ddnsupdaterConfig struct {
	Global               globalConfig                 `toml:"global"`
	IFaceAddressFinder   iface.IFaceFinderConfig      `toml:"address_interface"`
	UPnPAddressFinder    upnp.UPnPFinderConfig        `toml:"address_upnp"`
	WebAddressFinder     web.WebFinderConfig          `toml:"address_web"`
	DummyUpdaterConfig   dummy.DummyUpdaterConfig     `toml:"dns_dummy"`
	Route53UpdaterConfig route53.Route53UpdaterConfig `toml:"dns_route53"`
}

type globalConfig struct {
	Verbose       bool          `toml:"verbose"`
	Debug         bool          `toml:"debug"`
	Cache         string        `toml:"cache"`
	CacheDuration time.Duration `toml:"cache_duration"`
}

var cmd ddnsupdater = ddnsupdater{logger: logging.RootLogger()}

func Run() error {
	err := kong.Parse(&cmd).Run()
	if err != nil {
		cmd.logger.Error().Msgf("ddns-updater command failed\n\tcause: %v", err)
	}
	return err
}