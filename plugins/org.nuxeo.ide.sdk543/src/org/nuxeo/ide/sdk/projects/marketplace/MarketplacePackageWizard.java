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

import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.FUNKLOAD_ENABLED;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.SELENIUM_ENABLED;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.WEBDRIVER_ENABLED;

import java.io.File;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.WizardPage;
import org.nuxeo.ide.sdk.projects.AbstractProjectWizard;
import org.nuxeo.ide.sdk.projects.CreateProjectFromTemplate;
import org.nuxeo.ide.sdk.projects.ProjectTemplateContext;
import org.nuxeo.ide.sdk.templates.Constants;

/**
 * @since 1.2
 */
public class MarketplacePackageWizard extends AbstractProjectWizard {

    public static final String ID = MarketplacePackageWizard.class.getName();

    public static final String WEBDRIVER = "webdriver";

    public static final String SELENIUM = "selenium";

    public static final String FUNKLOAD = "funkload";

    public MarketplacePackageWizard() {
        super("marketplace");
    }

    @Override
    protected WizardPage[] createPages() {
        return new WizardPage[] { new EclipseProjectPage(),
                new MarketplaceInformationPage() };
    }

    /**
     * @since 1.2 - depending on test framework selection in MP information
     *        wizard page, generate new test project(s).
     */
    @Override
    protected boolean execute(ProjectTemplateContext ctx)
            throws JavaModelException {
        if (!super.execute(ctx)) {
            return false;
        }
        if (Boolean.parseBoolean(ctx.getProperty(WEBDRIVER_ENABLED))) {
            ProjectTemplateContext initCtx = (ProjectTemplateContext) ctx.clone();
            generateProjectFromTemplate(ctx, WEBDRIVER);
            ctx = initCtx;
        }
        if (Boolean.parseBoolean(ctx.getProperty(SELENIUM_ENABLED))) {
            ProjectTemplateContext initCtx = (ProjectTemplateContext) ctx.clone();
            generateProjectFromTemplate(ctx, SELENIUM);
            ctx = initCtx;
        }
        if (Boolean.parseBoolean(ctx.getProperty(FUNKLOAD_ENABLED))) {
            generateProjectFromTemplate(ctx, FUNKLOAD);
        }
        return true;
    }

    protected boolean generateProjectFromTemplate(ProjectTemplateContext ctx,
            String frameworkTemplate) {
        ctx.setTemplate(frameworkTemplate);
        String projectId = ctx.getProperty(Constants.PROJECT_ID);
        String projectPkg = ctx.getProperty(Constants.PROJECT_PACKAGE);
        String artifactName = ctx.getProperty(Constants.ARTIFACT_NAME);
        String artifactId = ctx.getProperty(Constants.ARTIFACT_ID);
        String projectName = ctx.getProperty(Constants.PROJECT_NAME);
        File projectLocation = ctx.getProjectLocation();
        ctx.setProjectLocation(new File(projectLocation.getParentFile(),
                projectLocation.getName() + "-" + frameworkTemplate));
        ctx.put(Constants.PROJECT_ID, projectId + "-" + frameworkTemplate);
        ctx.put(Constants.PROJECT_NAME, projectName + "-" + frameworkTemplate);
        ctx.put(Constants.ARTIFACT_NAME, artifactName + "-" + frameworkTemplate);
        ctx.put(Constants.ARTIFACT_ID, artifactId + "-" + frameworkTemplate);
        ctx.put(Constants.PROJECT_PACKAGE, projectPkg + "." + frameworkTemplate);
        CreateProjectFromTemplate op = new CreateProjectFromTemplate(ctx);
        if (!CreateProjectFromTemplate.run(getShell(), getContainer(), op)) {
            return false;
        }
        return true;
    }

}
