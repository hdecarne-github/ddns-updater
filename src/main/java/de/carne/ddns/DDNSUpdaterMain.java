/*
 * Copyright (c) 2018-2021 Holger de Carne and contributors, All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.carne.ddns;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.Properties;

import de.carne.boot.ApplicationMain;
import de.carne.ddns.route53.Route53Merger;
import de.carne.ddns.util.CombinedInquirer;
import de.carne.util.Exceptions;
import de.carne.util.cmdline.CmdLineException;
import de.carne.util.cmdline.CmdLineProcessor;
import de.carne.util.logging.ConsoleFormatter;
import de.carne.util.logging.ConsoleHandler;
import de.carne.util.logging.Log;
import de.carne.util.logging.LogBuffer;
import de.carne.util.logging.Logs;
import de.carne.util.prefs.FilePreferencesFactory;

/**
 * DDNSUpdater main class.
 */
public class DDNSUpdaterMain implements ApplicationMain {

	static {
		applyLogConfig(Logs.CONFIG_DEFAULT);
	}

	private static final Log LOG = new Log();

	private static final ConsoleHandler CONSOLE_HANDLER = new ConsoleHandler();

	static {
		CONSOLE_HANDLER.setFormatter(new ConsoleFormatter());
	}

	private static final String COMMAND_ARGUMENT_VERBOSE = "--verbose";
	private static final String COMMAND_ARGUMENT_DEBUG = "--debug";
	private static final String COMMAND_ARGUMENT_QUIET = "--quiet";
	private static final String COMMAND_ARGUMENT_PRETEND = "--pretend";
	private static final String COMMAND_ARGUMENT_FORCE = "--force";
	private static final String COMMAND_ARGUMENT_NOIPV4 = "--noipv4";
	private static final String COMMAND_ARGUMENT_NOIPV6 = "--noipv6";
	private static final String COMMAND_OPTION_HOST = "--host";
	private static final String COMMAND_OPTION_CREDENTIALS = "--credentials";

	private boolean runQuiet = false;
	private boolean runPretend = false;
	private long runForceTimeout = 24 * 60 * 60 * 1000l;
	private boolean runIPv4 = true;
	private boolean runIPv6 = true;
	private Optional<String> runHost = Optional.empty();
	private Optional<String> runCredentials = Optional
			.of(FilePreferencesFactory.customRootFile("credentials.conf").toString());

	@Override
	public int run(String[] args) {
		try {
			CmdLineProcessor cmdLine = buildCmdLine(args);

			cmdLine.process();

			initQuietMode();

			LOG.notice("Running command ''{0}''...", cmdLine);

			String host = this.runHost.orElseThrow(() -> missingCommandLineOption(cmdLine, COMMAND_OPTION_HOST));

			LOG.info(" Updating host: ''{0}''", host);

			Path credentialsFile = validatePathCommandLineOption(cmdLine, COMMAND_OPTION_CREDENTIALS,
					this.runCredentials);

			LOG.info(" Using credentials file: ''{0}''", credentialsFile);

			boolean updated = update(host, this.runIPv4, this.runIPv6, credentialsFile);

			if (updated) {
				untoggleQuietMode();
			}
		} catch (Exception e) {
			untoggleQuietMode();

			LOG.error(e, "Command failed: " + Exceptions.getMessage(e));
		}
		return 0;
	}

	private boolean update(String host, boolean ipv4, boolean ipv6, Path credentialsFile) throws IOException {
		Inquirer inquirer = new CombinedInquirer();

		Inet4Address inet4Address = null;

		if (ipv4) {
			inet4Address = inquirer.queryIPv4Address();
		}

		Inet6Address inet6Address = null;

		if (ipv6) {
			inet6Address = inquirer.queryIPv6Address();
		}

		UpdaterStatus updaterStatus = new UpdaterStatus(host);

		boolean updated = false;

		if (updaterStatus.isUpdateRequired(inet4Address, inet6Address, this.runForceTimeout)) {
			Credentials credentials = getCredentials(credentialsFile);
			Merger merger = new Route53Merger();

			merger.prepare(credentials, host);
			merger.mergeIPv4Address(inet4Address);
			merger.mergeIPv6Address(inet6Address);
			merger.commit(this.runPretend);
			updaterStatus.updateAndFlush();
			updated = !this.runPretend;
		}
		return updated;
	}

