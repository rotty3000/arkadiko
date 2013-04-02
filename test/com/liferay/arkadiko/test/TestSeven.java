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
import com.liferay.arkadiko.osgi.internal.ServiceTrackerInvocationHandler;
import com.liferay.arkadiko.test.beans.HasDependencyOnInterfaceOne;
import com.liferay.arkadiko.test.interfaces.InterfaceOne;
import com.liferay.arkadiko.test.util.BaseTest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.junit.Assert;

import org.osgi.framework.Bundle;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <a href="TestTest.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class TestSeven extends BaseTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new ClassPathXmlApplicationContext("META-INF/test-seven.xml");

		_context.registerShutdownHook();
	}

	public void testLoadOSGiOnlyDepedency() throws Exception {
		InterfaceOne interfaceOne = null;

		HasDependencyOnInterfaceOne bean =
			(HasDependencyOnInterfaceOne)_context.getBean(
				HasDependencyOnInterfaceOne.class.getName());

		interfaceOne = bean.getInterfaceOne();

		assertTrue(
			"interfaceOne is not a proxy",
			Proxy.isProxyClass(interfaceOne.getClass()));

		InvocationHandler ih = Proxy.getInvocationHandler(interfaceOne);

		assertTrue(
			"ih not instanceof AKServiceTrackerInvocationHandler",
			ih instanceof ServiceTrackerInvocationHandler);

		ServiceTrackerInvocationHandler akih =
			(ServiceTrackerInvocationHandler)ih;

		assertTrue(
			"currentService not equal to originalService",
			akih.getCurrentService() == akih.getOriginalService());

		try {
			interfaceOne.getValue();

			Assert.fail("Should have thrown exception");
		}
		catch (Exception e) {
			assertTrue(e instanceof IllegalStateException);
		}

		// Install the bundle with the dependency impl

		Bundle installedBundle = installAndStart(
			_context, "/bundles/bundle-one/bundle-one.jar");

		try {
			// Test to see that the dependency impl is used

			interfaceOne = bean.getInterfaceOne();

			assertTrue(
				"interfaceOne is not a proxy",
				Proxy.isProxyClass(interfaceOne.getClass()));

			ih = Proxy.getInvocationHandler(interfaceOne);

			assertTrue(
				"ih not instanceof AKServiceTrackerInvocationHandler",
				ih instanceof ServiceTrackerInvocationHandler);

			akih = (ServiceTrackerInvocationHandler)ih;

			assertFalse(
				"currentService is equal to originalService",
				akih.getCurrentService() == akih.getOriginalService());

			String testString = "test string";

			interfaceOne.setValue(testString);

			assertEquals(
				"dependency impl returns the incorrect value",
				interfaceOne.getValue(), testString);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			installedBundle.uninstall();
		}

		interfaceOne = bean.getInterfaceOne();

		assertTrue(
			"interfaceOne is not a proxy",
			Proxy.isProxyClass(interfaceOne.getClass()));

		ih = Proxy.getInvocationHandler(interfaceOne);

		assertTrue(
			"ih not instanceof AKServiceTrackerInvocationHandler",
			ih instanceof ServiceTrackerInvocationHandler);

		akih = (ServiceTrackerInvocationHandler)ih;

		assertTrue(
			"currentService is equal to originalService",
			akih.getCurrentService() == akih.getOriginalService());

		try {
			interfaceOne.getValue();

			Assert.fail("Should have thrown an exception");
		}
		catch (Exception e) {
			assertTrue(e instanceof IllegalStateException);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		_context.close();

		OSGiFrameworkFactory.stop();

		super.tearDown();
	}

	private AbstractApplicationContext _context;

}