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

package com.liferay.arkadiko.util;

import aQute.libg.header.OSGiHeader;
import aQute.libg.version.Version;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * <a href="FrameworkUtil.java.html"><b><i>View Source</i></b></a>
 *
 * @author Raymond Augé
 */
public class AKFrameworkFactory {

	public static Framework init(Map<String, String> properties)
		throws Exception {

		List<FrameworkFactory> frameworkFactories = AKServiceLoader.load(
			FrameworkFactory.class);

		if (frameworkFactories.isEmpty()) {
			return null;
		}

		FrameworkFactory frameworkFactory = frameworkFactories.get(0);

		Framework framework = frameworkFactory.newFramework(properties);

		framework.init();

		BundleContext bundleContext = framework.getBundleContext();

		bundleContext.registerService(
			LogFactory.class, LogFactory.getFactory(),
			new Hashtable<String, Object>());

		installBundles(bundleContext, properties);

		framework.start();

		startBundles(bundleContext, properties);

		return framework;
	}

	protected static Bundle getBundle(
		BundleContext bundleContext, Manifest manifest) {

		Attributes attributes = manifest.getMainAttributes();

		String bundleSymbolicNameAttribute = attributes.getValue(
			Constants.BUNDLE_SYMBOLICNAME);

		Map<String, Map<String, String>> bundleSymbolicNamesMap =
			OSGiHeader.parseHeader(bundleSymbolicNameAttribute);

		Set<String> bundleSymbolicNamesSet = bundleSymbolicNamesMap.keySet();

		Iterator<String> bundleSymbolicNamesIterator =
			bundleSymbolicNamesSet.iterator();

		String bundleSymbolicName = bundleSymbolicNamesIterator.next();

		String bundleVersionAttribute = attributes.getValue(
			Constants.BUNDLE_VERSION);

		Version bundleVersion = Version.parseVersion(bundleVersionAttribute);

		for (Bundle bundle : bundleContext.getBundles()) {
			if (bundleSymbolicName.equals(bundle.getSymbolicName()) &&
				bundleVersion.equals(bundle.getVersion())) {

				return bundle;
			}
		}

		return null;
	}

	protected static void installBundles(
			BundleContext bundleContext, Map<String, String> properties)
		throws BundleException, IOException {

		String projectDir = properties.get("project.dir");
		String bundlesToInstall = properties.get("bundles.to.install");

		String[] bundlePaths = bundlesToInstall.split(",");

		for (String bundlePath : bundlePaths) {
			File bundleFile = new File(projectDir + "/" + bundlePath.trim());

			JarFile jarFile = new JarFile(bundleFile);

			Bundle bundle = getBundle(bundleContext, jarFile.getManifest());

			jarFile.close();

			if (bundle != null) {
				continue;
			}

			FileInputStream fileInputStream = new FileInputStream(bundleFile);

			try {
				bundleContext.installBundle(
					bundleFile.getAbsolutePath(), fileInputStream);
			}
			catch (BundleException be) {
				_log.error(be, be);
			}
			finally {
				fileInputStream.close();
			}
		}
	}

	protected static void startBundles(
		BundleContext bundleContext, Map<String, String> properties) {

		String bundlesForceStart = properties.get("bundles.force.start");

		if (Boolean.TRUE.toString().equals(bundlesForceStart)) {
			Bundle[] bundles = bundleContext.getBundles();

			for (Bundle curBundle : bundles) {
				if (curBundle.getState() == Bundle.ACTIVE) {
					continue;
				}

				try {
					curBundle.start();
				}
				catch (Exception e) {
					_log.error(e, e);
				}
			}
		}
	}

	private static Log _log = LogFactory.getLog(AKFrameworkFactory.class);

}