package org.nuxeo.ide.sdk.java;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.JavaModelException;
import org.nuxeo.ide.common.UI;
import org.nuxeo.ide.sdk.NuxeoNature;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.SDKPlugin;

public class SDKClasspathFixer implements IResourceChangeListener {

    @Override
    public void resourceChanged(IResourceChangeEvent event) {

        IResourceDelta delta = event.getDelta();
        if (delta == null
                || delta.getKind() != IResourceChangeEvent.PRE_DELETE) {
            return;
        }

        for (IResourceDelta each : delta.getAffectedChildren()) {
            IResource resource = each.getResource();
            if ((each.getFlags()&IResourceDelta.OPEN) == 0) {
                continue;
            }
            if (resource.getType() != IResource.PROJECT) {
                return;
            }
            final IProject project = (IProject) resource;
            if (!project.isOpen()) {
                continue;
            }
            try {
                if (!project.hasNature(NuxeoNature.ID)) {
                    continue;
                }
            } catch (Exception e) {
                UI.showError("Cannot state on nuxeo's project nature for "
                        + project.getName(), e);
            }
            new Job("SDK classpath fixer (" + project.getName() + ")") {               
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        NuxeoNature.get(project).reloadClasspath();
                    } catch (CoreException cause) {
                        return SDKPlugin.statusError("Cannot update classpath of " + project.getName(), cause);
                    } 
                    return SDKPlugin.statusOk("Updated " + project.getName() + " classpath");
                }
            }.schedule();
        }
    }

    public void registerSelf() throws CoreException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        workspace.addResourceChangeListener(this,
                IResourceChangeEvent.POST_CHANGE);
    }

    public void unregisterSelf() {
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    }

}
