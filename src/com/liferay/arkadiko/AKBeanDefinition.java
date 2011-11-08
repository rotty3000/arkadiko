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

import java.util.List;

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
		List<Class<?>> interfaces, BundleContext bundleContext) {

		super(beanDefinition);

		_beanPostProcessor = beanPostProcessor;
		_beanDefinition = beanDefinition;
		_beanName = beanName;
		_interfaces = interfaces;
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
			_beanName, _interfaces, _bundleContext);
	}

	/**
	 * Gets the proxy.
	 *
	 * @return the proxy
	 */
	public Object getProxy() {
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

		if (_proxy != null) {
			return _proxy.getClass();
		}

		String className = getBeanClassName();

		if (className == null) {
			return null;
		}

		ClassNotFoundException cnfe = null;

		try {
			return _beanDefinition.resolveBeanClass(classLoader);
		}
		catch (ClassNotFoundException cnfe1) {
			cnfe = cnfe1;
		}

		if (_proxy == null) {
			try {
				_proxy = _beanPostProcessor.createProxy(
					_bundleContext, null, _beanName, _interfaces);

				setBeanClass(_proxy.getClass());
			}
			catch (Exception e) {
				throw cnfe;
			}
		}

		return _proxy.getClass();
	}

	private RootBeanDefinition _beanDefinition;
	private String _beanName;
	private AKBeanPostProcessor _beanPostProcessor;
	private BundleContext _bundleContext;
	private List<Class<?>> _interfaces;
	private Object _proxy;

}