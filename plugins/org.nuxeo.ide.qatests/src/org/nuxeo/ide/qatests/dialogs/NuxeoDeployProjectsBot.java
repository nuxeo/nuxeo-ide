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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;

public class NuxeoDeployProjectsBot extends DialogAreaBot {

    protected SWTBotButton ok;

    protected SWTBotButton selectAll;

    protected SWTBotToolbarButton add;

    public NuxeoDeployProjectsBot(SWTWorkbenchBot workbench) {
        super(workbench, "Deployment Configurations");
    }

    @Override
    protected void handleActivation() {
        add = workbench.toolbarButton("Add");
        add.setFocus();
        add.click();
        selectAll = workbench.button("Select/Unselect All");
        ok = workbench.button("OK");
        selectAll.setFocus();
        selectAll.click();
        ok.setFocus();
        ok.click();
    }
}
