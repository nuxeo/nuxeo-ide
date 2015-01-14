package org.nuxeo.ide.qatests.usecases;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ide.qatests.dialogs.DialogBot;
import org.nuxeo.ide.qatests.dialogs.DialogOperation;
import org.nuxeo.ide.qatests.dialogs.NewNuxeoArtifactDialogBot;
import org.nuxeo.ide.qatests.dialogs.NuxeoPreferencesBot;
import org.nuxeo.ide.qatests.perspectives.NuxeoPerspectiveBot;
import org.nuxeo.ide.qatests.wizards.NuxeoProjectCreationWizardBot;
import org.nuxeo.ide.qatests.wizards.SDKPreferenceBot;

@RunWith(SWTBotJunit4ClassRunner.class)
public class TestNuxeoIDESuite {

    SWTWorkbenchBot workbench = new SWTWorkbenchBot();

    public class OpenNewNuxeoArtifact implements
            DialogOperation<NewNuxeoArtifactDialogBot> {
        @Override
        public void run(NewNuxeoArtifactDialogBot dialog) {
            dialog.workbench.menu("Nuxeo").menu("Nuxeo Artifact").click();
        }
    }

    public class OpenNuxeoPerspective implements
            DialogOperation<NuxeoPerspectiveBot> {
        @Override
        public void run(NuxeoPerspectiveBot dialog) {
            // Change the perspective via the Open Perspective dialog
            workbench.menu("Window").menu("Open Perspective").menu("Other...").click();
        }
    }

    public class OpenNuxeoPreferences implements
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

    @Before
    public void configureSDK() throws IOException {
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

    @Test
    public void TestHotreload() {
        // Verify if workbench active
        assertNotNull(workbench.activeShell());

        // Open Nuxeo Wizard dialog
        NewNuxeoArtifactDialogBot dialog = DialogBot.asyncOpen(workbench,
                NewNuxeoArtifactDialogBot.class, new OpenNewNuxeoArtifact());

        // Create Nuxeo Project
        NuxeoProjectCreationWizardBot wizard = dialog.enterWizard(NuxeoProjectCreationWizardBot.class, "Nuxeo Plugin Project", "Nuxeo Plugin Project");
        wizard.fillAndFinish("project");
    }

}
