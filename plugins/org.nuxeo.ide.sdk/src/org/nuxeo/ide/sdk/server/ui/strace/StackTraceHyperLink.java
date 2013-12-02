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
package org.nuxeo.ide.sdk.server.ui.strace;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.internal.debug.ui.actions.OpenTypeAction;
import org.eclipse.jdt.internal.debug.ui.console.ConsoleMessages;
import org.eclipse.jdt.internal.debug.ui.console.JavaStackTraceHyperlink;
import org.nuxeo.ide.sdk.SDKPlugin;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class StackTraceHyperLink extends JavaStackTraceHyperlink {

    protected String linkText;

    public StackTraceHyperLink(String text) {
        super(null);
        this.linkText = text.trim();
    }

    public void onClick() {
        linkActivated();
    }

    protected String getLinkText() throws CoreException {
        return linkText;
    }

    protected void startSourceSearch(final String typeName, final int lineNumber) {
        Job search = new Job(ConsoleMessages.JavaStackTraceHyperlink_2) {
            protected IStatus run(IProgressMonitor monitor) {
                Object result = null;
                try {
                    // search for the type in the workspace
                    result = OpenTypeAction.findTypeInWorkspace(typeName, true);
                    searchCompleted(result, typeName, lineNumber, null);
                } catch (CoreException e) {
                    searchCompleted(null, typeName, lineNumber, e.getStatus());
                }
                return Status.OK_STATUS;
            }

        };
        search.schedule();
    }

}
