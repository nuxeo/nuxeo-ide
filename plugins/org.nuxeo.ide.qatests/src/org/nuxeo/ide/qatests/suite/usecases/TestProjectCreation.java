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

import org.junit.Test;
import org.nuxeo.ide.qatests.dialogs.DialogBot;
import org.nuxeo.ide.qatests.dialogs.NewNuxeoArtifactDialogBot;
import org.nuxeo.ide.qatests.suite.NuxeoIDEWorkbench;
import org.nuxeo.ide.qatests.wizards.NuxeoProjectCreationWizardBot;

public class TestProjectCreation extends NuxeoIDEWorkbench {

    @Test
    public void testProjectCreation() throws InterruptedException {
        // Verify if workbench active
        assertNotNull(workbench.activeShell());

        // Open Nuxeo Wizard dialog
        NewNuxeoArtifactDialogBot dialog = DialogBot.asyncOpen(workbench,
                NewNuxeoArtifactDialogBot.class, new OpenNewNuxeoArtifact());

        // Create Nuxeo Project
        NuxeoProjectCreationWizardBot wizard = dialog.enterWizard(
                NuxeoProjectCreationWizardBot.class, "Nuxeo Plugin Project",
                "Nuxeo Plugin Project");
        wizard.fillAndFinish("project");
        workbench.tree().getTreeItem("project").expand();
    }

}
