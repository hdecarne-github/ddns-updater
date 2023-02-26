// cache.go
//
// Copyright (C) 2023 Holger de Carne
//
// This software may be modified and distributed under the terms
// of the MIT license.  See the LICENSE file for details.

package cache

import "time"

type Provider interface {
	Get(key string) string
	Put(key string, value string, validity time.Duration)
	Flush() error
}

var provider Provider = &defaultProvider{cache: make(map[string]string)}
var cacheValidity time.Duration = time.Hour * 24

func Get(key string) string {
	return provider.Get(key)
}

func Put(key string, value string) {
	provider.Put(key, value, cacheValidity)
}

func Flush() error {
	return provider.Flush()
}

type defaultProvider struct {
	cache map[string]string
}

func (p *defaultProvider) Get(key string) string {
	return p.cache[key]
}

func (p *defaultProvider) Put(key string, value string, validity time.Duration) {
	p.cache[key] = value
}

func (p *defaultProvider) Flush() error {
	return nil
}
