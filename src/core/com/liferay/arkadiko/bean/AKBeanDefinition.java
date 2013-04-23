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

package com.liferay.arkadiko.bean;

import com.liferay.arkadiko.internal.Constants;
import com.liferay.arkadiko.sr.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;

/**
 * @author Raymond Augé
 */
public class AKBeanDefinition extends GenericBeanDefinition {

	/**
	 * Instantiates a new Arkadiko bean definition.
	 *
	 * @param beanPostProcessor the bean post processor
	 * @param abstractBeanDefinition the bean definition
	 * @param beanName the bean name
	 * @param interfaces the interfaces
	 * @param serviceRegistry the service registry
	 */
	public AKBeanDefinition(
		AKBeanPostProcessor beanPostProcessor,
		AbstractBeanDefinition abstractBeanDefinition, String beanName,
		ServiceRegistry serviceRegistry) {

		super(abstractBeanDefinition);

		_beanPostProcessor = beanPostProcessor;
		_abstractBeanDefinition = abstractBeanDefinition;
		_beanName = beanName;
		_serviceRegistry = serviceRegistry;
		_originalClassName = abstractBeanDefinition.getBeanClassName();
	}

	/**
	 * Clone bean definition.
	 *
	 * @return the aK bean definition
	 */
	@Override
	public AKBeanDefinition cloneBeanDefinition() {
		return new AKBeanDefinition(
			_beanPostProcessor,
			_abstractBeanDefinition.cloneBeanDefinition(),
			_beanName, _serviceRegistry);
	}

	/**
	 * Gets the proxy.
	 *
	 * @return the proxy
	 */
	public Object getProxy() {
		String className = _originalClassName;

		if (className == null) {
			return null;
		}

		if (_proxyMap.get() == null) {
			_proxyMap.set(new HashMap<String,Object>());
		}

		String mapKey = _beanName.concat(Constants.POUND).concat(className);

		if ((_proxy == null) && _proxyMap.get().containsKey(mapKey)) {
			_proxy = _proxyMap.get().get(mapKey);
		}

		if (_proxy != null) {
			setBeanClass(_proxy.getClass());

			return _proxy;
		}

		try {
			Class<?>[] interfaces = getInterfaces(className);

			_proxy = _serviceRegistry.registerBeanAsService(
				null, _beanName, interfaces, true);

			setBeanClass(_proxy.getClass());

			_proxyMap.get().put(mapKey, _proxy);
		}
		catch (Exception e) {
			if (_log.isLoggable(Level.SEVERE)) {
				_log.log(Level.SEVERE, e.getMessage(), e);
			}
		}

		return _proxy;
	}

	/**
	 * Resolve bean class.
	 *
	 * @param classLoader the class loader
	 * @return the class
	 * @throws ClassNotFoundException the class not found exception
	 */
	@Override
	public Class resolveBeanClass(ClassLoader classLoader)
		throws ClassNotFoundException {

		Object proxy = getProxy();

		if (proxy == null) {
			throw new ClassNotFoundException(getBeanClassName());
		}

		return proxy.getClass();
	}

	protected Class<?>[] getInterfaces(String className)
		throws ClassNotFoundException {

		Class<?> interfaceClass = Class.forName(className);

		if (!interfaceClass.isInterface()) {
			throw new IllegalArgumentException(
				className + " is not an interface");
		}

		return new Class<?>[] {interfaceClass};
	}

	private static Logger _log = Logger.getLogger(
		AKBeanDefinition.class.getName());

	private AbstractBeanDefinition _abstractBeanDefinition;
	private String _beanName;
	private AKBeanPostProcessor _beanPostProcessor;
	private String _originalClassName;
	private Object _proxy;
	private ServiceRegistry _serviceRegistry;

	private static ThreadLocal<Map<String,Object>> _proxyMap =
		new ThreadLocal<Map<String,Object>>();

}