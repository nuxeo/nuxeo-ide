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

import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.CMIS_ENABLED;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.DISTRIB_VERSION;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.FUNKLOAD_ENABLED;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.LICENSE_URL;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_DESCRIPTION;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_DISTRIBUTION;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_DISTRIBUTION_ARTIFACT_ID;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_GROUPID;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_HOME_PAGE;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_HOT_RELOAD_SUPPORT;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_INSTALL_RESTART;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_LICENSE;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_LICENSE_URL;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_NAME;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_POM_VERSION_VARIABLE;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_REQUIRE_TERM_AND_CONDITION;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_TITLE;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_UNINSTALL_RESTART;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_VENDOR;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.MP_VERSION;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.SELENIUM_ENABLED;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.TEST_ENABLED;
import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.WEBDRIVER_ENABLED;

import org.eclipse.swt.widgets.Composite;
import org.nuxeo.ide.common.forms.HasValue;
import org.nuxeo.ide.common.forms.UIObject;
import org.nuxeo.ide.common.wizards.FormWizardPage;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.projects.ProjectTemplateContext;
import org.nuxeo.ide.sdk.templates.Constants;

/**
 * @since 1.2
 */
public class MarketplaceInformationPage extends
        FormWizardPage<ProjectTemplateContext> implements Constants {

    public MarketplaceInformationPage() {
        super("permission1", "Define Marketplace Information", null);
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        UIObject<?> obj = form.getWidget(DISTRIB_VERSION);
        ((HasValue) obj).setValue(NuxeoSDK.getDefault().getVersion());
    }

    @Override
    public void update(ProjectTemplateContext ctx) {
        // Add values set into the templating computation
        ctx.setPropertyIfNotNull(form, MP_TITLE);
        ctx.setPropertyIfNotNull(form, MP_NAME);
        ctx.setPropertyIfNotNull(form, DISTRIB_VERSION);
        ctx.setPropertyIfNotNull(form, MP_HOME_PAGE);
        ctx.setPropertyIfNotNull(form, MP_VERSION);
        ctx.setPropertyIfNotNull(form, MP_VENDOR);
        ctx.setPropertyIfNotNull(form, MP_DESCRIPTION);
        ctx.setPropertyIfNotNull(form, MP_LICENSE);
        ctx.setPropertyIfNotNull(form, MP_DISTRIBUTION);
        ctx.setPropertyIfNotNull(form, MP_INSTALL_RESTART);
        ctx.setPropertyIfNotNull(form, MP_UNINSTALL_RESTART);
        ctx.setPropertyIfNotNull(form, MP_HOT_RELOAD_SUPPORT);
        ctx.setPropertyIfNotNull(form, MP_REQUIRE_TERM_AND_CONDITION);
        ctx.setPropertyIfNotNull(form, WEBDRIVER_ENABLED);
        ctx.setPropertyIfNotNull(form, SELENIUM_ENABLED);
        ctx.setPropertyIfNotNull(form, FUNKLOAD_ENABLED);
        ctx.setPropertyIfNotNull(form, CMIS_ENABLED);

        addComputedValues(ctx);
    }

    private void addComputedValues(ProjectTemplateContext ctx) {
        String name = ctx.getProperty(MP_NAME);
        String parentGroupId = ctx.getProperty(PARENT_GROUP_ID);
        String license = form.getWidgetValueAsString(MP_LICENSE);
        String distribution = form.getWidgetValueAsString(MP_DISTRIBUTION);

        if (LICENSE_URL.containsKey(license)) {
            ctx.setProperty(MP_LICENSE_URL, LICENSE_URL.get(license));
        }

        ctx.setProperty(MP_DISTRIBUTION, distribution);
        ctx.setProperty(MP_GROUPID, parentGroupId + ".marketplace");
        ctx.setProperty(MP_POM_VERSION_VARIABLE, "marketplace." + name
                + ".version");
        ctx.setProperty(MP_DISTRIBUTION_ARTIFACT_ID, "nuxeo-distribution-"
                + distribution);

        Boolean testEnabled = form.getWidgetValueAsBoolean(WEBDRIVER_ENABLED)
                || form.getWidgetValueAsBoolean(SELENIUM_ENABLED)
                || form.getWidgetValueAsBoolean(FUNKLOAD_ENABLED)
                || form.getWidgetValueAsBoolean(CMIS_ENABLED);
        ctx.setProperty(TEST_ENABLED, testEnabled.toString());
    }
}
