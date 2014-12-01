/*
 * (C) Copyright 2013-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Sun Seng David TAN <stan@nuxeo.com>
 *     jcarsique
 */
package org.nuxeo.ide.jdt;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import org.nuxeo.ide.common.UI;
import org.nuxeo.ide.sdk.NuxeoSDK;

/**
 * Default implementation will try to get the preferences files in order from:
 * <ul>
 * <li>the "sdk/tools" folder in the configured Nuxeo SDK</li>
 * <li>the "nuxeo/tools" folder in the current workspace</li>
 * <li>the current classpath (ie embedded in the current plugin)</li>
 * </ul>
 */
public class PreferenceFilesStreamProvider {

    String preferenceFileName;

    public PreferenceFilesStreamProvider(String fileName) {
        preferenceFileName = fileName;
    }

    public InputStream getInputStream() {
        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        if (NuxeoSDK.getDefault() != null) {
            IFile prefIFile = root.getFile(Path.fromOSString(NuxeoSDK.getDefault().getToolsDir().resolve(
                    preferenceFileName).toString()));
            try {
                return prefIFile.getContents();
            } catch (CoreException e) {
                UI.showError("Resource not found in Nuxeo SDK tools directory", e);
            }
        }

        IFile prefIFile = root.getFile(new Path("/nuxeo/tools/" + preferenceFileName));
        if (prefIFile.exists()) {
            try {
                return prefIFile.getContents();
            } catch (CoreException e) {
                UI.showError("Resource not found in Nuxeo sources directory", e);
            }
        }

        return getInputStreamFromCP();
    }

    public InputStream getInputStreamFromCP() {
        return PreferenceFilesStreamProvider.class.getResourceAsStream(preferenceFileName);
    }

}
