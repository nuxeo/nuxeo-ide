/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 * Contributors: Nuxeo contributors
 */
package org.nuxeo.ide.qatests.suite.usecases;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.nuxeo.ide.qatests.dialogs.DialogBot;
import org.nuxeo.ide.qatests.dialogs.NuxeoPreferencesBot;
import org.nuxeo.ide.qatests.perspectives.NuxeoPerspectiveBot;
import org.nuxeo.ide.qatests.suite.NuxeoIDEWorkbench;
import org.nuxeo.ide.qatests.wizards.SDKPreferenceBot;

public class TestSDKConfiguration extends NuxeoIDEWorkbench {

    @Test
    public void configureSDK() throws IOException {
        // Close welcome page if exists
        workbench.viewByTitle("Welcome").close();
        // Open Nuxeo Perspective
        DialogBot.asyncOpen(workbench, NuxeoPerspectiveBot.class,
                new OpenNuxeoPerspective());
        // Open Preferences
        NuxeoPreferencesBot preferences = DialogBot.asyncOpen(workbench,
                NuxeoPreferencesBot.class, new OpenNuxeoPreferences());
        // Choose SDK
        SDKPreferenceBot sdk = preferences.select(SDKPreferenceBot.class);
        File home = new File("/Users/vladimirpasquier/Desktop/nuxeosdk");
        sdk.addAndSelect(home);
        preferences.finish();
    }

}
