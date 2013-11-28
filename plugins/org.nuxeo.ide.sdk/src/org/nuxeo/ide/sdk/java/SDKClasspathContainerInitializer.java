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
package org.nuxeo.ide.sdk.java;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.nuxeo.ide.sdk.NuxeoNature;
import org.nuxeo.ide.sdk.NuxeoSDK;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class SDKClasspathContainerInitializer extends
        ClasspathContainerInitializer {

	
    public SDKClasspathContainerInitializer() {
    }
    
    protected SDKClasspathContainer container() {
    	return NuxeoSDK.getDefault().mainClasspathContainer;
    }

    @Override
    public void initialize(IPath containerPath, IJavaProject project)
            throws CoreException {
    	if (NuxeoSDK.getDefault() == null) {
    		return;
    	}
        if (!project.getProject().hasNature(NuxeoNature.ID)) {
        	return;
        }
		JavaCore.setClasspathContainer(containerPath,
				new IJavaProject[] { project },
				new IClasspathContainer[] { container() }, null);
    }
    
}
