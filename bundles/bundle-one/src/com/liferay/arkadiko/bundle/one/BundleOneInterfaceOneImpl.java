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

package com.liferay.arkadiko.bundle.one;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.liferay.arkadiko.test.interfaces.InterfaceOne;

import org.osgi.service.log.LogService;

/**
 * <a href="BundleOneInterfaceOneImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
@Component
public class BundleOneInterfaceOneImpl implements InterfaceOne {

	public String getValue() {
		return _value;
	}

	public String methodOne() {
		_log.log(LogService.LOG_INFO, getClass().getName());

		return getClass().getName();
	}

	public void setValue(String value) {
		_value = value;

		_log.log(LogService.LOG_INFO, getClass().getName() + " " + value);
	}

	@Reference
	public void setLog(LogService log) {
		_log = log;
	}

	private LogService _log;
	private String _value;

}