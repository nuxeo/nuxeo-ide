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
package org.nuxeo.ide.sdk.features.gadget;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Composite;
import org.nuxeo.ide.common.forms.Form;
import org.nuxeo.ide.common.wizards.FormWizardPage;
import org.nuxeo.ide.sdk.NuxeoNature;
import org.nuxeo.ide.sdk.features.FeatureCreationWizard;
import org.nuxeo.ide.sdk.features.FeatureTemplateContext;
import org.nuxeo.ide.sdk.ui.widgets.ProjectChooser;
import org.nuxeo.ide.sdk.ui.widgets.ProjectChooserWidget;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class GadgetWizardPage extends FormWizardPage<FeatureTemplateContext> {

    public GadgetWizardPage() {
        super("createGadget1", "Create Gadget", null);
    }

    @Override
    public Form createForm() {
        Form form = super.createForm();
        // form.addWidgetType(PackageChooserWidget.class);
        form.addWidgetType(ProjectChooserWidget.class);
        return form;
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        ProjectChooser projChooser = (ProjectChooser) form.getWidgetControl("project");
        FeatureCreationWizard wiz = (FeatureCreationWizard) getWizard();
        IJavaProject project = wiz.getSelectedNuxeoProject();
        projChooser.setNature(NuxeoNature.ID);
        if (project != null) {
            projChooser.setValue(project);
        }
    }

    @Override
    public void update(FeatureTemplateContext ctx) {
        IJavaProject project = (IJavaProject) ((ProjectChooserWidget) form.getWidget("project")).getValue();
        ctx.setProject(project);
        ctx.setProperty(form, "gadgetName");
        ctx.setProperty(form, "gadgetTitle");
        ctx.setProperty(form, "gadgetDescription");
        ctx.setProperty(form, "gadgetCategory");
    }

}
