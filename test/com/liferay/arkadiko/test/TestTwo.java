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

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <a href="TestTest.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class TestTwo extends BaseTest {

	public void test() throws Exception {
		ApplicationContext context = new ClassPathXmlApplicationContext(
			"META-INF/test-two.xml");

		assertEquals(5, context.getBeanDefinitionCount());

		HasDependencyOnInterfaceOne bean =
			(HasDependencyOnInterfaceOne)context.getBean(
				HasDependencyOnInterfaceOne.class.getName());

		InterfaceOne interfaceOne = bean.getInterfaceOne();

		assertNotNull(interfaceOne.toString(), interfaceOne);
		assertEquals(
			interfaceOne.toString(), interfaceOne.methodOne(),
			InterfaceOneImpl.class.getName());

		Framework framework = (Framework)context.getBean("framework");

		BundleContext bundleContext = framework.getBundleContext();

		File bundleOne = new File(
			getProjectDir() + "/bundles/bundle-one/bundle-one.jar");

		Bundle installedBundle = bundleContext.installBundle(
			bundleOne.getAbsolutePath(), new FileInputStream(bundleOne));

		installedBundle.start();

		assertFalse(
			interfaceOne.toString(),
			interfaceOne.methodOne().equals(InterfaceOneImpl.class.getName()));

		installedBundle.uninstall();

		assertEquals(
			interfaceOne.toString(), interfaceOne.methodOne(),
			InterfaceOneImpl.class.getName());
	}

}