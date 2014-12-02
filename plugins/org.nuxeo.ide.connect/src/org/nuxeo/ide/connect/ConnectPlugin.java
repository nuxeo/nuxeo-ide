/*
 * (C) Copyright 2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Julien Carsique
 *
 */
package org.nuxeo.ide.connect;

import java.io.File;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import org.nuxeo.ide.common.forms.PreferencesFormData;

/**
 * The activator class controls the plug-in life cycle
 */
public class ConnectPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.nuxeo.ide.connect"; //$NON-NLS-1$

    // The shared instance
    private static ConnectPlugin plugin;

    private static StudioProvider studioProvider;

    /**
     * The constructor
     */
    public ConnectPlugin() {
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        if (studioProvider != null) {
            studioProvider.dispose();
            studioProvider = null;
        }
        super.stop(context);
    }

    public BundleContext getContext() {
        return getBundle().getBundleContext();
    }

    public PreferencesFormData getPreferences() {
        return new PreferencesFormData(InstanceScope.INSTANCE.getNode(PLUGIN_ID));
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static ConnectPlugin getDefault() {
        return plugin;
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put("icons/studio_project.gif",
                ImageDescriptor.createFromURL(getBundle().getEntry("icons/studio_project.gif")));
    }

    public static StudioProvider getStudioProvider() {
        if (studioProvider == null) {
            File root = getDefault().getStateLocation().toFile();
            studioProvider = new StudioProvider(root);
        }
        return studioProvider;
    }

}
