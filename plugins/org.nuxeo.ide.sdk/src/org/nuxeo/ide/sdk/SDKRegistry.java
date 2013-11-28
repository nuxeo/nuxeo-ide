/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class SDKRegistry {

    private final static Preferences getGlobalPreferences() {
        return new ConfigurationScope().getNode(SDKPlugin.PLUGIN_ID).node(
                "sdks");
    }

    public final static Preferences getWorkspacePreferences() {
        return new InstanceScope().getNode(SDKPlugin.PLUGIN_ID);
    }

    public static List<SDKInfo> getConfiguredSDKs()
            throws BackingStoreException {
        Preferences prefs = getGlobalPreferences();
        ArrayList<SDKInfo> sdks = new ArrayList<SDKInfo>();
        for (String key : prefs.childrenNames()) {
            Preferences sdkPrefs = prefs.node(key);
            String path = sdkPrefs.get("path", null);
            if (path == null) {
                continue;
            }
            String version = sdkPrefs.get("version", null);
            if (version == null) {
                continue;
            }
            String name = sdkPrefs.get("name", "Nuxeo SDK");
            sdks.add(new SDKInfo(path, name, version));
        }
        return sdks;
    }

    public static synchronized void addSDK(SDKInfo sdk)
            throws BackingStoreException {
        Preferences prefs = getGlobalPreferences();
        Preferences sdkPrefs = prefs.node(sdk.getId());
        sdkPrefs.put("path", sdk.getPath());
        sdkPrefs.put("name", sdk.getName());
        sdkPrefs.put("version", sdk.getVersion());
        prefs.flush();
    }

    public static synchronized void removeSDK(SDKInfo sdk)
            throws BackingStoreException {
        Preferences prefs = getGlobalPreferences();
        prefs.node(sdk.getId()).removeNode();
        prefs.flush();
    }

    public static synchronized void save(List<SDKInfo> sdks)
            throws BackingStoreException {
        Preferences prefs = getGlobalPreferences();
        for (String key : prefs.childrenNames()) {
            prefs.node(key).removeNode();
        }
        if (sdks != null && !sdks.isEmpty()) {
            for (SDKInfo sdk : sdks) {
                Preferences sdkPrefs = prefs.node(sdk.getId());
                sdkPrefs.put("path", sdk.getPath());
                sdkPrefs.put("name", sdk.getName());
                sdkPrefs.put("version", sdk.getVersion());
            }
        }
        prefs.flush();
    }

    public static SDKInfo getSDK(String id) throws BackingStoreException {
        Preferences prefs = getGlobalPreferences();
        if (prefs.nodeExists(id)) {
            Preferences sdkPrefs = prefs.node(id);
            return new SDKInfo(sdkPrefs.get("path", null), sdkPrefs.get("name",
                    "Nuxeo SDK"), sdkPrefs.get("version", null));
        }
        return null;
    }

    public static List<SDKInfo> getMatchingSDK(String version)
            throws BackingStoreException {
        ArrayList<SDKInfo> result = new ArrayList<SDKInfo>();
        String suffix = new StringBuilder(version.length() + 1).append('#').append(
                version).toString();
        Preferences prefs = getGlobalPreferences();
        for (String key : prefs.childrenNames()) {
            if (key.endsWith(suffix)) {
                Preferences sdkPrefs = prefs.node(key);
                result.add(new SDKInfo(sdkPrefs.get("path", null),
                        sdkPrefs.get("name", "Nuxeo SDK"), sdkPrefs.get(
                                "version", null)));
            }
        }
        return result;
    }

    public static void setDefaultSDK(SDKInfo sdk) throws BackingStoreException {
        Preferences prefs = getWorkspacePreferences();
        if (sdk == null) {
            prefs.remove("defaultSDK");
        } else {
            prefs.put("defaultSDK", sdk.getId());
        }
        prefs.flush();
        NuxeoSDK.setDefault(sdk);
    }

    public static SDKInfo getDefaultSDK() throws BackingStoreException {
        String v = getDefaultSDKId();
        if (v == null) {
            return null;
        }
        return getSDK(v);
    }

    public static void setUseSDKClasspath(Boolean useSDKClasspath)
            throws CoreException {
    	Boolean oldUseSdk = useSDKClasspath();
        Preferences prefs = getWorkspacePreferences();
        prefs.putBoolean("useSDKClasspath",
                useSDKClasspath == null ? Boolean.TRUE : useSDKClasspath);
        if (!oldUseSdk.equals(useSDKClasspath)) {
            NuxeoSDK.reloadSDKClasspathContainer();
        }
    }

    public static Boolean useSDKClasspath() {
        return getWorkspacePreferences().getBoolean("useSDKClasspath",
                Boolean.TRUE);
    }

    public static String getDefaultSDKId() {
        Preferences prefs = getWorkspacePreferences();
        return prefs.get("defaultSDK", null);
    }

    public static <T> T loadInstance(Class<T> clazz, String id) throws CoreException {
        IConfigurationElement configs[] = Platform.getExtensionRegistry().getConfigurationElementsFor(
                id);
        ;
        if (configs.length == 0) {
            throw new CoreException(new Status(IStatus.ERROR,
                    SDKPlugin.PLUGIN_ID, "No class of " + clazz.getSimpleName() + " contribued"));

        }
        if (configs.length > 1) {
            throw new CoreException(new Status(IStatus.ERROR,
                    SDKPlugin.PLUGIN_ID, "Only one connect provider allowed"));
        }
        return clazz.cast(configs[0].createExecutableExtension("class"));
    }
    
}
