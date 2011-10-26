/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
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

package com.liferay.arkadiko.test.util;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * <a href="FrameworkUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class FrameworkUtil {

	public static Framework getFramework() {
		return _framework;
	}

	public static void init() throws Exception {
		List<FrameworkFactory> frameworkFactories = ServiceLoader.load(
			FrameworkFactory.class);

		if (frameworkFactories.isEmpty()) {
			return;
		}

		FrameworkFactory frameworkFactory = frameworkFactories.get(0);

		Map<String, String> properties = _buildProperties();

		_framework = frameworkFactory.newFramework(properties);

		_framework.init();

		_framework.start();
	}

	public static void stop() throws Exception {
		_framework.stop();
	}

	protected static Map<String, String> _buildProperties() {
		Map<String, String> properties = new HashMap<String, String>();

		properties.put(
			Constants.FRAMEWORK_BEGINNING_STARTLEVEL, String.valueOf(1));
		properties.put(
			Constants.FRAMEWORK_BUNDLE_PARENT,
			Constants.FRAMEWORK_BUNDLE_PARENT_APP);
		properties.put(Constants.FRAMEWORK_STORAGE, "data");

		StringBuffer sb = new StringBuffer();

		// Need to export the packages of bean interfaces we want to resolve
		// services for from OSGi.

		properties.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, sb.toString());

		return properties;
	}

	private static Framework _framework;

}