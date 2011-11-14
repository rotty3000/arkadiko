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

import java.util.LinkedList;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

/**
 * @author Raymond Augé
 */
public class AKServiceTrackerInvocationHandler
	extends ServiceTracker implements InvocationHandler {

	/**
	 * Instantiates a new AKServiceTrackerInvocationHandler.
	 *
	 * @param context the context
	 * @param filter the filter
	 * @param bean the bean
	 */
	public AKServiceTrackerInvocationHandler(
		BundleContext context, Filter filter, Object bean) {

		super(context, filter, null);

		_currentService = bean;
		_originalService = bean;
	}

	/**
	 * Adding service.
	 *
	 * @param reference the reference
	 * @return the object
	 */
	@Override
	public Object addingService(ServiceReference reference) {
		Object service = super.addingService(reference);

		if (service == _originalService) {
			return service;
		}

		while (!_methodQueue.isEmpty()) {
			MethodInvocation methodInvocation = _methodQueue.poll();

			Method method = methodInvocation.getMethod();
			Object[] arguments = methodInvocation.getArguments();

			try {
				return method.invoke(service, arguments);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		_currentService = service;

		return service;
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
			Class<?> returnType = method.getReturnType();

			if (returnType.equals(Void.TYPE)) {
				_methodQueue.push(new MethodInvocation(method, arguments));

				return null;
			}

			throw new IllegalStateException("called too early!");
		}

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

	/**
	 * Removed service.
	 *
	 * @param reference the reference
	 * @param service the service
	 */
	@Override
	public void removedService(ServiceReference reference, Object service) {
		_currentService = _originalService;

		super.removedService(reference, service);
	}

	private Object _currentService;
	private Object _originalService;
	private LinkedList<MethodInvocation> _methodQueue =
		new LinkedList<MethodInvocation>();

	private class MethodInvocation {

		public MethodInvocation(Method method, Object[] arguments) {
			_method = method;
			_arguments = arguments;
		}

		public Object[] getArguments() {
			return _arguments;
		}

		public Method getMethod() {
			return _method;
		}

		private Method _method;
		private Object[] _arguments;

	}

}