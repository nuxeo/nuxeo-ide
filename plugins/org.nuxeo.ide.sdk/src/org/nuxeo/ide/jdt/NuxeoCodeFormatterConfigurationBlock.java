/*
 * (C) Copyright 2013-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 * Contributors:
 *     Sun Seng David TAN <stan@nuxeo.com>
 */
package org.nuxeo.ide.jdt;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.jdt.internal.ui.preferences.formatter.CodeFormatterConfigurationBlock;
import org.eclipse.jdt.internal.ui.preferences.formatter.IProfileVersioner;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.CustomProfile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileManager.Profile;
import org.eclipse.jdt.internal.ui.preferences.formatter.ProfileStore;
import org.xml.sax.InputSource;

import org.nuxeo.ide.common.UI;

/**
 * Code formatter configuration block with a method that set Nuxeo's formatter configuration.
 *
 * @author Sun Seng David TAN <stan@nuxeo.com>
 */
@SuppressWarnings("restriction")
public class NuxeoCodeFormatterConfigurationBlock extends CodeFormatterConfigurationBlock {

    public NuxeoCodeFormatterConfigurationBlock(IProject project, PreferencesAccess access) {
        super(project, access);
    }

    IProfileVersioner profileVersioner;

    ProfileStore profileStore;

    ProfileManager profileManager;

    @Override
    protected ProfileStore createProfileStore(IProfileVersioner versioner) {
        // just keeping the private variable at creation time
        profileStore = super.createProfileStore(versioner);
        return profileStore;
    }

    @Override
    protected IProfileVersioner createProfileVersioner() {
        // just keeping the private variable at creation time
        profileVersioner = super.createProfileVersioner();
        return profileVersioner;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    protected ProfileManager createProfileManager(List profiles, IScopeContext context, PreferencesAccess access,
            IProfileVersioner aProfileVersioner) {
        // just keeping the private variable at creation time
        profileManager = super.createProfileManager(profiles, context, access, aProfileVersioner);
        return profileManager;
    }

    @SuppressWarnings("static-access")
    public void setNuxeoCodeFormatterProfile() {

        List<Profile> profiles = null;
        PreferenceFilesStreamProvider preferenceFilesStreamProvider = new PreferenceFilesStreamProvider(
                "nuxeo_formatter.xml");
        try {
            profiles = profileStore.readProfilesFromStream(new InputSource(
                    preferenceFilesStreamProvider.getInputStream()));
        } catch (CoreException e) {
            try {
                profiles = profileStore.readProfilesFromStream(new InputSource(
                        preferenceFilesStreamProvider.getFallbackStream()));
            } catch (CoreException e1) {
                UI.showError("An error occurred while reading Nuxeo Cleanup profiles", e);
            }
        }
        if (profiles == null || profiles.isEmpty()) {
            return;
        }
        Iterator<Profile> iter = profiles.iterator();
        while (iter.hasNext()) {
            final CustomProfile profile = (CustomProfile) iter.next();

            if (!profileVersioner.getProfileKind().equals(profile.getKind())) {
                UI.showError("Not the same profile kind", "Not the same profile kind");
                return;
            }

            if (profile.getVersion() > profileVersioner.getCurrentVersion()) {
                UI.showError("Profile version too new", "Profile version too new");
                return;
            }
            profileVersioner.update(profile);
            profileManager.addProfile(profile);

        }
    }
}
