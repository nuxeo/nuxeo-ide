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
import org.nuxeo.ide.qatests.dialogs.DialogBot;

public class ServiceChooserBot extends DialogBot {

    protected SWTBotText filter;

    protected SWTBotTable services;

    protected SWTBotButton ok;

    public ServiceChooserBot(SWTWorkbenchBot workbench) {
        super(workbench, "Service Selection");
    }

    @Override
    protected void handleActivation() {
        filter = workbench.text();
        services = workbench.table();
        ok = workbench.button("OK");
    }

    public void selectByName(final String name) {
        filter.setFocus();
        filter.typeText(name);
        workbench.waitWhile(new ICondition() {

            @Override
            public boolean test() throws Exception {
                return services.rowCount() != 1;
            }

            @Override
            public void init(SWTBot bot) {
                ;
            }

            @Override
            public String getFailureMessage() {
                if (services.rowCount() > 1) {
                    return "Too much services selected (" + services.rowCount()
                            + ") for " + name;
                }
                return "No matching service selected for " + name;
            }

        });
        services.setFocus();
        services.select(0);
        ok.setFocus();
        ok.click();
    }
}
