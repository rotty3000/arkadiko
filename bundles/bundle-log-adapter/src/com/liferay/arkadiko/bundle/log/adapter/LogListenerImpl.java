/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.arkadiko.bundle.log.adapter;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;

import java.io.InputStream;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * @author Raymond Augé
 */
@Component
public class LogListenerImpl implements LogListener {

	public LogListenerImpl() {
		try {
			Class<?> clazz = getClass();

			InputStream inputStream = clazz.getResourceAsStream(
				"/logging.properties");

			if (inputStream != null) {
				LogManager logManager = LogManager.getLogManager();

				logManager.readConfiguration(inputStream);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void logged(LogEntry entry) {
		Bundle bundle = entry.getBundle();
		int level = entry.getLevel();
		ServiceReference<?> serviceReference = entry.getServiceReference();

		Logger log = Logger.getLogger(bundle.getSymbolicName());

		StringBuilder sb = new StringBuilder(3);

		sb.append(entry.getMessage());

		if (serviceReference != null) {
			sb.append(" ");
			sb.append(serviceReference.toString());
		}

		if ((level == LogService.LOG_DEBUG) && log.isLoggable(Level.FINE)) {
			log.log(Level.FINE, sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_ERROR) &&
				 log.isLoggable(Level.SEVERE)) {

			log.log(Level.SEVERE, sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_INFO) && log.isLoggable(Level.INFO)) {
			log.log(Level.INFO, sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_WARNING) &&
				 log.isLoggable(Level.WARNING)) {

			log.log(Level.INFO, sb.toString(), entry.getException());
		}
	}

	@Activate
	public void activate() {
		_logReaderService.addLogListener(this);
	}

	@Deactivate
	public void deactivate() {
		_logReaderService.removeLogListener(this);
	}

	@Reference
	public void setLogReaderService(LogReaderService logReaderService) {
		_logReaderService = logReaderService;
	}

	private LogReaderService _logReaderService;

}