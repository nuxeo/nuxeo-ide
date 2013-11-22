/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     Benjamin JALON<bjalon@nuxeo.com>
 */
package org.nuxeo.ide.sdk.projects.marketplace;

import org.eclipse.jdt.core.IJavaProject;
import org.nuxeo.ide.common.UI;
import org.nuxeo.ide.common.forms.Form;
import org.nuxeo.ide.sdk.model.PomModel;
import org.nuxeo.ide.sdk.projects.NuxeoProjectPage1;
import org.nuxeo.ide.sdk.projects.ProjectTemplateContext;
import org.nuxeo.ide.sdk.templates.Constants;
import org.nuxeo.ide.sdk.ui.widgets.ProjectChooser;
import org.nuxeo.ide.sdk.ui.widgets.ProjectChooserWidget;

/**
 * @since 1.2
 */
public class EclipseProjectPage extends NuxeoProjectPage1 {

    public EclipseProjectPage() {
        super("marketplace", "Create a Nuxeo Marketplace Package", null);
    }
    
    @Override
    public Form createForm() {
        Form form = super.createForm();
        form.addWidgetType(ProjectChooserWidget.class);
        return form;
    }

    @Override
    public void update(ProjectTemplateContext ctx) {
        ctx.put(Constants.BYPASS_PACKAGE, "true");
        super.update(ctx);
        ProjectChooser projChooser = (ProjectChooser) form.getWidgetControl("project");
        IJavaProject project = projChooser.getProject();
        try {
            PomModel pom = PomModel.getPomModel(project.getProject());
            ctx.put(MartketplaceWizardConstants.MP_ARTIFACT_ID,
                    pom.getArtifactId());
            ctx.put(Constants.PARENT_GROUP_ID, pom.getGroupId());
            ctx.put(MartketplaceWizardConstants.MP_NAME, pom.getArtifactId());
            ctx.put(MartketplaceWizardConstants.BUNDLE_VERSION, pom.getArtifactVersion());
        } catch (Exception e) {
            UI.showError(
                    "Errors occured while introspecting the project pom model",
                    e, "Project: " + project.getProject().getName());
        }
    }

}
