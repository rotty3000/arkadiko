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

package com.liferay.arkadiko.osgi;

import com.liferay.arkadiko.osgi.internal.Constants;
import com.liferay.arkadiko.osgi.internal.ServiceTrackerInvocationHandler;
import com.liferay.arkadiko.sr.ServiceRegistry;

import java.lang.reflect.Proxy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.framework.InvalidSyntaxException;

/**
 * <a href="OSGiServiceRegistry.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class OSGiServiceRegistry implements ServiceRegistry {

	public OSGiServiceRegistry(BundleContext bundleContext) {
		_bundleContext = bundleContext;
	}

	public Map<String, Object> getExtraBeanProperties() {
		if (_extraBeanProperties == null) {
			_extraBeanProperties = Collections.emptyMap();
		}

		return _extraBeanProperties;
	}

	/**
	 * Checks if is strict matching.
	 *
	 * @return true, if is strict matching
	 */
	public boolean isStrictMatching() {
		return _strictMatching;
	}

	public Object registerBeanAsService(
		Object bean, String beanName, Class<?>[] interfaces,
		boolean trackService) throws Exception {

		if (interfaces.length <= 0) {
			return bean;
		}

		List<String> names = new ArrayList<String>();

		for (Class<?> interfaceClass : interfaces) {
			names.add(interfaceClass.getName());
		}

		if (bean != null) {
			Hashtable<String,Object> properties =
				new Hashtable<String, Object>();

			properties.put(Constants.BEAN_ID, beanName);
			properties.put(Constants.ORIGINAL_BEAN, Boolean.TRUE);

			addExtraBeanProperties(properties);

			_bundleContext.registerService(
				names.toArray(new String[names.size()]), bean, properties);
		}

		if (!trackService) {
			return bean;
		}

		Filter filter = createFilter(_bundleContext, beanName, interfaces);

		ServiceTrackerInvocationHandler serviceTrackerInvocationHandler =
			new ServiceTrackerInvocationHandler(
				_bundleContext, filter, bean);

		serviceTrackerInvocationHandler.open(true);

		return Proxy.newProxyInstance(
			getClass().getClassLoader(), interfaces,
			serviceTrackerInvocationHandler);
	}

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
	public void setStrictMatching(boolean strictMatching) {
		_strictMatching = strictMatching;
	}

	/**
	 * extraBeanProperties (Optional):
	 *
	 * Provide a Map (<util:map/>) of properties to be added to beans published
	 * to the service registry.
	 *
	 * @param extraBeanProperties the extra bean properties
	 */
	public void setExtraBeanProperties(
		Map<String, Object> extraBeanProperties) {

		_extraBeanProperties = extraBeanProperties;
	}

	protected void addExtraBeanProperties(Hashtable<String,Object> properties) {
		for (Map.Entry<String, Object> entry :
				getExtraBeanProperties().entrySet()) {

			properties.put(entry.getKey(), entry.getValue());
		}
	}

	protected Filter createFilter(
			BundleContext bundleContext, String beanName,
			Class<?>[] interfaces)
		throws InvalidSyntaxException {

		StringBuffer sb = new StringBuffer((interfaces.length * 5) + 10);

		sb.append(Constants.OPEN_PAREN_AND_AMP);

		if (!isStrictMatching() && (interfaces.length > 1)) {
			sb.append(Constants.OPEN_PAREN_AND_PIPE);
		}

		for (Class<?> clazz : interfaces) {
			sb.append(Constants.OPEN_PAREN);
			sb.append(Constants.OBJECT_CLASS);
			sb.append(Constants.EQUAL);
			sb.append(clazz.getName());
			sb.append(Constants.CLOSE_PAREN);
		}

		if (!isStrictMatching() && (interfaces.length > 1)) {
			sb.append(Constants.CLOSE_PAREN);
		}

		sb.append(Constants.OPEN_PAREN);
		sb.append(Constants.BEAN_ID);
		sb.append(Constants.EQUAL);
		sb.append(beanName);
		sb.append(Constants.CP_OP_EX_OP);
		sb.append(Constants.ORIGINAL_BEAN);
		sb.append(Constants.EQ_STAR_CP_CP_CP);

		return bundleContext.createFilter(sb.toString());
	}

	private BundleContext _bundleContext;
	private Map<String, Object> _extraBeanProperties;
	private boolean _strictMatching = false;

}