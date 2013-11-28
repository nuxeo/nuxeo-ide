/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ide.sdk;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nuxeo.ide.common.forms.PreferencesFormData;
import org.nuxeo.ide.sdk.java.SDKClasspathFixer;
import org.nuxeo.ide.sdk.templates.TemplateManager;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SDKPlugin extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.nuxeo.ide.sdk"; //$NON-NLS-1$

    // The shared instance
    private static SDKPlugin plugin;

    protected TemplateManager tempMgr;

    protected IConnectProvider connectProvider;
    
    protected IEclipseAdapter eclipseAdapter;
    
    /**
     * The constructor
     */
    public SDKPlugin() {
    }

    public TemplateManager getTemplateManager() {
        return tempMgr;
    }

    public void start(BundleContext context) throws Exception {
        super.start(context);
        tempMgr = new TemplateManager();
        tempMgr.loadRegistry(context.getBundle());
        plugin = this;
        connectProvider = SDKRegistry.loadInstance(IConnectProvider.class, IConnectProvider.ID);
        eclipseAdapter = SDKRegistry.loadInstance(IEclipseAdapter.class, IEclipseAdapter.ID);
        NuxeoSDK.initialize();
    }

    public void stop(BundleContext context) throws Exception {
        NuxeoSDK.dispose();
        connectProvider = null;
        plugin = null;
        tempMgr = null;
        super.stop(context);
    }

    public BundleContext getContext() {
        return getBundle().getBundleContext();
    }

    public PreferencesFormData getPreferences() {
        return new PreferencesFormData(new InstanceScope().getNode(PLUGIN_ID));
    }

    public IConnectProvider getConnectProvider() {
        return connectProvider;
    }
    
    
    public IEclipseAdapter getEclipseAdapter() {
        return eclipseAdapter;
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static SDKPlugin getDefault() {
        return plugin;
    }

    public static CoreException coreException(String message, Throwable cause) {
    	return new CoreException(statusError(message, cause));
    }
    
    public static IStatus statusError(String message, Throwable cause) {
        return new Status(Status.ERROR, PLUGIN_ID,  message, cause);
    }
    
    public static IStatus statusOk(String message) {
        return new Status(Status.OK, PLUGIN_ID, message);
    }
    
    /**
     *
     * @param status a status value from {@link IStatus}
     * @param message
     */
    public static void log(int status, String message) {
        getDefault().getLog().log(new Status(status, PLUGIN_ID, message));
    }

    /**
     *
     * @param status a status value from {@link IStatus}
     * @param message
     * @param cause
     */
    public static void log(int status, String message, Throwable cause) {
        getDefault().getLog().log(new Status(status, PLUGIN_ID, message, cause));
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put("icons/comp/component.gif",
                ImageDescriptor.createFromURL(getBundle().getEntry(
                        "icons/comp/component.gif")));
        reg.put("icons/comp/service.gif",
                ImageDescriptor.createFromURL(getBundle().getEntry(
                        "icons/comp/service.gif")));
        reg.put("icons/comp/xpoint.gif",
                ImageDescriptor.createFromURL(getBundle().getEntry(
                        "icons/comp/xpoint.gif")));
    }

}
