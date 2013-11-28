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
 *     bstefanescu
 *     Vladimir Pasquier <vpasquier@nuxeo.com>
 *     Sun Seng David TAN <stan@nuxeo.com>
 */
package org.nuxeo.ide.sdk.ui.actions;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.nuxeo.ide.common.AddNaturesAction;
import org.nuxeo.ide.common.UI;
import org.nuxeo.ide.sdk.NuxeoNature;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.nuxeo.ide.sdk.SDKRegistry;
import org.nuxeo.ide.sdk.java.ClasspathEditor;
import org.nuxeo.ide.sdk.java.SDKClasspathContainer;
import org.osgi.service.prefs.BackingStoreException;

/**
 * "Convert to Nuxeo Project" action
 */
public class AddNuxeoNature extends AddNaturesAction {

    public AddNuxeoNature() {
        super(NuxeoNature.ID);
    }

    @Override
    public void install(IProject project, String natureId,
            IProgressMonitor monitor) throws CoreException {
        NuxeoSDK sdk = NuxeoSDK.getDefault();
        if (sdk == null) {
            UI.showError("Please configure the Nuxeo SDK to convert the project");
            return;
        }
        super.install(project, natureId, monitor);
        if (!SDKRegistry.getWorkspacePreferences().getBoolean(
                "useSDKClasspath", Boolean.TRUE)) {
            return;
        }
        NuxeoNature.get(project).addClasspath();
    }

    protected void createSourceFolder(IProject project, String path,
            IProgressMonitor monitor) throws CoreException {
        IFolder folder = project.getFolder(path);
        if (!folder.exists()) {
            folder.create(true, false, monitor);
        }
    }

}
