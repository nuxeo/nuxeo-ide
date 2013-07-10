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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.nuxeo.ide.sdk.userlibs.UserLib;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class DeploymentPanel extends Composite {

    protected Deployment deployment;

    protected Text name;

    protected ProjectCheckList projects;

    protected UserLibCheckList libraries;

    protected DeploymentDialog dialog;

    public DeploymentPanel(Composite parent) {
        super(parent, SWT.BORDER);
        createContent();
    }

    public void setDialog(DeploymentDialog dialog) {
        this.dialog = dialog;
    }

    public void setDeployment(Deployment deployment) {
        this.deployment = deployment;
        name.setText(deployment.getName());
        projects.setCheckedProjects(deployment.getProjects());
        libraries.setCheckedLibs(deployment.getLibraries());
    }

    public Deployment getDeployment() {
        return deployment;
    }

    @Override
    public void dispose() {

    }

    protected void createContent() {
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        setLayout(layout);

        GridData oneColumn = new GridData();
        oneColumn.horizontalSpan = 2;
        oneColumn.horizontalAlignment = SWT.BEGINNING;
        oneColumn.grabExcessHorizontalSpace = false;

        GridData fillColumn = new GridData();
        fillColumn.horizontalSpan = 2;
        fillColumn.horizontalAlignment = SWT.FILL;
        fillColumn.verticalAlignment = SWT.FILL;
        fillColumn.grabExcessHorizontalSpace = true;
        fillColumn.grabExcessVerticalSpace = true;

        name = new Text(this, SWT.BORDER);
        name.setLayoutData(oneColumn);

        Label label1 = new Label(this, SWT.NONE);
        label1.setText("Projects");
        projects = new ProjectCheckList(this);
        projects.setLayoutData(fillColumn);
        Button selectAllProjects = selectAllButton();
        selectAllProjects.setLayoutData(oneColumn);

        Label label2 = new Label(this, SWT.NONE);
        label2.setText("User Libraries");
        libraries = new UserLibCheckList(this);
        libraries.setLayoutData(fillColumn);
        Button selectAllLibraries = selectAllButton();
        selectAllLibraries.setLayoutData(oneColumn);


        name.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (dialog != null) {
                    deployment.setName(name.getText());
                    dialog.getDeploymentsTable().updateDeployment(deployment);
                }
            }
        });

        projects.getTableViewer().addCheckStateListener(
                new ICheckStateListener() {
                    @Override
                    public void checkStateChanged(CheckStateChangedEvent event) {
                        if (event.getChecked()) {
                            deployment.addProject((IProject) event.getElement());
                        } else {
                            deployment.removeProject((IProject) event.getElement());
                        }
                    }
                });

        libraries.getTableViewer().addCheckStateListener(
                new ICheckStateListener() {
                    @Override
                    public void checkStateChanged(CheckStateChangedEvent event) {
                        UserLib lib = (UserLib) event.getElement();
                        if (event.getChecked()) {
                            deployment.addLibrary(lib);
                        } else {
                            deployment.removeLibrary(lib);
                        }
                    }
                });

        selectAllProjects.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (projects.getTableViewer().getCheckedElements().length != 0) {
                    projects.getTableViewer().setAllChecked(false);
                    deployment.clearProjects();
                } else {
                    projects.getTableViewer().setAllChecked(true);
                    deployment.addProjects(projects.getCheckedProjects());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        selectAllLibraries.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (libraries.getTableViewer().getCheckedElements().length != 0) {
                    libraries.getTableViewer().setAllChecked(false);
                    deployment.clearLibraries();
                } else {
                    libraries.getTableViewer().setAllChecked(true);
                    deployment.addLibraries(libraries.getCheckedLibs());
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

    }

    /**
     * Select/Unselect All buttons
     */
    protected Button selectAllButton() {
        Button selectButton = new Button(this, SWT.PUSH);
        selectButton.setLocation(50, 50);
        selectButton.setText("Select/Unselect All");
        selectButton.pack();
        return selectButton;
    }
}
