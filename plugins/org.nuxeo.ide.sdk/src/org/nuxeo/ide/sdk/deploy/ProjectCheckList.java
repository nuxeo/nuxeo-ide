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
package org.nuxeo.ide.sdk.deploy;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.nuxeo.ide.sdk.NuxeoNature;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class ProjectCheckList extends Composite {

    protected CheckboxTableViewer tv;

    public ProjectCheckList(Composite parent) {
        super(parent, SWT.NONE);
        createControl();
    }

    public CheckboxTableViewer getTableViewer() {
        return tv;
    }

    protected void createControl() {
        setLayout(new FillLayout());
        tv = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.H_SCROLL
                | SWT.V_SCROLL);

        ProjectViewProvider provider = new ProjectViewProvider(
                new String[] { NuxeoNature.ID });
        tv.setLabelProvider(provider);
        tv.setContentProvider(provider);

        tv.setInput(ResourcesPlugin.getWorkspace().getRoot());

    }

    public void setCheckedProjects(IProject[] projects) {
        tv.setCheckedElements(projects);
    }

    public IProject[] getCheckedProjects() {
        Object[] ar = tv.getCheckedElements();
        IProject[] result = new IProject[ar.length];
        System.arraycopy(ar, 0, result, 0, ar.length);
        return result;
    }

}
