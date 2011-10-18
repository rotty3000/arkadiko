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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;

/**
 * @author Raymond Augé
 */
public class AKBeanPostProcessor implements BeanPostProcessor, Ordered {

	public void afterPropertiesSet() {
		if (_classLoader == null) {
			_classLoader = Thread.currentThread().getContextClassLoader();
		}

		if (_framework == null) {
			throw new IllegalArgumentException("framework is required");
		}

		if (_proxyFactory == null) {
			_proxyFactory = Proxy.class;
		}

		try {
			_proxyFactoryMethod = _proxyFactory.getMethod(
				"newProxyInstance", ClassLoader.class, Class[].class,
				InvocationHandler.class);
		}
		catch (Exception e) {
			throw new IllegalArgumentException(
				"proxy factory must implement newProxyInstance(...); " +
					"see java.lang.reflect.Proxy", e);
		}
	}

	public ClassLoader getClassLoader() {
		return _classLoader;
	}

	public Map<String, Object> getExtraBeanProperties() {
		return _extraBeanProperties;
	}

	public Framework getFramework() {
		return _framework;
	}

	public int getOrder() {
		return _order;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName)
		throws BeansException {

		List<Class<?>> interfaces = AKIntrospector.getInterfacesAsList(
			bean);

		if (ignoreBean(bean, beanName, interfaces)) {
			return bean;
		}

		Framework framework = getFramework();

		BundleContext bundleContext = framework.getBundleContext();

		registerService(bundleContext, bean, beanName, interfaces);

		return createProxy(bundleContext, bean, beanName, interfaces);
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName)
		throws BeansException {

		return bean;
	}

	public void setClassLoader(ClassLoader classLoader) {
		_classLoader = classLoader;
	}

	public void setExtraBeanProperties(
		Map<String, Object> extraBeanProperties) {

		_extraBeanProperties = extraBeanProperties;
	}

	public void setFramework(Framework framework) {
		_framework = framework;
	}

	public void setIgnoredBeanNames(List<String> ignoredBeanNames) {
		_ignoredBeanNames = ignoredBeanNames;
	}

	public void setIgnoredClassNames(List<String> ignoredClassNames) {
		_ignoredClassNames = ignoredClassNames;
	}

	public void setOrder(int order) {
		_order = order;
	}

	public void setProxyFactory(Class<?> proxyFactory) {
		_proxyFactory = proxyFactory;
	}

	protected void addExtraBeanProperties(Hashtable<String,Object> properties) {
		if ((_extraBeanProperties == null) || _extraBeanProperties.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Object> entry :
				_extraBeanProperties.entrySet()) {

			properties.put(entry.getKey(), entry.getValue());
		}
	}

	protected Filter createFilter(
			BundleContext bundleContext, List<Class<?>> interfaces)
		throws AKBeansException {

		StringBuffer sb = new StringBuffer(interfaces.size() * 3 + 4);

		sb.append("(&(|");

		for (Class<?> class1 : interfaces) {
			sb.append("(objectClass=");
			sb.append(class1.getName());
			sb.append(")");
		}

		sb.append(")(!(");
		sb.append(AKConstants.ORIGINAL_BEAN);
		sb.append("=*)))");

		try {
			return bundleContext.createFilter(sb.toString());
		}
		catch (InvalidSyntaxException e) {
			throw new AKBeansException(e.getMessage(), e);
		}
	}

	protected Object createProxy(
		BundleContext bundleContext, Object bean, String beanName,
		List<Class<?>> interfaces) throws BeansException {

		Filter filter = createFilter(bundleContext, interfaces);

		AKServiceTrackerInvocationHandler serviceTrackerInvocationHandler =
			new AKServiceTrackerInvocationHandler(
				bundleContext, filter, bean);

		serviceTrackerInvocationHandler.open();

		try {
			return _proxyFactoryMethod.invoke(
				null, getClassLoader(),
				interfaces.toArray(new Class<?>[interfaces.size()]),
				serviceTrackerInvocationHandler);
		}
		catch (Exception e) {
			throw new AKBeansException("", e);
		}
	}

	protected boolean ignoreBean(
		Object bean, String beanName, List<Class<?>> interfaces) {

		if (interfaces.isEmpty() ||
			ignoreBeanByBeanName(beanName) ||
			ignoreBeanByClassName(bean.getClass().getName())) {

			return true;
		}

		return false;
	}

	protected boolean ignoreBeanByBeanName(String beanName) {
		if (_ignoredBeanNames == null) {
			return false;
		}

		for (String ignoredBeanName : _ignoredBeanNames) {
			if (beanName.equals(ignoredBeanName)) {
				return true;
			}
			else if (ignoredBeanName.startsWith(AKConstants.STAR) &&
					 beanName.endsWith(ignoredBeanName.substring(1))) {

				return true;
			}
			else if (ignoredBeanName.endsWith(AKConstants.STAR) &&
					 beanName.startsWith(
						 ignoredBeanName.substring(
							 0, ignoredBeanName.length() - 1))) {

				return true;
			}
		}

		return false;
	}

	protected boolean ignoreBeanByClassName(String className) {
		if (_ignoredClassNames == null) {
			return false;
		}

		for (String ignoredClassName : _ignoredClassNames) {
			if (className.equals(ignoredClassName)) {
				return true;
			}
			else if (ignoredClassName.startsWith(AKConstants.STAR) &&
					 className.endsWith(ignoredClassName.substring(1))) {

				return true;
			}
			else if (ignoredClassName.endsWith(AKConstants.STAR) &&
					 className.startsWith(
						 ignoredClassName.substring(
							 0, ignoredClassName.length() - 1))) {

				return true;
			}
		}

		return false;
	}

	protected void registerService(
		BundleContext bundleContext, Object bean, String beanName,
		List<Class<?>> interfaces) {

		List<String> names = new ArrayList<String>();

		for (Class<?> interfaceClass : interfaces) {
			names.add(interfaceClass.getName());
		}

		Hashtable<String,Object> properties = new Hashtable<String, Object>();

		properties.put(AKConstants.BEAN_NAME, beanName);
		properties.put(AKConstants.ORIGINAL_BEAN, Boolean.TRUE);

		addExtraBeanProperties(properties);

		bundleContext.registerService(
			names.toArray(new String[names.size()]), bean, properties);
	}

	private Class<?> _proxyFactory;
	private ClassLoader _classLoader;
	private Map<String, Object> _extraBeanProperties;
	private Method _proxyFactoryMethod;
	private Framework _framework;
	private List<String> _ignoredBeanNames;
	private List<String> _ignoredClassNames;
	private int _order = 20;

}
