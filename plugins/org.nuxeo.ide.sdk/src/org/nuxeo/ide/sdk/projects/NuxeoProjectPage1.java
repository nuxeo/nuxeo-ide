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
package org.nuxeo.ide.sdk.projects;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkingSet;
import org.nuxeo.ide.common.forms.BindingContext;
import org.nuxeo.ide.common.forms.Form;
import org.nuxeo.ide.common.forms.HasValue;
import org.nuxeo.ide.common.forms.UIObject;
import org.nuxeo.ide.common.wizards.FormWizardPage;
import org.nuxeo.ide.common.wizards.ImportProject;
import org.nuxeo.ide.sdk.templates.Constants;
import org.nuxeo.ide.sdk.ui.widgets.JREList;
import org.nuxeo.ide.sdk.ui.widgets.WorkingSetsPanel;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class NuxeoProjectPage1 extends FormWizardPage<ProjectTemplateContext>
        implements Constants {

    public final static String PROJECT_LOCATION = "projectLocation";

    public NuxeoProjectPage1(String id, String title, ImageDescriptor image) {
        super(id, title, image);
    }

    public NuxeoProjectPage1() {
        super("nuxeoProjectPage1", "Create a Nuxeo Project", null);
    }

    @Override
    public Form createForm() {
        Form form = super.createForm();
        form.addWidgetType(JREList.class);
        form.addBinding("workingSets", new UIObject<Control>() {
            @Override
            protected Control createControl(Composite parent, Element element,
                    BindingContext ctx) {
                return new WorkingSetsPanel(parent, getSelection());
            }
        });
        return form;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        UIObject<?> obj = form.getWidget(PROJECT_LOCATION);
        ((HasValue) obj).setValue(getDefaultPath());

        final Text idText = (Text) (Text) form.getWidgetControl(PROJECT_ID);
        idText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(org.eclipse.swt.events.KeyEvent e) {
                String text = idText.getText();
                if (text.length() > 0) {
                    if (ImportProject.isExistingProject(text)) {
                        NuxeoProjectPage1.this.setErrorMessage("A project with that name already exists");
                        NuxeoProjectPage1.this.setPageComplete(false);
                    }
                }
            }
        });
    }

    protected IPath getDefaultLocation() {
        return Platform.getLocation();
    }

    protected IPath getLocation(String path) {
        return Path.fromOSString(path.trim());
    }

    protected String getDefaultPath() {
        final IPath path = Platform.getLocation();
        return path.toOSString();
    }

    public String getProjectId() {
        return form.getWidgetValueAsString(PROJECT_ID);
    }

    public String getProjectRootPackage() {
        return form.getWidgetValueAsString(PROJECT_PACKAGE);
    }

    public IPath getSelectedLocation() {
        return getLocation(form.getWidgetValueAsString(PROJECT_LOCATION));
    }

    public File getSelectedLocationFile() {
        return new File(form.getWidgetValueAsString(PROJECT_LOCATION));
    }

    public boolean isDefaultLocation(IPath path) {
        return path.equals(getDefaultLocation());
    }

    public URI getProjectLocationURI() {
        IPath path = getSelectedLocation();
        if (isDefaultLocation(path)) {
            return null;
        }
        return path.toFile().toURI();
    }

    public IWorkingSet[] getSelectedWorkingSets() {
        UIObject<?> obj = form.getWidget("workingSets");
        if (obj != null) {
            return ((WorkingSetsPanel) obj.getControl()).getSelectedWorkingSets();
        }
        return null;
    }

    public String getJre() {
        return ((HasValue) form.getWidget("jre")).getValueAsString();
    }

    public String getStudioProject() {
        return form.getWidgetValueAsString("studio");
    }

    @Override
    public void update(ProjectTemplateContext ctx) {
        ctx.setProjectLocation(new File(getSelectedLocationFile(),
                getProjectId()));
        ctx.setWorkingSets(getSelectedWorkingSets());
        String v = ctx.setProperty(form, PROJECT_ID, ARTIFACT_ID, ARTIFACT_NAME);
        ctx.put(PROJECT_NAME, v);
        ctx.setProperty(form, PROJECT_ID, ARTIFACT_ID, ARTIFACT_NAME);
        v = ctx.setProperty(form, PROJECT_PACKAGE);
        int i = v.lastIndexOf('.');
        if (i > 0) {
            ctx.put(GROUP_ID, v.substring(0, i));
        }
        // TODO
        // ctx.setStudioProject(getStudioProject());
    }
}
