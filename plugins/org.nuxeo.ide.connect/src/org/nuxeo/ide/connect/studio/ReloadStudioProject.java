/*
 * (C) Copyright 2006-2013 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Vladimir Pasquier <vpasquier@nuxeo.com>
 */
package org.nuxeo.ide.connect.studio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.nuxeo.ide.connect.ConnectPlugin;
import org.nuxeo.ide.connect.StudioProvider;
import org.nuxeo.ide.sdk.DeploymentChangedListener;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.deploy.Deployment;

/**
 * @since 1.1.11 Listener triggering the studio project download when deploying
 *        on sdk server.
 */
public class ReloadStudioProject implements DeploymentChangedListener {

    @Override
    public void deploymentChanged(NuxeoSDK sdk, Deployment deployment) {
        StudioProvider provider = ConnectPlugin.getStudioProvider();
        List<String> studioProjectNames = new ArrayList<String>();
        for (IProject project : deployment.getProjects()) {
            String[] projectStudioBoundIds = provider.getBindingManager().getBinding(
                    project).getProjectIds();
            studioProjectNames.addAll(Arrays.asList(projectStudioBoundIds));
        }
        provider.getRepositoryManager().handleDeploymentProjectsUpdate(
                studioProjectNames);
    }
}
