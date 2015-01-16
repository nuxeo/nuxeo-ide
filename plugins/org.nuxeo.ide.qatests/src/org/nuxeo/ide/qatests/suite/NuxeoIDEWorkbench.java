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
package org.nuxeo.ide.qatests.suite;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.nuxeo.ide.qatests.ComponentBot;
import org.nuxeo.ide.qatests.dialogs.DialogOperation;
import org.nuxeo.ide.qatests.dialogs.NewNuxeoArtifactDialogBot;
import org.nuxeo.ide.qatests.dialogs.NuxeoPreferencesBot;
import org.nuxeo.ide.qatests.perspectives.NuxeoPerspectiveBot;
import org.nuxeo.ide.qatests.views.NuxeoServerViewBot;

public class NuxeoIDEWorkbench {

    protected static final String NUXEOSDK_PATH = "/Users/vladimirpasquier/Desktop/nuxeosdk";

    protected SWTWorkbenchBot workbench = new SWTWorkbenchBot();

    public NuxeoPreferencesBot getPreferences() {
        return ComponentBot.asyncOpen(workbench, NuxeoPreferencesBot.class,
                new OpenNuxeoPreferences());
    }

    public void openNuxeoPerspective() {
        ComponentBot.asyncOpen(workbench, NuxeoPerspectiveBot.class,
                new OpenNuxeoPerspective());
    }

    public NewNuxeoArtifactDialogBot getNuxeoWizardsDialog() {
        return ComponentBot.asyncOpen(workbench,
                NewNuxeoArtifactDialogBot.class, new OpenNewNuxeoArtifact());
    }

    public NuxeoServerViewBot activateServerView() {
        return ComponentBot.asyncOpen(workbench, NuxeoServerViewBot.class,
                new ActivateServerView());
    }

    private class OpenNewNuxeoArtifact implements
            DialogOperation<NewNuxeoArtifactDialogBot> {
        @Override
        public void run(NewNuxeoArtifactDialogBot dialog) {
            dialog.workbench.menu("Nuxeo").menu("Nuxeo Artifact").click();
        }
    }

    private class OpenNuxeoPerspective implements
            DialogOperation<NuxeoPerspectiveBot> {
        @Override
        public void run(NuxeoPerspectiveBot dialog) {
            // Change the perspective via the Open Perspective dialog
            dialog.workbench.menu("Window").menu("Open Perspective").menu(
                    "Other...").click();
        }
    }

    private class OpenNuxeoPreferences implements
            DialogOperation<NuxeoPreferencesBot> {
        @Override
        public void run(NuxeoPreferencesBot dialog) {
            Menu appMenu = workbench.getDisplay().getSystemMenu();
            for (MenuItem item : appMenu.getItems()) {
                if (item.getText().startsWith("Preferences")) {
                    Event event = new Event();
                    event.time = (int) System.currentTimeMillis();
                    event.widget = item;
                    event.display = workbench.getDisplay();
                    item.setSelection(true);
                    item.notifyListeners(SWT.Selection, event);
                    break;
                }
            }
        }
    }

    private class ActivateServerView implements
            DialogOperation<NuxeoServerViewBot> {
        @Override
        public void run(NuxeoServerViewBot dialog) {
            dialog.workbench.viewByTitle("Nuxeo Server").setFocus();
        }
    }

}
