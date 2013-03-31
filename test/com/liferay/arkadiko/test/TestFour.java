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

package com.liferay.arkadiko.test;

import com.liferay.arkadiko.osgi.OSGiFrameworkFactory;
import com.liferay.arkadiko.test.beans.HasDependencyOnInterfaceOne;
import com.liferay.arkadiko.test.interfaces.InterfaceOne;
import com.liferay.arkadiko.test.util.BaseTest;

import java.lang.reflect.Proxy;

import org.osgi.framework.Bundle;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <a href="TestTest.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class TestFour extends BaseTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new ClassPathXmlApplicationContext("META-INF/test-four.xml");

		_context.registerShutdownHook();
	}

	public void testExcludeBeanName() throws Exception {
		Bundle installedBundle = installAndStart(
			_context, "/bundles/bundle-one/bundle-one.jar");

		InterfaceOne interfaceOne = null;

		HasDependencyOnInterfaceOne bean =
			(HasDependencyOnInterfaceOne)_context.getBean(
				HasDependencyOnInterfaceOne.class.getName());

		try {
			interfaceOne = bean.getInterfaceOne();

			assertFalse(
				"interfaceOne is a proxy",
				Proxy.isProxyClass(interfaceOne.getClass()));
		}
		finally {
			installedBundle.uninstall();
		}

		interfaceOne = bean.getInterfaceOne();

		assertFalse(
			"interfaceOne is a proxy",
			Proxy.isProxyClass(interfaceOne.getClass()));
	}

	@Override
	protected void tearDown() throws Exception {
		_context.close();

		OSGiFrameworkFactory.stop();

		super.tearDown();
	}

	private AbstractApplicationContext _context;

}