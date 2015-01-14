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
package org.nuxeo.ide.qatests.wizards;

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.nuxeo.ide.qatests.dialogs.DialogBot;

public class NuxeoProjectCreationWizardBot extends DialogBot {

    public NuxeoProjectCreationWizardBot(SWTWorkbenchBot workbench) {
        super(workbench, "New Nuxeo Artifact");
    }

    SWTBotText project;

    SWTBotButton finish;

    SWTBotButton next;

    @Override
    protected void handleActivation() {
        project = workbench.textWithId("projectId");
        next = workbench.button("Next >");
        finish = workbench.button("Finish");
    }

    public void typeProjectName(String name) {
        project.setText(name);
        project.setFocus();
    }

    public void next() {
        assertTrue(next.isEnabled());
        next.setFocus();
        next.click();
    }

    public void finish() {
        assertTrue(finish.isEnabled());
        finish.setFocus();
        finish.click();
    }

    public void fillAndFinish(String projectName) {
        typeProjectName(projectName);
        next();
        finish();
    }
}
