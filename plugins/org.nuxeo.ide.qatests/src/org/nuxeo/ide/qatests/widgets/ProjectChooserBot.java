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
package org.nuxeo.ide.qatests.widgets;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.nuxeo.ide.qatests.ComponentBot;

public class ProjectChooserBot extends ComponentBot {

    protected SWTBotText filter;

    protected SWTBotTable projects;

    protected SWTBotButton ok;

    public ProjectChooserBot(SWTWorkbenchBot workbench) {
        super(workbench, "Project selection");
    }

    @Override
    protected void handleActivation() {
        filter = workbench.text();
        projects = workbench.table();
        ok = workbench.button("OK");
    }

    public void selectByName(final String name) {
        filter.setFocus();
        filter.typeText(name);
        workbench.waitUntil(new ICondition() {

            @Override
            public boolean test() throws Exception {
                return projects.rowCount() != 1;
            }

            @Override
            public void init(SWTBot bot) {
                ;
            }

            @Override
            public String getFailureMessage() {
                if (projects.rowCount() == 0) {
                    return "No matching projects for " + name;
                }
                return "Too much projects selected (" + projects.rowCount()
                        + ") for " + name;
            }

        });
        projects.setFocus();
        projects.select(0);
        ok.setFocus();
        ok.click();
    }
}
