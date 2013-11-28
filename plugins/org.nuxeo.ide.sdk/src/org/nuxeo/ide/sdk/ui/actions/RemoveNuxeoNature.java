/*
 * (C) Copyright 2006-2012 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 */
package org.nuxeo.ide.sdk.ui.actions;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.nuxeo.ide.common.RemoveNaturesAction;
import org.nuxeo.ide.sdk.NuxeoNature;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.java.ClasspathEditor;
import org.nuxeo.ide.sdk.java.SDKClasspathContainer;

/**
 * Fake remove nature - used as an example
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class RemoveNuxeoNature extends RemoveNaturesAction {

    public RemoveNuxeoNature() {
        super(NuxeoNature.ID);
    }

    @Override
    public void uninstall(IProject project, String natureId)
            throws CoreException {
        NuxeoNature.get(project).removeClasspath();
        super.uninstall(project, natureId);
    }

}
