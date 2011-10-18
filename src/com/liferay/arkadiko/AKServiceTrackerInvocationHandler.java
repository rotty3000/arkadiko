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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Raymond Augé
 */
public class AKServiceTrackerInvocationHandler
	extends ServiceTracker implements InvocationHandler {

	public AKServiceTrackerInvocationHandler(
		BundleContext context, Filter filter, Object bean) {

		super(context, filter, null);

		_currentService = bean;
		_originalService = bean;
	}

	public AKServiceTrackerInvocationHandler(
		BundleContext bundleContext, ServiceReference serviceReference,
		Object bean) {

		super(bundleContext, serviceReference, null);

		_currentService = bean;
		_originalService = bean;
	}

	public AKServiceTrackerInvocationHandler(
		BundleContext context, String clazz, Object bean) {

		super(context, clazz, null);

		_currentService = bean;
		_originalService = bean;
	}

	@Override
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);

		Object originalBean = reference.getProperty("original.bean");

		if ((service == _originalService) || (originalBean != null)) {
			return service;
		}

		_currentService = service;

		return service;
	}

	public Object invoke(Object proxy, Method method, Object[] arguments)
		throws Throwable {

		try {
			return method.invoke(_currentService, arguments);
		}
		catch (InvocationTargetException ite) {
			throw ite.getCause();
		}
		catch (Exception e) {
			throw e;
		}
	}

	@Override
	public void removedService(ServiceReference reference, Object service) {
		_currentService = _originalService;

		super.removedService(reference, service);
	}

	private Object _currentService;
	private Object _originalService;

}