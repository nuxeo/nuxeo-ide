package org.nuxeo.ide.connect;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.nuxeo.ide.common.IOUtils;
import org.nuxeo.ide.common.UI;
import org.nuxeo.ide.sdk.DeploymentChangedListener;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.deploy.Deployment;

public class RepositoryManager implements IBindingListener, IStudioListener, DeploymentChangedListener {

    static final String studioJarSuffix = "-0.0.0-SNAPSHOT.jar";

    protected final File root;

    protected static class Entry {
        protected final String projectId;

        protected final File file;

        Entry(String projectId, File file) {
            this.projectId = projectId;
            this.file = file;
        }

        @Override
        public String toString() {
            return file.getName();
        }

        public void download() {
            if (Display.getCurrent() == null) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        doDownload();
                    }

                });
            } else {
                doDownload();
            }
        }

        private void doDownload() {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false,
                        true, new RepositoryDownloadTask(this));
            } catch (Exception e) {
                UI.showError("Download of " + this + " was canceled", e);
            }
        }
    }

    protected final Map<String, Entry> entries = new HashMap<String, Entry>();

    public RepositoryManager(File root) {
        this.root = new File(root, "repository");
    }

    public void handleNewBinding(StudioProjectBinding binding) {
        for (String projectId : binding.getProjectIds()) {
            Entry entry = entries.get(projectId);
            if (!entries.containsKey(projectId)) {
                entry = new Entry(projectId, new File(root, projectId
                        + studioJarSuffix));
                entry.download();
                entries.put(projectId, entry);
            }
        }
    }

    public void reload() {
        if (!root.exists()) {
            root.mkdirs();
        } else {
            for (File file : root.listFiles()) {
                String name = file.getName();
                if (name.endsWith(studioJarSuffix)) {
                    int suffixIndex = name.lastIndexOf(studioJarSuffix);
                    String projectId = name.substring(0, suffixIndex);
                    Entry entry = new Entry(projectId, file);
                    entries.put(projectId, entry);
                }
            }
        }
    }

    public void garbage(Set<StudioProjectBinding> bindings) {
        Set<String> projectIds = new HashSet<String>();
        for (StudioProjectBinding binding : bindings) {
            projectIds.addAll(Arrays.asList(binding.getProjectIds()));
        }
        Iterator<Entry> it = entries.values().iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            if (projectIds.contains(entry.projectId)) {
                entry.file.delete();
                it.remove();
            }
        }
    }

    public void dispose() {
        entries.clear();
    }

    public File getFile(String id) {
        return entries.get(id).file;
    }

    @Override
    public void handleProjectsUpdate(StudioProvider provider) {
        for (Entry entry : entries.values()) {
            entry.download();
        }
    }

    @Override
    public void deploymentChanged(NuxeoSDK sdk, Deployment deployment) {
    	StudioProvider provider = ConnectPlugin.getStudioProvider();
    	for (IProject project : deployment.getProjects()) {
    		StudioProjectBinding binding = provider.getBindingManager().getBinding(
    				project);
			for (String eachId:binding.getProjectIds()) {            	
    			try {
    				new RepositoryDownloadTask(entries.get(eachId)).run(new NullProgressMonitor());
    			} catch (InvocationTargetException | InterruptedException cause) {
    				UI.showError("Cannot reload jar for studio project " + eachId, cause);
    			}
    		}
    	}
    }

    public void handleProject(List<String> projectIds)  {
        for (String projectId : projectIds) {
            Entry entry = entries.get(projectId);
            try {
				new RepositoryDownloadTask(entry).run(new NullProgressMonitor());
			} catch (InvocationTargetException | InterruptedException e) {
				UI.showError("Cannot load studio project : " + projectId, e);
			}
        }
    }

    public void erase() {
        IOUtils.deleteTree(root);
        root.mkdirs();
        entries.clear();
    }

}
