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

import static org.nuxeo.ide.sdk.projects.marketplace.MartketplaceWizardConstants.*;

import org.nuxeo.ide.common.wizards.FormWizardPage;
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
    public void update(ProjectTemplateContext ctx) {
        // Add values set into the templating computation
        ctx.setPropertyIfNotNull(form, MP_TITLE);
        ctx.setPropertyIfNotNull(form, MP_NAME);
        ctx.setPropertyIfNotNull(form, PARENT_GROUP_ID);
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
        String title = form.getWidgetValueAsString(MP_TITLE);
        String name = form.getWidgetValueAsString(MP_NAME);
        String parentGroupId = form.getWidgetValueAsString(PARENT_GROUP_ID);
        String license = form.getWidgetValueAsString(MP_LICENSE);
        String distribution = form.getWidgetValueAsString(MP_DISTRIBUTION);

        if (LICENSE_URL.containsKey(license)) {
            ctx.setProperty(MP_LICENSE_URL, LICENSE_URL.get(license));
        }

        ctx.setProperty(MP_GROUPID, parentGroupId + ".marketplace");
        ctx.setProperty(MP_PARENT_ARTIFACT_ID, "Nuxeo Marketplace Parent Project for " + title);
        ctx.setProperty(MP_ARTIFACT_ID, "Nuxeo Marketplace packaging Project for " + title);
        ctx.setProperty(MP_POM_VERSION_VARIABLE, "marketplace." + name + ".version");
        ctx.setProperty(MP_DISTRIBUTION_ARTIFACT_ID, "nuxeo-distribution-" + distribution);

        Boolean testEnabled = form.getWidgetValueAsBoolean(WEBDRIVER_ENABLED)
                || form.getWidgetValueAsBoolean(SELENIUM_ENABLED)
                || form.getWidgetValueAsBoolean(FUNKLOAD_ENABLED)
                || form.getWidgetValueAsBoolean(CMIS_ENABLED);
        ctx.setProperty(TEST_ENABLED, testEnabled.toString());

    }
}
