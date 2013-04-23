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
import com.liferay.arkadiko.reflect.Introspector;
import com.liferay.arkadiko.sr.ServiceRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.support.SimpleInstantiationStrategy;
import org.springframework.core.Ordered;

/**
 * @author Raymond Augé
 */
public class AKBeanPostProcessor extends SimpleInstantiationStrategy
	implements BeanFactoryPostProcessor, BeanPostProcessor, Ordered {

	public void afterPropertiesSet() {
		if (_serviceRegistry == null) {
			throw new IllegalArgumentException("serviceRegistry is required");
		}

		if (_excludeBeanNames == null) {
			_excludeBeanNames = Collections.emptyList();
		}

		if (_excludeClassNames == null) {
			_excludeClassNames = Collections.emptyList();
		}

		if (_includeBeanNames == null) {
			_includeBeanNames = Collections.emptyList();
		}

		if (_includeClassNames == null) {
			_includeClassNames = Collections.emptyList();
		}
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
	 * Gets the service registry (an abstraction of the OSGi framework service
	 * registry).
	 *
	 * @return the service registry
	 */
	public ServiceRegistry getServiceRegistry() {
		return _serviceRegistry;
	}

	@Override
	public Object instantiate(
		RootBeanDefinition rootBeanDefinition, String beanName,
		BeanFactory owner) {

		Object bean = getServiceBean(rootBeanDefinition, beanName);

		if (bean != null) {
			return bean;
		}

		return super.instantiate(rootBeanDefinition, beanName, owner);
	}

	@Override
	public Object instantiate(
		RootBeanDefinition rootBeanDefinition, String beanName,
		BeanFactory owner, Constructor<?> constructor, Object[] arguments) {

		Object bean = getServiceBean(rootBeanDefinition, beanName);

		if (bean != null) {
			return bean;
		}

		return super.instantiate(
			rootBeanDefinition, beanName, owner, constructor, arguments);
	}

	@Override
	public Object instantiate(
		RootBeanDefinition rootBeanDefinition, String beanName,
		BeanFactory owner, Object factoryBean, Method factoryMethod,
		Object[] arguments) {

		Object bean = getServiceBean(rootBeanDefinition, beanName);

		if (bean != null) {
			return bean;
		}

		return super.instantiate(
			rootBeanDefinition, beanName, owner, factoryBean, factoryMethod,
			arguments);
	}

	/**
	 * Post process after initialization.
	 *
	 * @param bean the bean
	 * @param beanName the bean id
	 * @return the object
	 * @throws BeansException the beans exception
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName)
		throws BeansException {

		Class<?>[] interfaces = Introspector.getInterfaces(bean);

		if (ignoreBean(bean.getClass().getName(), beanName, interfaces)) {
			return bean;
		}

		try {
			return _serviceRegistry.registerBeanAsService(
				bean, beanName, interfaces, _trackService);
		}
		catch (Exception e) {
			throw new AKBeansException(e.getMessage(), e);
		}
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

		AutowireCandidateResolver autowireCandidateResolver =
			defaultListableBeanFactory.getAutowireCandidateResolver();

		defaultListableBeanFactory.setAutowireCandidateResolver(
			new AKAutowireCandidateResolver(autowireCandidateResolver));

		for (String beanName :
				defaultListableBeanFactory.getBeanDefinitionNames()) {

			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(
				beanName);

			if (!(beanDefinition instanceof AbstractBeanDefinition)) {
				continue;
			}

			String className = beanDefinition.getBeanClassName();

			if (className == null) {
				continue;
			}

			try {
				AKBeanDefinition akBeanDefinition = new AKBeanDefinition(
					this, (AbstractBeanDefinition)beanDefinition,
					beanName, getServiceRegistry());

				defaultListableBeanFactory.removeBeanDefinition(beanName);

				defaultListableBeanFactory.registerBeanDefinition(
					beanName, akBeanDefinition);
			}
			catch (Exception e) {
				if (_log.isLoggable(Level.SEVERE)) {
					_log.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * Post process before initialization.
	 *
	 * @param bean the bean
	 * @param beanName the bean id
	 * @return the object
	 * @throws BeansException the beans exception
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName)
		throws BeansException {

		return bean;
	}

	/**
	 * excludeBeanNames (Optional):
	 *
	 * Provide a list of bean names that should be excluded. Names may be
	 * prefixed OR suffixed with a * for simple matching.
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
	 * prefixed OR suffixed with a * for simple matching.
	 *
	 * @param excludeClassNames the new ignored class names
	 */
	public void setExcludeClassNames(List<String> excludeClassNames) {
		_excludeClassNames = excludeClassNames;
	}

	/**
	 * includeBeanNames (Optional):
	 *
	 * Provide a list of bean names that should be included. Names may be
	 * prefixed OR suffixed with a * for simple matching. The default
	 * behavior, if no list is provided, is to include no beans.
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
	 * prefixed OR suffixed with a * for simple matching. The default
	 * behavior, if no list is provided, is to include no beans.
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
	 * serviceRegistry (Required):
	 *
	 * Provide an instance of com.liferay.arkadiko.sr.ServiceRegistry into which
	 * the spring beans, matching the rules below, will be published. In
	 * turn, from the service registry, it will be possible to provide services
	 * that implement or override beans that are needed or used in this spring
	 * context.
	 *
	 * @param serviceRegistry the service registry
	 */
	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		_serviceRegistry = serviceRegistry;
	}

	/**
	 * trackService (Optional):
	 *
	 * Whether to only register matching beans as services or to also create
	 * trackers for them.
	 *
	 * @param trackService only register as a service or also track
	 */
	public void setTrackService(boolean trackService) {
		_trackService = trackService;
	}

	/**
	 * Exclude bean by bean name.
	 *
	 * @param beanName the bean id
	 * @return true, if successful
	 */
	protected boolean excludeBeanByBeanName(String beanName) {
		for (String excludedbeanName : _excludeBeanNames) {
			if (beanName.equals(excludedbeanName)) {
				return true;
			}
			else if (excludedbeanName.startsWith(Constants.STAR) &&
					 beanName.endsWith(excludedbeanName.substring(1))) {

				return true;
			}
			else if (excludedbeanName.endsWith(Constants.STAR) &&
					 beanName.startsWith(
						 excludedbeanName.substring(
							 0, excludedbeanName.length() - 1))) {

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
		for (String excludedClassName : _excludeClassNames) {
			if (className.equals(excludedClassName)) {
				return true;
			}
			else if (excludedClassName.startsWith(Constants.STAR) &&
					 className.endsWith(excludedClassName.substring(1))) {

				return true;
			}
			else if (excludedClassName.endsWith(Constants.STAR) &&
					 className.startsWith(
						 excludedClassName.substring(
							 0, excludedClassName.length() - 1))) {

				return true;
			}
		}

		return false;
	}

	/**
	 * Try to get an Arkadiko bean which is a proxy backed by an OSGi service
	 * tracker which is either going to wrap an existing spring bean, or wait
	 * for one to be registered. This only happens when the bean is declared to
	 * be wrapped by name or by class/interface.
	 *
	 * @param rootBeanDefinition
	 * @param beanName
	 * @return a bean or null
	 */
	protected Object getServiceBean(
		RootBeanDefinition rootBeanDefinition, String beanName) {

		BeanDefinition originatingBeanDefinition =
			rootBeanDefinition.getOriginatingBeanDefinition();

		if ((originatingBeanDefinition != null) &&
			(originatingBeanDefinition instanceof AKBeanDefinition)) {

			AKBeanDefinition akBeanDefinition =
				(AKBeanDefinition)originatingBeanDefinition;

			return akBeanDefinition.getProxy();
		}
		else if (rootBeanDefinition.hasBeanClass()) {
			Class<?> clazz = rootBeanDefinition.getBeanClass();

			if (clazz.isInterface()) {
				AKBeanDefinition akBeanDefinition = new AKBeanDefinition(
					this, rootBeanDefinition, beanName, getServiceRegistry());

				return akBeanDefinition.getProxy();
			}
		}

		return null;
	}

	/**
	 * Include bean by bean name.
	 *
	 * @param beanName the bean id
	 * @return true, if successful
	 */
	protected boolean includeBeanByBeanName(String beanName) {
		for (String includedBeanName : _includeBeanNames) {
			if (beanName.equals(includedBeanName)) {
				return true;
			}
			else if (includedBeanName.startsWith(Constants.STAR) &&
					 beanName.endsWith(includedBeanName.substring(1))) {

				return true;
			}
			else if (includedBeanName.endsWith(Constants.STAR) &&
					 beanName.startsWith(
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
		for (String includedClassName : _includeClassNames) {
			if (className.equals(includedClassName)) {
				return true;
			}
			else if (includedClassName.startsWith(Constants.STAR) &&
					 className.endsWith(includedClassName.substring(1))) {

				return true;
			}
			else if (includedClassName.endsWith(Constants.STAR) &&
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
	 * @param beanName the bean id
	 * @param interfaces the interfaces
	 * @return true, if successful
	 */
	protected boolean ignoreBean(
		String beanClass, String beanName, Class<?>[] interfaces) {

		// Automatically exclude anonymous beans or beans that don't implement
		// any interfaces

		if ((beanName.indexOf(Constants.POUND) != -1) ||
			(beanName.equals(Constants.INNER_BEAN)) ||
			(interfaces.length <= 0)) {

			return true;
		}

		if (!excludeBeanByBeanName(beanName) &&
			!excludeBeanByClassName(beanClass) &&
			(includeBeanByBeanName(beanName) ||
			 includeBeanByClassName(beanClass))) {

			return false;
		}

		return true;
	}

	private static Logger _log = Logger.getLogger(
		AKBeanPostProcessor.class.getName());

	private List<String> _excludeBeanNames;
	private List<String> _excludeClassNames;
	private List<String> _includeBeanNames;
	private List<String> _includeClassNames;
	private int _order = 20;
	private boolean _trackService = true;
	private ServiceRegistry _serviceRegistry;

	private class AKAutowireCandidateResolver
		implements AutowireCandidateResolver {

		AKAutowireCandidateResolver(
			AutowireCandidateResolver autowireCandidateResolver) {

			_autowireCandidateResolver = autowireCandidateResolver;
		}

		public boolean isAutowireCandidate(
			BeanDefinitionHolder beanDefinitionHolder,
			DependencyDescriptor descriptor) {

			return _autowireCandidateResolver.isAutowireCandidate(
				beanDefinitionHolder, descriptor);
		}

		public Object getSuggestedValue(DependencyDescriptor descriptor) {
			Object service = _autowireCandidateResolver.getSuggestedValue(
				descriptor);

			if (service != null) {
				return service;
			}

			try {
				return _serviceRegistry.registerBeanAsService(
					null, descriptor.getDependencyName(),
					new Class<?>[] {descriptor.getDependencyType()}, true);
			}
			catch (Exception e) {
				if (_log.isLoggable(Level.SEVERE)) {
					_log.log(Level.SEVERE, e.getMessage(), e);
				}
			}

			return null;
		}

		private AutowireCandidateResolver _autowireCandidateResolver;

	}

}