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

package com.liferay.arkadiko;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.BundleContext;

import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * @author Raymond Augé
 */
public class AKBeanDefinition extends RootBeanDefinition {

	/**
	 * Instantiates a new aK bean definition.
	 *
	 * @param beanPostProcessor the bean post processor
	 * @param beanDefinition the bean definition
	 * @param beanName the bean name
	 * @param interfaces the interfaces
	 * @param bundleContext the bundle context
	 */
	public AKBeanDefinition(
		AKBeanPostProcessor beanPostProcessor,
		RootBeanDefinition beanDefinition, String beanName,
		BundleContext bundleContext) {

		super(beanDefinition);

		_beanPostProcessor = beanPostProcessor;
		_beanDefinition = beanDefinition;
		_beanName = beanName;
		_bundleContext = bundleContext;
	}

	/**
	 * Clone bean definition.
	 *
	 * @return the aK bean definition
	 */
	@Override
	public AKBeanDefinition cloneBeanDefinition() {
		return new AKBeanDefinition(
			_beanPostProcessor, _beanDefinition.cloneBeanDefinition(),
			_beanName, _bundleContext);
	}

	/**
	 * Gets the proxy.
	 *
	 * @return the proxy
	 */
	public Object getProxy() {
		String className = getBeanClassName();

		if ((className == null) ||
			!(className.startsWith(AKConstants.CLASSNAME_DECORATOR) &&
			  className.endsWith(AKConstants.CLOSE_PAREN))) {

			return null;
		}

		if (_proxyMap.get() == null) {
			_proxyMap.set(new HashMap<String,Object>());
		}

		if ((_proxy == null) && _proxyMap.get().containsKey(className)) {
			_proxy = _proxyMap.get().get(className);
		}

		if (_proxy != null) {
			return _proxy;
		}

		try {
			List<Class<?>> interfaces = getInterfaces(className);

			_proxy = _beanPostProcessor.createProxy(
				_bundleContext, null, _beanName, interfaces);

			setBeanClass(_proxy.getClass());

			_proxyMap.get().put(className, _proxy);
		}
		catch (Exception e) {
			_log.error(e, e);
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

	protected List<Class<?>> getInterfaces(String className)
		throws ClassNotFoundException {

		className = className.substring(
			AKConstants.CLASSNAME_DECORATOR.length(), className.length() - 1);

		String[] classNameParts = className.split(AKConstants.COMMA);

		List<Class<?>> interfaces = new ArrayList<Class<?>>();

		for (String interfaceName : classNameParts) {
			Class<?> interfaceClass = Class.forName(interfaceName);

			if (!interfaceClass.isInterface()) {
				throw new IllegalArgumentException(
					interfaceName + " is not an interface");
			}

			interfaces.add(interfaceClass);
		}

		return interfaces;
	}

	private static final Log _log = LogFactory.getLog(AKBeanDefinition.class);

	private RootBeanDefinition _beanDefinition;
	private String _beanName;
	private AKBeanPostProcessor _beanPostProcessor;
	private BundleContext _bundleContext;
	private Object _proxy;

	private static ThreadLocal<Map<String,Object>> _proxyMap =
		new ThreadLocal<Map<String,Object>>();

}