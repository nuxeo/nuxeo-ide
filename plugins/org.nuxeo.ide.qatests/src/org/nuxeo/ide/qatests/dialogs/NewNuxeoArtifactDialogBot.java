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
package org.nuxeo.ide.qatests.dialogs;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.nuxeo.ide.qatests.ComponentBot;
import org.nuxeo.ide.qatests.wizards.NuxeoProjectCreationWizardBot;
import org.nuxeo.ide.qatests.wizards.SeamServiceCreationWizardBot;

public class NewNuxeoArtifactDialogBot extends ComponentBot {

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

    public <T extends ComponentBot> T enterWizard(Class<T> wizardClass,
            String name, String category) {

        text.setFocus();
        text.typeText(name);
        tree.setFocus();
        SWTBotTreeItem itemCategory = tree.getTreeItem(category);
        itemCategory.expand();
        if (name.equals(category)) {
            itemCategory.select();
        } else {
            itemCategory.select(name);
        }

        return ComponentBot.asyncOpen(workbench, wizardClass,
                new DialogOperation<T>() {

                    @Override
                    public void run(T dialog) {
                        next.click();
                    }
                });
    }

    public NuxeoProjectCreationWizardBot getNuxeoProjectWizard() {
        return enterWizard(NuxeoProjectCreationWizardBot.class,
                "Nuxeo Plugin Project", "Nuxeo Plugin Project");
    }

    public SeamServiceCreationWizardBot getSeamServiceWizard() {
        return enterWizard(SeamServiceCreationWizardBot.class, "Service Bean",
                "Seam");
    }

}
