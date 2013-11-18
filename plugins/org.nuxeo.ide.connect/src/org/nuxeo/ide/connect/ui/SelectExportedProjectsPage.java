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
package org.nuxeo.ide.connect.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.nuxeo.ide.connect.ConnectPlugin;
import org.nuxeo.ide.connect.studio.StudioProject;
import org.nuxeo.ide.sdk.deploy.ProjectViewProvider;
import org.nuxeo.ide.sdk.ui.NuxeoNature;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class SelectExportedProjectsPage extends WizardPage {

    protected CheckboxTableViewer tv;

    protected Combo combo;

    protected int studioProjectIndex = -1;

    protected StudioProject[] studioProjects;

    protected IProject[] selectedProjects;

    public SelectExportedProjectsPage() {
        super("selectProjects", "Select Projects to Scan", null);
    }

    @Override
    public void createControl(Composite parent) {
        studioProjects = ConnectPlugin.getStudioProvider().getProjects();

        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(2, false));
        Label label = new Label(panel, SWT.NONE);
        label.setText("Select the target Studio project:");
        combo = new Combo(panel, SWT.BORDER | SWT.READ_ONLY);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        combo.setLayoutData(gd);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        label = new Label(panel, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(gd);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        label = new Label(panel, SWT.NONE);
        label.setText("Select the projects to scan for operations:");
        label.setLayoutData(gd);
        ProjectViewProvider provider = new ProjectViewProvider(
                new String[] { NuxeoNature.ID });
        tv = CheckboxTableViewer.newCheckList(panel, SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.BORDER);
        tv.setLabelProvider(provider);
        tv.setContentProvider(provider);
        tv.setInput(ResourcesPlugin.getWorkspace().getRoot());
        gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gd.horizontalSpan = 2;
        tv.getTable().setLayoutData(gd);
        setControl(panel);

        combo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                studioProjectIndex = combo.getSelectionIndex();
                updateCompletionStatus();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }

        });

        tv.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                Object[] objs = tv.getCheckedElements();
                if (objs.length > 0) {
                    selectedProjects = new IProject[objs.length];
                    System.arraycopy(objs, 0, selectedProjects, 0, objs.length);
                } else {
                    selectedProjects = null;
                }
                updateCompletionStatus();
            }
        });
        for (StudioProject project : studioProjects) {
            combo.add(project.getName());
        }
        studioProjectIndex = -1;
        updateCompletionStatus();
    }

    protected void updateCompletionStatus() {
        setPageComplete(studioProjectIndex > -1 && selectedProjects != null
                && selectedProjects.length > 0);
    }

    public IProject[] getSelectedProjects() {
        return selectedProjects;
    }

    public StudioProject getSelectedStudioProject() {
        return studioProjectIndex > -1 ? studioProjects[studioProjectIndex]
                : null;
    }

}
