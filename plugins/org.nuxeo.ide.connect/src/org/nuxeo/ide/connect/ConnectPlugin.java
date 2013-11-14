package org.nuxeo.ide.connect;

import java.io.File;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nuxeo.ide.common.forms.PreferencesFormData;
import org.osgi.framework.BundleContext;

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

    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    public void stop(BundleContext context) throws Exception {
        studioProvider.dispose();
        plugin = null;
        studioProvider = null;
        super.stop(context);
    }

    public BundleContext getContext() {
        return getBundle().getBundleContext();
    }

    public PreferencesFormData getPreferences() {
        return new PreferencesFormData(new InstanceScope().getNode(PLUGIN_ID));
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
                ImageDescriptor.createFromURL(getBundle().getEntry(
                        "icons/studio_project.gif")));
    }

    public static StudioProvider getStudioProvider() {
        if (studioProvider == null) {
            File root = getDefault().getStateLocation().toFile();
            studioProvider = new StudioProvider(root);
        }
        return studioProvider;
    }

}