	private Credentials getCredentials(Path credentialsFile) throws IOException {
		Properties credentials = new Properties();

		try (InputStream credentialsStream = Files.newInputStream(credentialsFile, StandardOpenOption.READ)) {
			credentials.load(credentialsStream);
		} catch (NoSuchFileException e) {
			LOG.warning(e, "Ignoring non-existent credentials file ''{0}''", credentialsFile);
		}
		return key -> Optional.ofNullable(credentials.getProperty(key));
	}

	private Path validatePathCommandLineOption(CmdLineProcessor cmdLine, String optionName,
			Optional<String> optionValue) throws CmdLineException {
		String pathString = optionValue.orElseThrow(() -> missingCommandLineOption(cmdLine, optionName));
		Path path;

		try {
			path = Paths.get(pathString);
		} catch (InvalidPathException e) {
			throw invalidCommandLineOption(cmdLine, optionName, e);
		}
		return path;
	}

	private CmdLineException invalidCommandLineOption(CmdLineProcessor cmdLine, String optionName, Throwable cause) {
		return new CmdLineException(cmdLine, optionName, cause);
	}

	private CmdLineException missingCommandLineOption(CmdLineProcessor cmdLine, String optionName) {
		return new CmdLineException(cmdLine, optionName);
	}

	private void initQuietMode() {
		if (!this.runQuiet) {
			LogBuffer.addHandler(LOG, CONSOLE_HANDLER, true);
		}
	}

	private void untoggleQuietMode() {
		if (this.runQuiet) {
			LogBuffer.addHandler(LOG, CONSOLE_HANDLER, true);
			this.runQuiet = false;
		}
	}

	private static void applyLogConfig(String config) {
		try {
			Logs.readConfig(config);
		} catch (IOException e) {
			Exceptions.warn(e);
		}
	}

	private CmdLineProcessor buildCmdLine(String[] args) {
		CmdLineProcessor cmdLine = new CmdLineProcessor(name(), args);

		cmdLine.onSwitch(arg -> applyLogConfig(Logs.CONFIG_VERBOSE)).arg(COMMAND_ARGUMENT_VERBOSE);
		cmdLine.onSwitch(arg -> applyLogConfig(Logs.CONFIG_DEBUG)).arg(COMMAND_ARGUMENT_DEBUG);
		cmdLine.onSwitch(arg -> this.runQuiet = true).arg(COMMAND_ARGUMENT_QUIET);
		cmdLine.onSwitch(arg -> this.runPretend = true).arg(COMMAND_ARGUMENT_PRETEND);
		cmdLine.onSwitch(arg -> this.runForceTimeout = -1l).arg(COMMAND_ARGUMENT_FORCE);
		cmdLine.onSwitch(arg -> this.runIPv4 = false).arg(COMMAND_ARGUMENT_NOIPV4);
		cmdLine.onSwitch(arg -> this.runIPv6 = false).arg(COMMAND_ARGUMENT_NOIPV6);
		cmdLine.onOption((arg, value) -> this.runHost = Optional.of(value)).arg(COMMAND_OPTION_HOST);
		cmdLine.onOption((arg, value) -> this.runCredentials = Optional.of(value)).arg(COMMAND_OPTION_CREDENTIALS);
		cmdLine.onUnnamedOption(CmdLineProcessor::ignore);
		cmdLine.onUnknownArg(CmdLineProcessor::ignore);
		return cmdLine;
	}

}
