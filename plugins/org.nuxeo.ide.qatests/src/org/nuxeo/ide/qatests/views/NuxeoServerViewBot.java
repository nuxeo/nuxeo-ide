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

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.nuxeo.ide.qatests.ComponentBot;
import org.nuxeo.ide.qatests.dialogs.DialogOperation;

public class NuxeoServerViewBot extends ViewBot {

    public NuxeoServerViewBot(SWTWorkbenchBot workbench) {
        super(workbench, "Nuxeo Server");
    }

    SWTBotToolbarButton run;

    SWTBotToolbarButton stop;

    SWTBotToolbarButton hotreload;

    SWTBotToolbarButton debug;

    SWTBotToolbarButton deploy;

    SWTBotToolbarButton clear;

    @Override
    protected void handleActivation() {
        run = activeView.toolbarButton("Start the server");
        deploy = activeView.toolbarButton("Select projects to deploy on server");
        hotreload = activeView.toolbarButton("Reload projects on server");
        debug = activeView.toolbarButton("Start the server in debug mode");
        stop = activeView.toolbarButton("Stop the server");
        clear = activeView.toolbarButton("Clear the server console");
    }

    public void run() {
        assertTrue(run.isEnabled());
        run.setFocus();
        run.click();
    }

    public void stop() {
        assertTrue(stop.isEnabled());
        stop.setFocus();
        stop.click();
    }

    public void clearConsole() {
        assertTrue(clear.isEnabled());
        clear.setFocus();
        clear.click();
    }

    public void hotreload() {
        assertTrue(hotreload.isEnabled());
        hotreload.setFocus();
        hotreload.click();
    }

    public <T extends ComponentBot> T deploy(Class<T> dialogClass) {
        return ComponentBot.asyncOpen(workbench, dialogClass,
                new DialogOperation<T>() {
                    @Override
                    public void run(T dialog) {
                        deploy.click();
                    }
                });
    }

    public void playRun() {
        workbench.waitUntil(new ICondition() {
            @Override
            public boolean test() throws Exception {
                return stop.isEnabled();
            }

            @Override
            public void init(SWTBot bot) {
                run();
            }

            @Override
            public String getFailureMessage() {
                return "Nuxeo server starting failure";
            }
        }, 50000);
    }

    public void playStop() {
        workbench.waitUntil(new ICondition() {
            @Override
            public boolean test() throws Exception {
                return run.isEnabled();
            }

            @Override
            public void init(SWTBot bot) {
                stop();
            }

            @Override
            public String getFailureMessage() {
                return "Nuxeo server stopping failure";
            }
        }, 20000);
    }

    public void playHotReload() {
        workbench.waitUntil(new ICondition() {
            @Override
            public boolean test() throws Exception {
                return workbench.activeView().bot().styledText().getText().contains(
                        "Deploy done for bundle with name 'project'");
            }

            @Override
            public void init(SWTBot bot) {
                hotreload();
            }

            @Override
            public String getFailureMessage() {
                return "Nuxeo server hotreload failure";
            }
        });
    }
}
