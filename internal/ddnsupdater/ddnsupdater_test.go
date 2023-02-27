// ddnsupdater_test.go
//
// Copyright (C) 2023 Holger de Carne
//
// This software may be modified and distributed under the terms
// of the MIT license.  See the LICENSE file for details.

package ddnsupdater

import (
	"os"
	"testing"

	"github.com/stretchr/testify/require"
)

func TestReadConfig(t *testing.T) {
	cmd.Config = "../../ddns-updater.toml"
	cfg, err := cmd.readConfig()
	require.NoError(t, err)
	require.NotNil(t, cfg)
}

func TestRun(t *testing.T) {
	os.Args = []string{"ddns-updater", "--config=./testdata/ddns-updater.toml", "--pretend"}
	err := Run()
	require.NoError(t, err)
}
