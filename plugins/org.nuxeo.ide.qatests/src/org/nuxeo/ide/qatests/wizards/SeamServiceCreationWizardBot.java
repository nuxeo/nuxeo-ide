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

import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.nuxeo.ide.qatests.dialogs.DialogBot;
import org.nuxeo.ide.qatests.dialogs.DialogOperation;
import org.nuxeo.ide.qatests.widgets.ServiceChooserBot;

public class SeamServiceCreationWizardBot extends DialogBot {

    public SeamServiceCreationWizardBot(SWTWorkbenchBot workbench) {
        super(workbench, "New Nuxeo Artifact");
    }

    SWTBotText project;

    SWTBotText pkg;

    SWTBotText srv;

    SWTBotText bean;

    SWTBotButton browseSrv;

    SWTBotButton finish;

    @Override
    protected void handleActivation() {
        project = workbench.textWithId("org.nuxeo.ide.sdk.ui.widgets.ProjectChooser");
        pkg = workbench.textWithId("org.nuxeo.ide.sdk.ui.widgets.PackageChooser");
        srv = workbench.textWithId("org.nuxeo.ide.sdk.ui.widgets.ServiceChooser");
        browseSrv = workbench.buttonWithId("org.nuxeo.ide.sdk.ui.widgets.ServiceChooser");
        bean = workbench.textWithId("component");
        finish = workbench.button("Finish");
    }

    public void typeProjectName(String name) {
        project.setFocus();
        project.setText(name);
    }

    public void typePackagePath(String path) {
        pkg.setFocus();
        pkg.setText(path);
    }

    public void selectServiceByName(String name) {
        ServiceChooserBot chooser = DialogBot.asyncOpen(workbench,
                ServiceChooserBot.class,
                new DialogOperation<ServiceChooserBot>() {

                    @Override
                    public void run(ServiceChooserBot dialog) {
                        browseSrv.click();
                    }
                });
        chooser.selectByName(name);
        assertTrue(srv.getText().contains(name));
    }

    public void typeBeanName(String name) {
        bean.setFocus();
        bean.typeText(name);
    }

    public void finish() {
        assertTrue(finish.isEnabled());
        finish.setFocus();
        finish.click();
    }

    public void fillAndFinish(String projectName, String pkgPath, String srvName) {
        typeProjectName(projectName);
        typePackagePath(pkgPath);
        selectServiceByName(srvName);
        bean.setFocus();
        finish();
    }
}
