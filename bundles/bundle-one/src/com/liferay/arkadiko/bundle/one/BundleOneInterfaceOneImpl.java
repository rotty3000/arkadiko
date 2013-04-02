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

package com.liferay.arkadiko.bundle.one;

import aQute.bnd.annotation.component.Component;

import com.liferay.arkadiko.test.interfaces.InterfaceOne;

/**
 * <a href="BundleOneInterfaceOneImpl.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
@Component(properties = {"bean.id=interfaceOne"})
public class BundleOneInterfaceOneImpl implements InterfaceOne {

	public String getValue() {
		return _value;
	}

	public String methodOne() {
		return getClass().getName();
	}

	public void setValue(String value) {
		_value = value;
	}

	private String _value;

}