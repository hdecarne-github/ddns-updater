// buildinfo.go
//
// Copyright (C) 2023 Holger de Carne
//
// This software may be modified and distributed under the terms
// of the MIT license.  See the LICENSE file for details.

package ddnsupdater

import (
	"fmt"
	"runtime"
)

const undefined = "<undefined>"

var version = undefined
var timestamp = undefined

func Version() string {
	return version
}

func Timestamp() string {
	return timestamp
}

func FullVersion() string {
	return fmt.Sprintf("certd version %s (%s) %s/%s", Version(), Timestamp(), runtime.GOOS, runtime.GOARCH)
}