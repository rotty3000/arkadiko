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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.launch.Framework;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.core.Ordered;

/**
 * @author Raymond Augé
 */
public class AKBeanPostProcessor extends SimpleInstantiationStrategy
	implements BeanFactoryPostProcessor, BeanPostProcessor, Ordered {

	private static Constructor<RootBeanDefinition> _constructor = null;

	static {
		Constructor<RootBeanDefinition> constructor = null;

		try {
			for (Constructor<?> curConstructor :
					RootBeanDefinition.class.getDeclaredConstructors()) {

				Class<?>[] parameterTypes = curConstructor.getParameterTypes();
				if ((parameterTypes.length == 1) &&
					(parameterTypes[0].equals(BeanDefinition.class))) {

					constructor =
						(Constructor<RootBeanDefinition>)curConstructor;
				}
			}

			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);
			}

			_constructor = constructor;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * After properties set.
	 */
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

	/**
	 * Gets the class loader.
	 *
	 * @return the class loader
	 */
	public ClassLoader getClassLoader() {
		return _classLoader;
	}

	/**
	 * Gets the extra bean properties.
	 *
	 * @return the extra bean properties
	 */
	public Map<String, Object> getExtraBeanProperties() {
		return _extraBeanProperties;
	}

	/**
	 * Gets the framework.
	 *
	 * @return the framework
	 */
	public Framework getFramework() {
		return _framework;
	}

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	public int getOrder() {
		return _order;
	}

	/**
	 * Instantiate.
	 *
	 * @param beanDefinition the bean definition
	 * @param beanName the bean name
	 * @param owner the owner
	 * @return the object
	 */
	@Override
	public Object instantiate(
		RootBeanDefinition beanDefinition, String beanName, BeanFactory owner) {

		if (beanDefinition instanceof AKBeanDefinition) {
			AKBeanDefinition akBeanDefinition =
				(AKBeanDefinition)beanDefinition;

			return akBeanDefinition.getProxy();
		}

		return super.instantiate(beanDefinition, beanName, owner);
	}

	/**
	 * Instantiate.
	 *
	 * @param beanDefinition the bean definition
	 * @param beanName the bean name
	 * @param owner the owner
	 * @param ctor the ctor
	 * @param args the args
	 * @return the object
	 */
	@Override
	public Object instantiate(
		RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
		Constructor<?> ctor, Object[] args) {

		if (beanDefinition instanceof AKBeanDefinition) {
			AKBeanDefinition akBeanDefinition =
				(AKBeanDefinition)beanDefinition;

			return akBeanDefinition.getProxy();
		}

		return super.instantiate(beanDefinition, beanName, owner, ctor, args);
	}

	/**
	 * Instantiate.
	 *
	 * @param beanDefinition the bean definition
	 * @param beanName the bean name
	 * @param owner the owner
	 * @param factoryBean the factory bean
	 * @param factoryMethod the factory method
	 * @param args the args
	 * @return the object
	 */
	@Override
	public Object instantiate(
		RootBeanDefinition beanDefinition, String beanName, BeanFactory owner,
		Object factoryBean, Method factoryMethod, Object[] args) {

		if (beanDefinition instanceof AKBeanDefinition) {
			AKBeanDefinition akBeanDefinition =
				(AKBeanDefinition)beanDefinition;

			return akBeanDefinition.getProxy();
		}

		return super.instantiate(beanDefinition, beanName, owner, factoryBean,
			factoryMethod, args);
	}

	/**
	 * Checks if is strict matching.
	 *
	 * @return true, if is strict matching
	 */
	public boolean isStrictMatching() {
		return _strictMatching;
	}

	/**
	 * Post process after initialisation.
	 *
	 * @param bean the bean
	 * @param beanId the bean id
	 * @return the object
	 * @throws BeansException the beans exception
	 */
	public Object postProcessAfterInitialization(Object bean, String beanId)
		throws BeansException {

		List<Class<?>> interfaces = AKIntrospector.getInterfacesAsList(
			bean);

		if (ignoreBean(bean, beanId, interfaces)) {
			return bean;
		}

		Framework framework = getFramework();

		BundleContext bundleContext = framework.getBundleContext();

		registerService(bundleContext, bean, beanId, interfaces);

		return createProxy(bundleContext, bean, beanId, interfaces);
	}

	/**
	 * Post process bean factory.
	 *
	 * @param beanFactory the bean factory
	 * @throws BeansException the beans exception
	 */
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory)
		throws BeansException {

		if (!(beanFactory instanceof DefaultListableBeanFactory)) {
			return;
		}

		DefaultListableBeanFactory defaultListableBeanFactory =
			(DefaultListableBeanFactory)beanFactory;

		defaultListableBeanFactory.setInstantiationStrategy(this);

		for (String beanName :
				defaultListableBeanFactory.getBeanDefinitionNames()) {

			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(
				beanName);

			String className = beanDefinition.getBeanClassName();

			if ((className == null) ||
				!(className.startsWith(AKConstants.CLASSNAME_DECORATOR) &&
				  className.endsWith(AKConstants.CLOSE_PAREN))) {

				continue;
			}

			try {
				AKBeanDefinition akBeanDefinition = new AKBeanDefinition(
					this, _constructor.newInstance(beanDefinition),
					beanName, getFramework().getBundleContext());

				defaultListableBeanFactory.removeBeanDefinition(beanName);

				defaultListableBeanFactory.registerBeanDefinition(
					beanName, akBeanDefinition);
			}
			catch (Exception e) {
				_log.error(e, e);
			}
		}
	}

	/**
	 * Post process before initialisation.
	 *
	 * @param bean the bean
	 * @param beanId the bean id
	 * @return the object
	 * @throws BeansException the beans exception
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanId)
		throws BeansException {

		return bean;
	}

	/**
	 * classLoader (Optional):
	 *
	 * The ClassLoader which will be used to create proxies around beans
	 * in order to implement ServiceListener support. If no ClassLoader is
	 * provided Thread.currentThread().getContextClassLoader() will be used.
	 *
	 * @param classLoader the new class loader
	 */
	public void setClassLoader(ClassLoader classLoader) {
		_classLoader = classLoader;
	}

	/**
	 * excludeBeanNames (Optional):
	 *
	 * Provide a list of bean names that should be excluded. Names may be
	 * prefixed or suffixed with a * for simple matching.
	 *
	 * @param excludeBeanNames the new ignored bean names
	 */
	public void setExcludeBeanNames(List<String> excludeBeanNames) {
		_excludeBeanNames = excludeBeanNames;
	}

	/**
	 * excludeClassNames (Optional):
	 *
	 * Provide a list of class names that should be excluded. Names may be
	 * prefixed or suffixed with a * for simple matching.
	 *
	 * @param excludeClassNames the new ignored class names
	 */
	public void setExcludeClassNames(List<String> excludeClassNames) {
		_excludeClassNames = excludeClassNames;
	}

	/**
	 * extraBeanProperties (Optional):
	 *
	 * Provide a Map (<util:map/>) which specifies key=value pairs for
	 * properties you want to add to each bean published to the framework.
	 *
	 * @param extraBeanProperties the extra bean properties
	 */
	public void setExtraBeanProperties(
		Map<String, Object> extraBeanProperties) {

		_extraBeanProperties = extraBeanProperties;
	}

	/**
	 * framework (Required):
	 *
	 * Provide an instance of org.osgi.framework.launch.Framework into which
	 * the spring beans, matching the rules below, will be published. In
	 * turn, from the framework it will be possible to provide services that
	 * implement or override beans that are needed or used in this spring
	 * context.
	 *
	 * @param framework the new framework
	 */
	public void setFramework(Framework framework) {
		_framework = framework;
	}

	/**
	 * includeBeanNames (Optional):
	 *
	 * Provide a list of bean names that should be included. Names may be
	 * prefixed or suffixed with a * for simple matching. The default
	 * behavior, if no list is provided, is to include all beans.
	 *
	 * @param includeBeanNames the new include bean names
	 */
	public void setIncludeBeanNames(List<String> includeBeanNames) {
		_includeBeanNames = includeBeanNames;
	}

	/**
	 * includeClassNames (Optional):
	 *
	 * Provide a list of class names that should be included. Names may be
	 * prefixed or suffixed with a * for simple matching. The default
	 * behavior, if no list is provided, is to include all beans.
	 *
	 * @param includeClassNames the new include class names
	 */
	public void setIncludeClassNames(List<String> includeClassNames) {
		_includeClassNames = includeClassNames;
	}


	/**
	 * order (Optional):
	 *
	 * As a BeanPostProcessor, we may have to play nicely with other
	 * BeanPostProcessors also included in the spring context. Use the
	 * order property to adjust the order in which BeanPostProcessors are
	 * invoked by spring. When BeanPostProcessors don't set an order, the
	 * ordering of invocation is indeterminate which may lead to unexpected
	 * behavior and possibly errors. Therefore setting the specific order is
	 * recommended. The most common case for Arkadiko is to be invoked last.
	 * The default value is 20.
	 *
	 * @param order the new order
	 */
	public void setOrder(int order) {
		_order = order;
	}

	/**
	 * proxyFactory (Optional):
	 *
	 * Provide the class name of a proxy factory class with a static method
	 * matching the signature of "newProxyInstance" method of
	 * java.lang.reflect.Proxy. If none is provided,
	 * {@link java.lang.reflect.Proxy} is used.
	 *
	 * @param proxyFactory the proxy factory class
	 */
	public void setProxyFactory(Class<?> proxyFactory) {
		_proxyFactory = proxyFactory;
	}

	/**
	 * strictMatching (Optional):
	 *
	 * If set to true, strict matching should
	 * occur when new services are published into the framework. Strict
	 * matches involve matching all interfaces as well as the bean.id
	 * property. Otherwise, only a single interface (typically the primary
	 * interface under which the service is published) and the bean.id
	 * property must match. Default is false.
	 *
	 * @param strictMatching the new strict matching
	 */
	public void setStrictMatching(boolean strictMatching) {
		_strictMatching = strictMatching;
	}

	/**
	 * Adds the extra bean properties.
	 *
	 * @param properties the properties
	 */
	protected void addExtraBeanProperties(Hashtable<String,Object> properties) {
		if ((_extraBeanProperties == null) || _extraBeanProperties.isEmpty()) {
			return;
		}

		for (Map.Entry<String, Object> entry :
				_extraBeanProperties.entrySet()) {

			properties.put(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Creates the filter.
	 *
	 * @param bundleContext the bundle context
	 * @param beanId the bean id
	 * @param interfaces the interfaces
	 * @return the filter
	 * @throws AKBeansException the aK beans exception
	 */
	protected Filter createFilter(
			BundleContext bundleContext, String beanId,
			List<Class<?>> interfaces)
		throws AKBeansException {

		StringBuffer sb = new StringBuffer(interfaces.size() * 5 + 10);

		sb.append(AKConstants.OPEN_PAREN_AND_AMP);

		if (!isStrictMatching() && (interfaces.size() > 1)) {
			sb.append(AKConstants.OPEN_PAREN_AND_PIPE);
		}

		for (Class<?> clazz : interfaces) {
			sb.append(AKConstants.OPEN_PAREN);
			sb.append(Constants.OBJECTCLASS);
			sb.append(AKConstants.EQUAL);
			sb.append(clazz.getName());
			sb.append(AKConstants.CLOSE_PAREN);
		}

		if (!isStrictMatching() && (interfaces.size() > 1)) {
			sb.append(AKConstants.CLOSE_PAREN);
		}

		sb.append(AKConstants.OPEN_PAREN);
		sb.append(AKConstants.BEAN_ID);
		sb.append(AKConstants.EQUAL);
		sb.append(beanId);
		sb.append(AKConstants.CP_OP_EX_OP);
		sb.append(AKConstants.ORIGINAL_BEAN);
		sb.append(AKConstants.EQ_STAR_CP_CP_CP);

		try {
			return bundleContext.createFilter(sb.toString());
		}
		catch (InvalidSyntaxException e) {
			throw new AKBeansException(e.getMessage(), e);
		}
	}

	/**
	 * Creates the proxy.
	 *
	 * @param bundleContext the bundle context
	 * @param bean the bean
	 * @param beanId the bean id
	 * @param interfaces the interfaces
	 * @return the object
	 * @throws BeansException the beans exception
	 */
	protected Object createProxy(
		BundleContext bundleContext, Object bean, String beanId,
		List<Class<?>> interfaces) throws BeansException {

		Filter filter = createFilter(bundleContext, beanId, interfaces);

		AKServiceTrackerInvocationHandler serviceTrackerInvocationHandler =
			new AKServiceTrackerInvocationHandler(
				bundleContext, filter, bean);

		serviceTrackerInvocationHandler.open(true);

		try {
			return _proxyFactoryMethod.invoke(
				null, getClassLoader(),
				interfaces.toArray(new Class<?>[interfaces.size()]),
				serviceTrackerInvocationHandler);
		}
		catch (Exception e) {
			throw new AKBeansException(e.getMessage(), e);
		}
	}

	/**
	 * Exclude bean by bean name.
	 *
	 * @param beanId the bean id
	 * @return true, if successful
	 */
	protected boolean excludeBeanByBeanName(String beanId) {
		if (_excludeBeanNames == null) {
			return false;
		}

		for (String excludedBeanId : _excludeBeanNames) {
			if (beanId.equals(excludedBeanId)) {
				return true;
			}
			else if (excludedBeanId.startsWith(AKConstants.STAR) &&
					 beanId.endsWith(excludedBeanId.substring(1))) {

				return true;
			}
			else if (excludedBeanId.endsWith(AKConstants.STAR) &&
					 beanId.startsWith(
						 excludedBeanId.substring(
							 0, excludedBeanId.length() - 1))) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Exclude bean by class name.
	 *
	 * @param className the class name
	 * @return true, if successful
	 */
	protected boolean excludeBeanByClassName(String className) {
		if (_excludeClassNames == null) {
			return false;
		}

		for (String excludedClassName : _excludeClassNames) {
			if (className.equals(excludedClassName)) {
				return true;
			}
			else if (excludedClassName.startsWith(AKConstants.STAR) &&
					 className.endsWith(excludedClassName.substring(1))) {

				return true;
			}
			else if (excludedClassName.endsWith(AKConstants.STAR) &&
					 className.startsWith(
						 excludedClassName.substring(
							 0, excludedClassName.length() - 1))) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Include bean by bean name.
	 *
	 * @param beanId the bean id
	 * @return true, if successful
	 */
	protected boolean includeBeanByBeanName(String beanId) {
		if (_includeBeanNames == null) {
			return true;
		}

		for (String includedBeanName : _includeBeanNames) {
			if (beanId.equals(includedBeanName)) {
				return true;
			}
			else if (includedBeanName.startsWith(AKConstants.STAR) &&
					 beanId.endsWith(includedBeanName.substring(1))) {

				return true;
			}
			else if (includedBeanName.endsWith(AKConstants.STAR) &&
					 beanId.startsWith(
						 includedBeanName.substring(
							 0, includedBeanName.length() - 1))) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Include bean by class name.
	 *
	 * @param className the class name
	 * @return true, if successful
	 */
	protected boolean includeBeanByClassName(String className) {
		if (_includeClassNames == null) {
			return true;
		}

		for (String includedClassName : _includeClassNames) {
			if (className.equals(includedClassName)) {
				return true;
			}
			else if (includedClassName.startsWith(AKConstants.STAR) &&
					 className.endsWith(includedClassName.substring(1))) {

				return true;
			}
			else if (includedClassName.endsWith(AKConstants.STAR) &&
					 className.startsWith(
						 includedClassName.substring(
							 0, includedClassName.length() - 1))) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Ignore bean.
	 *
	 * @param bean the bean
	 * @param beanId the bean id
	 * @param interfaces the interfaces
	 * @return true, if successful
	 */
	protected boolean ignoreBean(
		Object bean, String beanId, List<Class<?>> interfaces) {

		// Automatically exclude anonymous beans or beans that don't implement
		// any interfaces

		if ((beanId.indexOf(AKConstants.POUND) != -1) ||
			(beanId.equals("(inner bean)")) ||
			interfaces.isEmpty()) {

			return true;
		}

		// If there are inclusion lists, and the bean is not in the lists,
		// ignore it

		if (!includeBeanByBeanName(beanId) &&
			!includeBeanByClassName(beanId)) {

			return true;
		}

		// If the bean is specifically excluded by name of class, ignore it

		if (excludeBeanByBeanName(beanId) ||
			excludeBeanByClassName(bean.getClass().getName())) {

			return true;
		}

		return false;
	}

	/**
	 * Register service.
	 *
	 * @param bundleContext the bundle context
	 * @param bean the bean
	 * @param beanId the bean id
	 * @param interfaces the interfaces
	 */
	protected void registerService(
		BundleContext bundleContext, Object bean, String beanId,
		List<Class<?>> interfaces) {

		List<String> names = new ArrayList<String>();

		for (Class<?> interfaceClass : interfaces) {
			names.add(interfaceClass.getName());
		}

		if (names.isEmpty()) {
			return;
		}

		Hashtable<String,Object> properties = new Hashtable<String, Object>();

		properties.put(AKConstants.BEAN_ID, beanId);
		properties.put(AKConstants.ORIGINAL_BEAN, Boolean.TRUE);

		addExtraBeanProperties(properties);

		bundleContext.registerService(
			names.toArray(new String[names.size()]), bean, properties);
	}

	private static final Log _log = LogFactory.getLog(
		AKBeanPostProcessor.class);

	private ClassLoader _classLoader;
	private List<String> _excludeBeanNames;
	private List<String> _excludeClassNames;
	private Map<String, Object> _extraBeanProperties;
	private Framework _framework;
	private List<String> _includeBeanNames;
	private List<String> _includeClassNames;
	private int _order = 20;
	private Class<?> _proxyFactory;
	private Method _proxyFactoryMethod;
	private boolean _strictMatching = false;

}
