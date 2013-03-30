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

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
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

	public static Bundle getBundle(
		BundleContext bundleContext, File bundleFile) throws IOException {

		JarFile jarFile = new JarFile(bundleFile);

		Manifest manifest = jarFile.getManifest();

		jarFile.close();

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
			Version curBundleVersion = Version.parseVersion(
				bundle.getVersion().toString());

			if (bundleSymbolicName.equals(bundle.getSymbolicName()) &&
				bundleVersion.equals(curBundleVersion)) {

				return bundle;
			}
		}

		return null;
	}

	public static Framework init(Map<String, String> properties)
		throws Exception {

		Iterator<FrameworkFactory> iterator = ServiceLoader.load(
			FrameworkFactory.class).iterator();

		FrameworkFactory frameworkFactory = iterator.next();

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

	public static boolean isFragment(Bundle bundle) {
		Dictionary<String,String> headers = bundle.getHeaders();

		Enumeration<String> keys = headers.keys();

		while (keys.hasMoreElements()) {
			if (keys.nextElement().equals(Constants.FRAGMENT_HOST)) {
				return true;
			}
		}

		return false;
	}

	protected static void installBundles(
			BundleContext bundleContext, Map<String, String> properties)
		throws BundleException, IOException {

		String projectDir = properties.get("project.dir");
		String bundlesToInstall = properties.get("bundles.to.install");

		String[] bundlePaths = bundlesToInstall.split(",");

		for (String bundlePath : bundlePaths) {
			File bundleFile = new File(projectDir + "/" + bundlePath.trim());

			Bundle bundle = getBundle(bundleContext, bundleFile);

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
				if ((curBundle.getState() == Bundle.ACTIVE) ||
					isFragment(curBundle)) {

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