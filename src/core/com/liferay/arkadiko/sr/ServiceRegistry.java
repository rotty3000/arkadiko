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
 * @author Raymond Augé
 */
public interface ServiceRegistry {

	/**
	 * Creates a proxy which is backed by a service tracker.
	 *
	 * @param bean the bean
	 * @param beanName the bean's logical name
	 * @param interfaces the interfaces bean implements
	 * @return the proxy
	 * @throws Exception
	 */
	public Object createTrackingProxy(
			Object bean, String beanName, Class<?>[] interfaces)
		throws Exception;

	/**
	 * Register a bean as a service without a service tracker.
	 *
	 * @param bean the bean
	 * @param beanName the bean's logical name
	 * @param interfaces the interfaces the bean implements
	 */
	public void registerBeanService(
		Object bean, String beanName, Class<?>[] interfaces);

}