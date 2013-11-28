/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Sun Seng David TAN <stan@nuxeo.com>
 */
package org.nuxeo.ide.sdk.model;

import java.io.File;

/**
 * Provides a PomModel object from a jar file. Having a jar file,
 * {@link M2PomModelProvider#getPomModel()} looks if there is a .pom file next
 * to it and parse it to return a PomModel.
 */
public class M2PomModelProvider {

    String libFilePath;

    /**
     * @param libFilePath the jar filePath (with the extension .jar)
     */
    public M2PomModelProvider(String libFilePath) {
        this.libFilePath = libFilePath;
    }

    /**
     * {@link M2PomModelProvider}
     *
     * @return null if no pom file is found
     * @throws Exception if any parse error
     */
    public PomModel getPomModel() throws Exception {
        if (libFilePath.endsWith(".jar")) {
            File pom = new File(libFilePath.substring(0,
                    libFilePath.length() - 3) + "pom");
            if (pom.isFile()) {
                return new PomModel(pom);
            }
        }
        return null;
    }

}
