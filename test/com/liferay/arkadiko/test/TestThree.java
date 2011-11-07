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
public class TestThree extends BaseTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new ClassPathXmlApplicationContext("META-INF/test-three.xml");

		_context.registerShutdownHook();
	}

	public void testBeanCount() {
		assertEquals(7, _context.getBeanDefinitionCount());
	}

	public void testDeployBundleWithImplementation() throws Exception {
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

		HasDependencyOnInterfaceOne beanTwo =
			(HasDependencyOnInterfaceOne)_context.getBean(
				HasDependencyOnInterfaceOne.class.getName().concat("_TWO"));

		interfaceOne = beanTwo.getInterfaceOne();

		assertFalse(
			interfaceOne.methodOne().equals(InterfaceOneImpl.class.getName()));

		HasDependencyOnInterfaceOne beanThree =
			(HasDependencyOnInterfaceOne)_context.getBean(
				HasDependencyOnInterfaceOne.class.getName().concat("_THREE"));

		interfaceOne = beanThree.getInterfaceOne();

		assertTrue(
			interfaceOne.methodOne().equals(InterfaceOneImpl.class.getName()));

		installedBundle.uninstall();

		interfaceOne = bean.getInterfaceOne();

		assertEquals(
			interfaceOne.methodOne(), InterfaceOneImpl.class.getName());

		interfaceOne = beanTwo.getInterfaceOne();

		assertEquals(
			interfaceOne.methodOne(), InterfaceOneImpl.class.getName());

		interfaceOne = beanThree.getInterfaceOne();

		assertEquals(
			interfaceOne.methodOne(), InterfaceOneImpl.class.getName());
	}

	@Override
	protected void tearDown() throws Exception {
		_context.close();

		super.tearDown();
	}

	private AbstractApplicationContext _context;

}