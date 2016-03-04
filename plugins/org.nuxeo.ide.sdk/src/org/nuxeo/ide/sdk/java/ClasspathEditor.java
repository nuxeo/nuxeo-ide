/*
 * (C) Copyright 2006-2013 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     slacoin
 *     Vladimir Pasquier <vpasquier@nuxeo.com>
 *     Sun Seng David TAN <stan@nuxeo.com>
 */
package org.nuxeo.ide.sdk.java;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.nuxeo.ide.common.JarUtils;
import org.nuxeo.ide.common.UI;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.model.M2PomModelProvider;
import org.nuxeo.ide.sdk.model.PomModel;
import org.nuxeo.ide.sdk.userlibs.UserLib;
import org.nuxeo.ide.sdk.userlibs.UserLibPreferences;
import org.osgi.service.prefs.BackingStoreException;

/**
 *
 * @author matic
 * @since 1.1
 */
public class ClasspathEditor {

    public ClasspathEditor(IProject project) throws JavaModelException {
        this.project = project;
        java = JavaCore.create(project);
        entries.addAll(Arrays.asList(java.getRawClasspath()));
    }

    protected final IJavaProject java;

    protected final IProject project;

    protected final List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();

    boolean dirty = false;

    /**
     * Extends classpath target project with src folder
     *
     * @param name
     * @throws JavaModelException
     */
    public void extendClasspath(String name) throws JavaModelException {
        IProject project = java.getProject();
        IFolder folder = project.getFolder("src/main/" + name);
        IPackageFragmentRoot root = java.getPackageFragmentRoot(folder);
        if (root.exists()) {
            IClasspathEntry entry = root.getRawClasspathEntry();
            if (entry.getOutputLocation() != null) {
                return;
            }
            entries.remove(entry);
        }
        // extend project class path
        IFolder binFolder = project.getFolder("bin/" + name);
        IClasspathEntry newEntry = JavaCore.newSourceEntry(
                folder.getFullPath(), new IPath[0], new IPath[0],
                binFolder.getFullPath());
        entries.add(newEntry);
        dirty = true;
    }

    /**
     * Adding containers to the project classpath
     *
     * @param containers
     * @throws JavaModelException
     */
    public void addContainers(String[] containers)
            throws JavaModelException {
        for (String container : containers) {
            IClasspathEntry classPathEntry = JavaCore.newContainerEntry(
                    new Path(container), new IAccessRule[0], null, false);
            entries.add(classPathEntry);
        }
        dirty = true;
    }

    public void addLibrary(IPath path) {
        IClasspathEntry newEntry = JavaCore.newLibraryEntry(path, null, null);
        entries.add(newEntry);
        dirty = true;
    }

    public void removeLibrary(Path path) {
        Iterator<IClasspathEntry> it = entries.iterator();
        while (it.hasNext()) {
            IClasspathEntry entry = it.next();
            if (path.equals(entry.getPath())) {
                it.remove();
                dirty = true;
            }
        }
    }

    public void removeLibraries() {
        Iterator<IClasspathEntry> it = entries.iterator();
        while (it.hasNext()) {
            IClasspathEntry entry = it.next();
            switch (entry.getEntryKind()) {
            case IClasspathEntry.CPE_LIBRARY:
            case IClasspathEntry.CPE_VARIABLE:
                it.remove();
                dirty = true;
            }
        }
    }

    public void flush() throws JavaModelException {
        if (!dirty) {
        	return;
        }
		java.setRawClasspath(
				entries.toArray(new IClasspathEntry[entries.size()]), null);
		dirty = false;
    }

    /**
     * Removing containers to the project classpath
     *
     * @param containers
     * @throws JavaModelException
     */
    public void removeContainers(String[] containers) {
        for (String container : containers) {
            IClasspathEntry classPathEntry = JavaCore.newContainerEntry(
                    new Path(container), new IAccessRule[0], null, false);
            entries.remove(classPathEntry);
        }
        dirty = true;
    }

