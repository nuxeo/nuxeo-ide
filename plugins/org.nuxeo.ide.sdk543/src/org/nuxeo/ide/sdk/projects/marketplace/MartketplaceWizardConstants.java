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

import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.2
 */
public class MartketplaceWizardConstants {

    public static final String MP_TITLE = "mpTitle";

    public static final String MP_NAME = "mpName";

    public static final String MP_HOME_PAGE = "mpHomePage";

    public static final String MP_VERSION = "mpVersion";

    public static final String DISTRIB_VERSION = "parentVersion";

    public static final String MP_VENDOR = "mpVendor";

    public static final String MP_DISTRIBUTION = "mpDistribution";

    public static final String MP_DESCRIPTION = "mpDescription";

    public static final String MP_LICENSE = "mpLicense";

    public static final String MP_INSTALL_RESTART = "mpInstallRestart";

    public static final String MP_UNINSTALL_RESTART = "mpUninstallRestart";

    public static final String MP_HOT_RELOAD_SUPPORT = "mpHotReloadSupport";

    public static final String MP_REQUIRE_TERM_AND_CONDITION = "mpRequireTermAndConditionAcceptance";

    public static final String WEBDRIVER_ENABLED = "webdriver";

    public static final String SELENIUM_ENABLED = "selenium";

    public static final String FUNKLOAD_ENABLED = "funkload";

    public static final String CMIS_ENABLED = "cmis";

    // COMPUTED VALUES

    /**
     * ${groupId}.markeplace
     */
    public static final String MP_GROUPID = "mpGroupId";

    /**
     * LICENSE_URL.get(MP_LICENSE)
     */
    public static final String MP_LICENSE_URL = "mpLicenseUrl";

    /**
     * Nuxeo Marketplace Parent Project for ${mpTitle}
     */
    public static final String MP_PARENT_ARTIFACT_ID = "mpParentArtifactId";

    /**
     * Nuxeo Marketplace packaging Project for ${mpTitle}
     */
    public static final String MP_ARTIFACT_ID = "mpMarketplaceArtifactId";

    /**
     * marketplace.${mpName}.version
     */
    public static final String MP_POM_VERSION_VARIABLE = "mpVersionVariable";

    /**
     * nuxeo-distribution-${mpDistribution}
     */
    public static final String MP_DISTRIBUTION_ARTIFACT_ID = "mpDistributionArtifactId";

    /**
     * true if one test is enabled
     */
    public static final String TEST_ENABLED = "testEnabled";

    public static final Map<String, String> LICENSE_URL = new HashMap<String, String>();

    static {
        LICENSE_URL.put("LGPL 2.1", "http://www.gnu.org/licenses/lgpl-2.1.html");
        LICENSE_URL.put("EPL 1.0", "http://www.eclipse.org/legal/epl-v10.html");
        LICENSE_URL.put("AL 2.0",
                "http://www.apache.org/licenses/LICENSE-2.0.html");
        LICENSE_URL.put("AL 1.1", "http://www.apache.org/licenses/LICENSE-1.1");
        LICENSE_URL.put("BSD 2",
                "http://opensource.org/licenses/bsd-license.php");
        LICENSE_URL.put("BSD 3", "http://opensource.org/licenses/BSD-3-Clause");
        LICENSE_URL.put("CC BY 2.5",
                "http://creativecommons.org/licenses/by/2.5/");
        LICENSE_URL.put("CDDL 1.0",
                "http://glassfish.java.net/public/CDDLv1.0.html");
        LICENSE_URL.put("CDDL 1.1",
                "http://glassfish.java.net/public/CDDL+GPL_1_1.html");
        LICENSE_URL.put("LGPL 3", "http://www.gnu.org/licenses/lgpl.html");
        LICENSE_URL.put("MIT", "http://opensource.org/licenses/mit-license.php");
        LICENSE_URL.put("W3C",
                "http://www.w3.org/Consortium/Legal/2002/copyright-software-20021231");
        LICENSE_URL.put("Other", "Give here the License description URL.");

    }

}
