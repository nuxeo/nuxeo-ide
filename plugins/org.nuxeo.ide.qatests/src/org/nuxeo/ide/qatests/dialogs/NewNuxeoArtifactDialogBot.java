package org.nuxeo.ide.qatests.dialogs;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;

public class NewNuxeoArtifactDialogBot extends DialogBot {

    protected SWTBotText text;

    protected SWTBotTree tree;

    protected SWTBotButton cancel;

    protected SWTBotButton next;

    @Override
    protected void handleActivation() {
        text = workbench.text();
        tree = workbench.tree();
        cancel = workbench.button("Cancel");
        next = workbench.button("Next >");
    }

    public NewNuxeoArtifactDialogBot(SWTWorkbenchBot workbench) {
        super(workbench, "New Nuxeo Artifact");
    }

    public <T extends DialogBot> T enterWizard(Class<T> wizardClass, String name, String category) {

        text.setFocus();
        text.typeText(name);
        tree.setFocus();
        SWTBotTreeItem itemCategory = tree.getTreeItem(category);
        itemCategory.expand();
        if(name.equals(category)){
            itemCategory.select();
        }else{
            itemCategory.select(name);
        }

        return DialogBot.asyncOpen(workbench, wizardClass,
                new DialogOperation<T>() {

                    @Override
                    public void run(T dialog) {
                        next.click();
                    }
                });
    }

}
