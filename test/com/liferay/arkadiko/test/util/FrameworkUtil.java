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

package com.liferay.arkadiko.test.util;


import java.io.File;
import java.io.FileInputStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.service.log.LogReaderService;

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

		_initLogListener();

		BundleContext bundleContext = _framework.getBundleContext();

		String bundlePath = System.getProperty("project.dir");

		File bundleFile = new File(
			bundlePath + "/lib/org.eclipse.equinox.ds_1.3.0.v20110502.jar");

		Bundle bundle = bundleContext.installBundle(
			bundleFile.getAbsolutePath(), new FileInputStream(bundleFile));

		bundleFile = new File(
			bundlePath + "/lib/org.eclipse.equinox.util_1.0.300.v20110502.jar");

		bundleContext.installBundle(
			bundleFile.getAbsolutePath(), new FileInputStream(bundleFile));

		bundleFile = new File(
			bundlePath + "/lib/org.eclipse.osgi.services_3.3.0.v20110513.jar");

		bundleContext.installBundle(
			bundleFile.getAbsolutePath(), new FileInputStream(bundleFile));

		_framework.start();

		for (Bundle curBundle : bundleContext.getBundles()) {
			try {
				curBundle.start();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void stop() throws Exception {
		_framework.stop();
	}

	protected static Map<String, String> _buildProperties() {
		Map<String, String> properties = new HashMap<String, String>();

		properties.put(
			Constants.FRAMEWORK_BEGINNING_STARTLEVEL, String.valueOf(2));
		properties.put(
			Constants.FRAMEWORK_BUNDLE_PARENT,
			Constants.FRAMEWORK_BUNDLE_PARENT_APP);
		properties.put(Constants.FRAMEWORK_STORAGE, "data");

		StringBuffer sb = new StringBuffer();

		// Need to export the packages of bean interfaces we want to resolve
		// services for from OSGi.

		sb.append("com.liferay.arkadiko.test.interfaces");

		properties.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, sb.toString());

		return properties;
	}

	private static void _initLogListener() {
		BundleContext bundleContext = _framework.getBundleContext();

		ServiceReference<LogReaderService> logReaderServiceReference =
			bundleContext.getServiceReference(LogReaderService.class);

		LogReaderService logReaderService = bundleContext.getService(
			logReaderServiceReference);

		if (logReaderService == null) {
			return;
		}

		logReaderService.addLogListener(new LogListener());
	}

	private static Framework _framework;

}