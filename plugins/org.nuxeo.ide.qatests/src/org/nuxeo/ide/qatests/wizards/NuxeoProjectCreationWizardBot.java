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
