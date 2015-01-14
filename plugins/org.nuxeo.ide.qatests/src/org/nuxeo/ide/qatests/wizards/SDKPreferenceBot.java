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
package org.nuxeo.ide.qatests.wizards;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.nuxeo.ide.qatests.Bot;
import org.nuxeo.ide.sdk.SDKInfo;
import org.nuxeo.ide.sdk.server.ui.SDKTableWidget;

public class SDKPreferenceBot implements Bot {

    protected final SWTWorkbenchBot workbench;

    protected final SWTBotTable sdks;

    public SDKPreferenceBot(SWTWorkbenchBot workbench) {
        this.workbench = workbench;
        sdks = workbench.table();
    }

    public void addSDK(final File home) throws IOException {
        final SDKInfo info = SDKInfo.loadSDK(home);
        UIThreadRunnable.asyncExec(SWTUtils.display(), new VoidResult() {
            @Override
            public void run() {
                SDKTableWidget w = (SDKTableWidget) sdks.widget.getData("org.eclipse.swtbot.widget.owner");
                CheckboxTableViewer v = w.getViewer();
                v.add(info);
            }
        });

    }

    public boolean selectSDK(File home) throws IOException {
        String path = home.getPath();
        int count = sdks.rowCount();
        for (int row = 0; row < count; ++row) {
            SWTBotTableItem item = sdks.getTableItem(row);
            if (path.equals(item.getText(2))) {
                item.check();
                return true;
            }
        }
        return false;
    }

    public void addAndSelect(File home) throws IOException {
        if (selectSDK(home) == false) {
            addSDK(home);
        }
        selectSDK(home);
    }

}
