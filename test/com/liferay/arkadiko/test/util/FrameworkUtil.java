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

import java.util.List;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * <a href="FrameworkUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class FrameworkUtil {

	public static Framework init(Map<String, String> properties)
		throws Exception {

		List<FrameworkFactory> frameworkFactories = ServiceLoader.load(
			FrameworkFactory.class);

		if (frameworkFactories.isEmpty()) {
			return null;
		}

		FrameworkFactory frameworkFactory = frameworkFactories.get(0);

		Framework framework = frameworkFactory.newFramework(properties);

		framework.init();

		BundleContext bundleContext = framework.getBundleContext();

		String bundlesPath = properties.get("bundles.dir");
		String bundlesToInstall = properties.get("bundles.to.install");

		String[] bundleNames = bundlesToInstall.split(",");

		for (String bundleName : bundleNames) {
			File bundleFile = new File(bundlesPath + "/" + bundleName);

			Bundle bundle = bundleContext.getBundle(
				bundleFile.getAbsolutePath());

			if (bundle != null) {
				continue;
			}

			bundleContext.installBundle(
				bundleFile.getAbsolutePath(), new FileInputStream(bundleFile));
		}

		framework.start();

		String bundlesForceStart = properties.get("bundles.force.start");

		if (Boolean.TRUE.toString().equals(bundlesForceStart)) {
			for (Bundle curBundle : bundleContext.getBundles()) {
				try {
					curBundle.start();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return framework;
	}

}