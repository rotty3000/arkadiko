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

package com.liferay.arkadiko.reflect;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Raymond Augé
 */
public class Introspector {

	/**
	 * Gets the interfaces.
	 *
	 * @param bean the bean
	 * @return the interfaces
	 */
	public static Class<?>[] getInterfaces(Object bean) {
		Set<Class<?>> interfaces = getInterfacesAsSet(bean);

		return interfaces.toArray(new Class<?>[interfaces.size()]);
	}

	/**
	 * Gets the interfaces as set.
	 *
	 * @param bean the bean
	 * @return the interfaces as a set
	 */
	public static Set<Class<?>> getInterfacesAsSet(Object bean) {
		Set<Class<?>> interfaces = new HashSet<Class<?>>();

		Class<?> beanClass = bean.getClass();

		interfaces.addAll(Arrays.asList(beanClass.getInterfaces()));

		while((beanClass = beanClass.getSuperclass()) != null) {
			for (Class<?> classInterface : beanClass.getInterfaces()) {
				interfaces.add(classInterface);
			}
		}

		return interfaces;
	}

}