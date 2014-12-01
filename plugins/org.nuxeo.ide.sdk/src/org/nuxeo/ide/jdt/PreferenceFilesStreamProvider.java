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
 */
package org.nuxeo.ide.jdt;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

import org.nuxeo.ide.common.UI;

/**
 * Knows how to retrieve an input stream of a preference file. Default implementation will try to get it from the
 * "tools" folder of the "nuxeo" project in the current workspace or from the current classpath if it can't find it.
 */
public class PreferenceFilesStreamProvider {

    String preferenceFileName;

    public PreferenceFilesStreamProvider(String fileName) {
        preferenceFileName = fileName;
    }

    public InputStream getInputStream() {
        try {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
                    new Path("/nuxeo/tools/" + preferenceFileName));
            if (file.exists()) {
                return file.getContents();
            }
        } catch (Exception e) {
            UI.showWarning("An error occurred while setting " + preferenceFileName
                    + " from nuxeo/tools folder, fallback to the default one");
        }

        return getFallbackStream();
    }

    public InputStream getFallbackStream() {
        return PreferenceFilesStreamProvider.class.getResourceAsStream(preferenceFileName);
    }

}