    protected String artifactKey(IClasspathEntry entry) throws Exception {
        IPath jarPath = JavaCore.getResolvedClasspathEntry(entry).getPath();
        File jarFile = jarPath.toFile();
        if (jarFile.isDirectory()) {
            PomModel pom = new PomModel(JarUtils.getPom(jarFile));
            return String.format("%s-%s.jar", pom.getArtifactId(), pom.getArtifactVersion());
        }
        return jarPath.lastSegment();
    }

    protected String artifactKey(String gav) {
        int twoDotIndex = 0;
        for (int i = 0; i < 2; i++) {
            twoDotIndex = gav.indexOf(':', twoDotIndex + 1);
        }
        return gav.substring(0, twoDotIndex);
    }

    /**
     * Removing classpath entries that are in already in the containers. This
     * method is intended to be called after a mvn eclipse:eclipse
     */
    protected void removeDuplicates()  {

        // classpath index
        Map<String, IClasspathEntry> cpIndex = new HashMap<String, IClasspathEntry>();
        List<IClasspathEntry> duplicateEntries = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry each : entries) {
            if (each.getEntryKind() != IClasspathEntry.CPE_VARIABLE) {
                continue;
            }
            String key;
            try {
                key = artifactKey(each);
            } catch (Exception cause) {
                UI.showWarning("cannot compute artifact key for " + each
                        + ", skipping (don't embed a pom in jar)", cause);
                continue;
            }
            if (cpIndex.containsKey(key)) {
                UI.showWarning("classpath contains duplicate entries for "
                        + key + ", keeping last " + each);
                duplicateEntries.add(each);
                continue;
            }
            cpIndex.put(key, each);
        }
        entries.removeAll(duplicateEntries);

        // sdk index
        Collection<String> sdkIndex = new ArrayList<String>();
        sdkIndex.addAll(NuxeoSDK.getDefault().getArtifactIndex().getIndex().keySet());
        sdkIndex.addAll(NuxeoSDK.getDefault().getTestArtifactIndex().getIndex().keySet());

        // cleanup
        for (String each : sdkIndex) {
            if (cpIndex.containsKey(each)) {
                IClasspathEntry entry = cpIndex.get(each);
                entries.remove(entry);
                dirty = true;
            }
        }
    }

    /**
     * Check into the classpath entries and add all as a NuxeoSDK UserLib (can
     * be deployed in the sdk)
     *
     */
    public void setLibsAsSdkUserLibs() throws CoreException, BackingStoreException {
        removeDuplicates();
        UserLibPreferences prefs = UserLibPreferences.load();
        ArrayList<IClasspathEntry> entriesToRemove = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry iClasspathEntry : entries) {
            String path = JavaCore.getResolvedClasspathEntry(iClasspathEntry).getPath().toString();
            File file = new File(path);
            // a userlib is normally a jar file, right ?
            if (path.endsWith(".jar")) {
                UserLib lib = new UserLib(file.getAbsolutePath());
                M2PomModelProvider m2PomModelProvider = new M2PomModelProvider(
                        path);
                try {
                    PomModel model = m2PomModelProvider.getPomModel();
                    if (model != null) {
                        lib.setGroupId(model.getGroupId());
                        lib.setArtifactId(model.getArtifactId());
                        lib.setVersion(model.getArtifactVersion());
                    }
                } catch (Exception e) {
                    UI.showError("Failed to parse associated pom", e);
                }
                prefs.addUserLib(lib);
                // removing from the classpath entries to avoid duplicated
                // entries
                entriesToRemove.add(iClasspathEntry);
            }
        }
        // in 2 steps to avoid concurent access to entries
        for (IClasspathEntry iClasspathEntry : entriesToRemove) {
            entries.remove(iClasspathEntry);
            dirty = true;
        }

        if (prefs.isModified()) {
            prefs.save();
            NuxeoSDK.reloadSDKClasspathContainer();
        }

    }

}
