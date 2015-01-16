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
package org.nuxeo.ide.qatests.views;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.nuxeo.ide.qatests.ComponentBot;

public class ViewBot extends ComponentBot {

    public SWTBotShell shell;

    public SWTBotView activeView;

    public ViewBot(SWTWorkbenchBot workbench, String title) {
        super(workbench, title);
    }

    @Override
    protected SWTBotShell waitShellUntil() {
        workbench.waitUntil(new DefaultCondition() {

            @Override
            public boolean test() throws Exception {
                return workbench.viewByTitle(title).bot().activeShell() != null;
            }

            @Override
            public String getFailureMessage() {
                return "Cannot find shell " + title;
            }
        });
        shell = workbench.viewByTitle(title).bot().activeShell();
        shell.activate();
        activeView = workbench.activeView();
        handleActivation();
        return shell;
    }

}
