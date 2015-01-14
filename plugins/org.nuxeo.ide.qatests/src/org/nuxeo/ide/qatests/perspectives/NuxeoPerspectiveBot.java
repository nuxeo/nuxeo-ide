package org.nuxeo.ide.qatests.perspectives;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.nuxeo.ide.qatests.dialogs.DialogBot;

public class NuxeoPerspectiveBot extends DialogBot {

    public NuxeoPerspectiveBot(SWTWorkbenchBot workbench) {
        super(workbench, "Open Perspective");
    }

    @Override
    protected void handleActivation() {
        // select the Nuxeo perspective
        workbench.table().select("Nuxeo");
        workbench.button("OK").click();
    }
}
