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

import static org.junit.Assert.assertNotNull;

import org.junit.Assert;
import org.junit.Test;
import org.nuxeo.ide.qatests.dialogs.DialogBot;
import org.nuxeo.ide.qatests.dialogs.NuxeoDeployProjectsBot;
import org.nuxeo.ide.qatests.suite.NuxeoIDEWorkbench;
import org.nuxeo.ide.qatests.views.NuxeoServerViewBot;

public class TestHotReload extends NuxeoIDEWorkbench {

    @Test
    public void testHotreload() throws InterruptedException {
        // Verify if workbench active
        assertNotNull(workbench.activeShell());

        // Active Nuxeo Server View
        NuxeoServerViewBot dialog = DialogBot.asyncOpen(workbench,
                NuxeoServerViewBot.class, new ActivateServerView());

        // Manage the server - test hotreload
        // Deploy project
        dialog.deploy(NuxeoDeployProjectsBot.class);
        // Start the server and wait until fully started.
        dialog.playRun();
        // Clear console
        dialog.clearConsole();
        // Hotreload server.
        dialog.playHotReload();
        // Check if no error in console
        Assert.assertTrue(!workbench.activeView().bot().styledText().getText().contains(
                "ERROR"));
        // Stop the server
        dialog.playStop();
        // Check if server stopped in console
        Assert.assertTrue(workbench.activeView().bot().styledText().getText().contains(
                "Nuxeo Server Stopped"));
    }

}
