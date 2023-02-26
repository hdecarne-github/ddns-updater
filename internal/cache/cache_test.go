// cache.go
//
// Copyright (C) 2023 Holger de Carne
//
// This software may be modified and distributed under the terms
// of the MIT license.  See the LICENSE file for details.

package cache

import (
	"testing"

	"github.com/stretchr/testify/require"
)

func TestDefaultCache(t *testing.T) {
	key := "key"
	miss := Get(key)
	require.Equal(t, miss, "")
	value := "value"
	Put(key, value)
	hit := Get(key)
	require.Equal(t, value, hit)
	err := Flush()
	require.NoError(t, err)
}
