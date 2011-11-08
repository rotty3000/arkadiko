/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
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

import org.apache.commons.logging.Log;

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

	public void logged(LogEntry entry) {
		Bundle bundle = entry.getBundle();
		int level = entry.getLevel();
		ServiceReference<?> serviceReference = entry.getServiceReference();

		StringBuilder sb = new StringBuilder(5);

		sb.append("[");
		sb.append(bundle.getSymbolicName());
		sb.append("] ");
		sb.append(entry.getMessage());

		if (serviceReference != null) {
			sb.append(serviceReference.toString());
		}

		if ((level == LogService.LOG_DEBUG) && _log.isDebugEnabled()) {
			_log.debug(sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_ERROR) && _log.isErrorEnabled()) {
			_log.error(sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_INFO) && _log.isInfoEnabled()) {
			_log.info(sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_WARNING) && _log.isWarnEnabled()) {
			_log.warn(sb.toString(), entry.getException());
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
	public void setLog(Log log) {
		_log = log;
	}

	@Reference
	public void setLogReaderService(LogReaderService logReaderService) {
		_logReaderService = logReaderService;
	}

	private Log _log;
	private LogReaderService _logReaderService;

}