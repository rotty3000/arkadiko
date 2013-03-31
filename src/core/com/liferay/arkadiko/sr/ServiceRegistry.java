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

import java.util.Map;

import org.springframework.beans.BeansException;

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
	 * @throws BeansException the beans exception
	 */
	public Object createTrackingProxy(
			Object bean, String beanName, Class<?>[] interfaces)
		throws BeansException;

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

	/**
	 * strictMatching (Optional):
	 *
	 * If set to true, strict matching should occur when new services are
	 * published into the framework. Strict matches involve matching all
	 * interfaces as well as the bean.id property. Otherwise, only a single
	 * interface (typically the primary interface under which the service is
	 * published) and the bean.id property must match. Default is false.
	 *
	 * @param strictMatching the new strict matching
	 */
	public void setStrictMatching(boolean strictMatching);

	/**
	 * extraBeanProperties (Optional):
	 *
	 * Provide a Map (<util:map/>) of properties to be added to beans published
	 * to the service registry.
	 *
	 * @param extraBeanProperties the extra bean properties
	 */
	public void setExtraBeanProperties(
		Map<String, Object> extraBeanProperties);

}