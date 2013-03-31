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

package com.liferay.arkadiko.osgi;

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
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

/**
 * @author Raymond Augé
 */
public class OSGiFrameworkFactory {

	public static Bundle getBundle(File bundleFile) throws IOException {
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

		for (Bundle bundle : _bundleContext.getBundles()) {
			Version curBundleVersion = Version.parseVersion(
				bundle.getVersion().toString());

			if (bundleSymbolicName.equals(bundle.getSymbolicName()) &&
				bundleVersion.equals(curBundleVersion)) {

				return bundle;
			}
		}

		return null;
	}

	public static BundleContext init(Map<String, String> properties)
		throws Exception {

		_lock.lock();

		try {
			if (_bundleContext != null) {
				return _bundleContext;
			}

			Iterator<FrameworkFactory> iterator = ServiceLoader.load(
				FrameworkFactory.class).iterator();

			FrameworkFactory frameworkFactory = iterator.next();

			_framework = frameworkFactory.newFramework(properties);

			_framework.init();

			_bundleContext = _framework.getBundleContext();

			_bundleContext.registerService(
				LogFactory.class, LogFactory.getFactory(),
				new Hashtable<String, Object>());

			installBundles(properties);

			_framework.start();

			startBundles(properties);

			return _bundleContext;
		}
		finally {
			_lock.unlock();
		}
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

	public static void stop() throws Exception {
		_lock.lock();

		try {
			_framework.stop();

			_bundleContext = null;
			_framework = null;
		}
		finally {
			_lock.unlock();
		}
	}

	protected static void installBundles(Map<String, String> properties)
		throws BundleException, IOException {

		String projectDir = properties.get("project.dir");
		String bundlesToInstall = properties.get("bundles.to.install");

		String[] bundlePaths = bundlesToInstall.split(",");

		for (String bundlePath : bundlePaths) {
			File bundleFile = new File(projectDir + "/" + bundlePath.trim());

			Bundle bundle = getBundle(bundleFile);

			if (bundle != null) {
				continue;
			}

			FileInputStream fileInputStream = new FileInputStream(bundleFile);

			try {
				_bundleContext.installBundle(
					bundleFile.getAbsolutePath(), fileInputStream);
			}
			catch (BundleException be) {
				if (_log.isLoggable(Level.SEVERE)) {
					_log.log(Level.SEVERE, be.getMessage(), be);
				}
			}
			finally {
				fileInputStream.close();
			}
		}
	}

	protected static void startBundles(Map<String, String> properties) {
		String bundlesForceStart = properties.get("bundles.force.start");

		if (!Boolean.TRUE.toString().equals(bundlesForceStart)) {
			return;
		}

		Bundle[] bundles = _bundleContext.getBundles();

		for (Bundle curBundle : bundles) {
			if (((curBundle.getState() & Bundle.ACTIVE) == Bundle.ACTIVE) ||
				isFragment(curBundle)) {

				continue;
			}

			try {
				curBundle.start();
			}
			catch (Exception e) {
				if (_log.isLoggable(Level.SEVERE)) {
					_log.log(Level.SEVERE, e.getMessage(), e);
				}
			}
		}
	}

	private static BundleContext _bundleContext;
	private static Framework _framework;
	private static final ReentrantLock _lock = new ReentrantLock(true);

	private static Logger _log = Logger.getLogger(
		OSGiFrameworkFactory.class.getName());

}