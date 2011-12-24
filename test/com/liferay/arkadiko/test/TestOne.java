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

import com.liferay.arkadiko.AKBeanPostProcessor;
import com.liferay.arkadiko.test.util.BaseTest;

import org.osgi.framework.launch.Framework;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <a href="TestTest.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class TestOne extends BaseTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_context = new ClassPathXmlApplicationContext("META-INF/test-one.xml");

		_context.registerShutdownHook();
	}

	public void testAKBeanPostProcessorNotNull() {
		AKBeanPostProcessor akBeanPostProcessor =
			(AKBeanPostProcessor)_context.getBean(
				AKBeanPostProcessor.class.getName());

		assertNotNull("AKBeanPostProcessor is null", akBeanPostProcessor);
	}

	public void testFrameworkNotNull() {
		Framework framework = (Framework)_context.getBean("framework");

		assertNotNull("Framework is null", framework);
	}

	@Override
	protected void tearDown() throws Exception {
		_context.close();

		super.tearDown();
	}

	private AbstractApplicationContext _context;

}