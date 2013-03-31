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

package com.liferay.arkadiko.test.beans;

import com.liferay.arkadiko.test.interfaces.InterfaceOne;

/**
 * <a href="HasDependencyOnInterfaceOne.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class HasDependencyOnInterfaceOne {

	public void useInterfaceOne() {
		getInterfaceOne().methodOne();
	}

	/**
	 * @return the interfaceOne
	 */
	public InterfaceOne getInterfaceOne() {
		return _interfaceOne;
	}

	/**
	 * @param interfaceOne the interfaceOne to set
	 */
	public void setInterfaceOne(InterfaceOne interfaceOne) {
		_interfaceOne = interfaceOne;
	}

	private InterfaceOne _interfaceOne;

}