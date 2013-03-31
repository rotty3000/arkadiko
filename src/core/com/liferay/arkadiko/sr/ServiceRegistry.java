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

package com.liferay.arkadiko.sr;

/**
 * <a href="ServiceRegistry.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public interface ServiceRegistry {

	/**
	 * Creates a proxy which is backed by a service tracker on the service
	 * registry.
	 *
	 * @param bean the bean
	 * @param beanName the bean id
	 * @param interfaces the interfaces
	 * @return the proxy
	 * @throws Exception
	 */
	public Object createTrackingProxy(
			Object bean, String beanName, Class<?>[] interfaces)
		throws Exception;

	/**
	 * Checks if is strict matching.
	 *
	 * @return true, if is strict matching
	 */
	public boolean isStrictMatching();

	/**
	 * Register a bean as a service into the service registry.
	 *
	 * @param bean
	 * @param beanName
	 * @param interfaces
	 */
	public void registerBeanService(
		Object bean, String beanName, Class<?>[] interfaces);

}