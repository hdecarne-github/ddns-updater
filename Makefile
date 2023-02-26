GOCMDS := ddns-updater
GOMODULE := github.com/hdecarne-github/$(firstword $(GOCMDS))
GOMODULE_VERSION :=  $(shell cat version.txt)

GO := $(shell command -v go 2> /dev/null)

ifdef GO
GOOS ?= $(shell go env GOOS)
LDFLAGS := $(LDFLAGS) -X $(GOMODULE)/internal/ddnsupdater.version=$(GOMODULE_VERSION) -X $(GOMODULE)/internal/ddnsupdater.timestamp=$(shell date +%Y%m%d%H%M%S)
ifneq (windows, $(GOOS))
GOCMDEXT :=
else
GOCMDEXT := .exe
endif
endif

.DEFAULT_GOAL := help

.PHONY: help
help:
	@echo "Please use 'make <target>' where <target> is one of the following:"
	@echo "  make check\tcheck whether current build environment is sane"
	@echo "  make deps\tprepare needed dependencies"
	@echo "  make build\tbuild artifacts"
	@echo "  make clean\tdiscard build artifacts (not dependencies)"

.PHONY: check
check:
	@echo "Using build environment:"
ifndef GO
    $(error "ERROR: go command is not available")
endif
	@echo "  GO: $(GO)"

.PHONY: deps
deps: check deps-init deps-go

.PHONY: deps-init
deps-init:
	@echo "Preparing dependencies..."

.PHONY: deps-go
deps-go:
	$(GO) mod download -x

.PHONY: build
build: check build-init build-go

.PHONY: build-init
build-init:
	@echo "Building artifacts..."

.PHONY: build-go
build-go:
	mkdir -p "build/bin"
	$(foreach GOCMD, $(GOCMDS), $(GO) build -ldflags "$(LDFLAGS)" -o "./build/bin/$(GOCMD)$(GOCMDEXT)" ./cmd/$(GOCMD);)

.PHONY: test
test: check test-init test-go

.PHONY: test-init
test-init:
	@echo "Testing artifacts..."

.PHONY: test-go
test-go:
	$(GO) test -v -covermode=atomic -coverprofile=build/coverage.out ./...

.PHONY: clean
clean: check clean-init clean-go clean-build

.PHONY: clean-init
clean-init:
	@echo "Cleaning build artifacts..."

.PHONY: clean-go
clean-go:
	$(GO) clean ./...

.PHONY: clean-build
clean-build:
	rm -rf "build"

.PHONY: tidy
tidy:
	go mod verify
	go mod tidy