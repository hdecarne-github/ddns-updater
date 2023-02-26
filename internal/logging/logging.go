// logging.go
//
// Copyright (C) 2023 Holger de Carne
//
// This software may be modified and distributed under the terms
// of the MIT license.  See the LICENSE file for details.

package logging

import (
	"io"
	"log"
	"os"

	"github.com/rs/zerolog"
)

var rootLogger = NewSimpleConsoleLogger(os.Stdout)

func UpdateRootLogger(logger zerolog.Logger, level zerolog.Level) {
	zerolog.SetGlobalLevel(level)
	rootLogger = logger
	log.SetFlags(0)
	log.SetOutput(logger)
	rootLogger.Debug().Msg("root logger updated")
}

func RootLogger() zerolog.Logger {
	return rootLogger
}

func NewDefaultConsoleLogger(out io.Writer) zerolog.Logger {
	return zerolog.New(zerolog.ConsoleWriter{Out: out}).With().Timestamp().Logger()
}

func NewSimpleConsoleLogger(out io.Writer) zerolog.Logger {
	return zerolog.New(zerolog.ConsoleWriter{Out: out, NoColor: true}).With().Timestamp().Logger()
}

func init() {
	zerolog.TimeFieldFormat = zerolog.TimeFormatUnix
}
