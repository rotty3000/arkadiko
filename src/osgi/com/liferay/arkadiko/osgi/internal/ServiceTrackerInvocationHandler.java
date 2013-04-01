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

package com.liferay.arkadiko.osgi.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Raymond Augé
 */
public class ServiceTrackerInvocationHandler
	extends ServiceTracker implements InvocationHandler {

	/**
	 * Instantiates a new ServiceTrackerInvocationHandler.
	 *
	 * @param bundleContext the context
	 * @param filter the filter
	 * @param bean the bean
	 */
	public ServiceTrackerInvocationHandler(
		BundleContext bundleContext, Filter filter, Object bean) {

		super(bundleContext, filter, null);

		_currentService = bean;
		_originalService = bean;
	}

	@Override
	public Object addingService(ServiceReference serviceReference) {
		_currentService = context.getService(serviceReference);

		return _currentService;
	}

	public Object getCurrentService() {
		return _currentService;
	}

	public Object getOriginalService() {
		return _originalService;
	}

	/**
	 * Invoke.
	 *
	 * @param proxy the proxy
	 * @param method the method
	 * @param arguments the arguments
	 * @return the object
	 * @throws Throwable the throwable
	 */
	public Object invoke(Object proxy, Method method, Object[] arguments)
		throws Throwable {

		if (_currentService == null) {
			throw new IllegalStateException("called too early!");
		}

		try {
			return method.invoke(_currentService, arguments);
		}
		catch (InvocationTargetException ite) {
			throw ite.getCause();
		}
	}

	@Override
	public void modifiedService(
		ServiceReference serviceReference, Object oldService) {

		_currentService = context.getService(serviceReference);
	}

	@Override
	public void removedService(
		ServiceReference serviceReference, Object service) {

		_currentService = _originalService;

		context.ungetService(serviceReference);
	}

	private Object _currentService;
	private Object _originalService;

}