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
package org.nuxeo.ide.qatests;

import java.lang.reflect.Constructor;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.nuxeo.ide.qatests.dialogs.DialogOperation;

public class ComponentBot {

    public final SWTWorkbenchBot workbench;

    public final String title;

    public SWTBotShell shell;

    public ComponentBot(SWTWorkbenchBot workbench, String title) {
        this.workbench = workbench;
        this.title = title;
    }

    public boolean isDisplayed() {
        return shell != null;
    }

    protected SWTBotShell waitShellUntil() {
        workbench.waitUntil(new DefaultCondition() {

            @Override
            public boolean test() throws Exception {
                return workbench.shell(title) != null;
            }

            @Override
            public String getFailureMessage() {
                return "Cannot find shell " + title;
            }
        });
        shell = workbench.shell(title);
        shell.activate();
        handleActivation();
        return shell;
    }

    protected void handleActivation() {
        ;
    }

    protected static <T extends ComponentBot> T newDialog(
            SWTWorkbenchBot workbench, Class<T> dialogClass) {
        try {
            Constructor<T> c = dialogClass.getConstructor(new Class<?>[] { SWTWorkbenchBot.class });
            return c.newInstance(workbench);
        } catch (Exception e) {
            throw new Error("Cannot create dialog of type "
                    + dialogClass.getName(), e);
        }
    }

    public static <T extends ComponentBot> T asyncOpen(SWTWorkbenchBot workbench,
            final Class<T> dialogClass, final DialogOperation<T> op) {
        final T dialog = newDialog(workbench, dialogClass);
        // opens the dialog asynchronous in the UI thread
        UIThreadRunnable.asyncExec(SWTUtils.display(), new VoidResult() {
            @Override
            public void run() {
                op.run(dialog);
            }
        });
        dialog.waitShellUntil();
        return dialog;
    }

    public static <T extends ComponentBot> T asyncExecute(
            SWTWorkbenchBot workbench, final Class<T> dialogClass,
            final DialogOperation<T> op) {
        final T dialog = newDialog(workbench, dialogClass);
        UIThreadRunnable.asyncExec(SWTUtils.display(), new VoidResult() {
            @Override
            public void run() {
                if (dialog.shell == null) {
                    dialog.waitShellUntil();
                }
                op.run(dialog);
            }
        });
        return dialog;
    }

}
