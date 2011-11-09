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
import org.apache.commons.logging.LogFactory;

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

		Log log = _logFactory.getInstance(bundle.getSymbolicName());

		StringBuilder sb = new StringBuilder(3);

		sb.append(entry.getMessage());

		if (serviceReference != null) {
			sb.append(" ");
			sb.append(serviceReference.toString());
		}

		if ((level == LogService.LOG_DEBUG) && log.isDebugEnabled()) {
			log.debug(sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_ERROR) && log.isErrorEnabled()) {
			log.error(sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_INFO) && log.isInfoEnabled()) {
			log.info(sb.toString(), entry.getException());
		}
		else if ((level == LogService.LOG_WARNING) && log.isWarnEnabled()) {
			log.warn(sb.toString(), entry.getException());
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
	public void setLogFactory(LogFactory logFactory) {
		_logFactory = logFactory;
	}

	@Reference
	public void setLogReaderService(LogReaderService logReaderService) {
		_logReaderService = logReaderService;
	}

	private LogFactory _logFactory;
	private LogReaderService _logReaderService;

}