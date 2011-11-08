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

package com.liferay.arkadiko.test;

import com.liferay.arkadiko.test.beans.HasDependencyOnInterfaceOne;
import com.liferay.arkadiko.test.impl.InterfaceOneImpl;
import com.liferay.arkadiko.test.interfaces.InterfaceOne;
import com.liferay.arkadiko.test.util.BaseTest;

import java.io.File;
import java.io.FileInputStream;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <a href="TestTest.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class TestSix extends BaseTest {

	public static void main(String[] args) throws Exception {
		TestSix testSix = new TestSix();

		testSix.setUp();
		testSix.testIgnoreByName();
		testSix.tearDown();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new ClassPathXmlApplicationContext("META-INF/test-six.xml");

		_context.registerShutdownHook();
	}

	public void testBeanCount() {
		assertEquals(5, _context.getBeanDefinitionCount());
	}

	public void testIgnoreByName() throws Exception {
		Framework framework = (Framework)_context.getBean("framework");

		BundleContext bundleContext = framework.getBundleContext();

		File bundleOne = new File(
			getProjectDir() + "/bundles/bundle-one/bundle-one.jar");

		Bundle installedBundle = bundleContext.installBundle(
			bundleOne.getAbsolutePath(), new FileInputStream(bundleOne));

		installedBundle.start();

		HasDependencyOnInterfaceOne bean =
			(HasDependencyOnInterfaceOne)_context.getBean(
				HasDependencyOnInterfaceOne.class.getName());

		InterfaceOne interfaceOne = bean.getInterfaceOne();

		assertFalse(
			interfaceOne.methodOne().equals(InterfaceOneImpl.class.getName()));

		String testString = "test string";

		interfaceOne.setValue(testString);

		assertEquals(interfaceOne.getValue(), testString);

		installedBundle.uninstall();

		Exception e = null;

		try {
			interfaceOne.getValue();
		}
		catch (Exception e1) {
			e = e1;
		}

		assertNotNull(e);

		assertTrue(e instanceof IllegalStateException);
	}

	@Override
	protected void tearDown() throws Exception {
		_context.close();

		super.tearDown();
	}

	private AbstractApplicationContext _context;

}