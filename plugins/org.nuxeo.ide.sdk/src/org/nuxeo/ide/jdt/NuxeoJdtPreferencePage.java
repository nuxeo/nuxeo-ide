/*
 * (C) Copyright 2013-2014 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License (LGPL)
 * version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Contributors: Sun Seng David TAN <stan@nuxeo.com>
 */
package org.nuxeo.ide.jdt;

import java.io.IOException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.CleanUpPostSaveListener;
import org.eclipse.jdt.internal.corext.fix.CleanUpPreferenceUtil;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.jdt.internal.ui.viewsupport.ProjectTemplateStore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.cleanup.CleanUpOptions;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateReaderWriter;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

import org.nuxeo.ide.common.UI;

/**
 * Preference page with a button that perform Nuxeo contributors java editor settings.
 * <ul>
 * <li>cleanup</p>
 * <li>code template</li>
 * <li>formatter</li>
 * <li>java editor template</li>
 * <li>cleanup on save</li>
 */
@SuppressWarnings("restriction")
public class NuxeoJdtPreferencePage extends PropertyAndPreferencePage {

    public static final java.lang.String EDITOR_SAVE_PARTICIPANT_PREFIX = "editor_save_participant_";

    public static final String PREF_ID = NuxeoJdtPreferencePage.class.getName();

    public static final String PROP_ID = NuxeoJdtPreferencePage.class.getName();

    protected NuxeoCleanUpConfigurationBlock cleanupConfigurationBlock;

    protected NuxeoCodeFormatterConfigurationBlock codeFormatterConfigurationBlock;

    ProjectTemplateStore templateStore;

    private PreferencesAccess fAccess;

    @Override
    protected Control createPreferenceContent(Composite composite) {
        // Reusing Eclipse JDT configuration block to setup our custom
        // configuration
        // Preparing access to preference store
        IPreferencePageContainer container = getContainer();
        IWorkingCopyManager workingCopyManager;
        if (container instanceof IWorkbenchPreferenceContainer) {
            workingCopyManager = ((IWorkbenchPreferenceContainer) container).getWorkingCopyManager();
        } else {
            workingCopyManager = new WorkingCopyManager(); // non shared
        }
        fAccess = PreferencesAccess.getWorkingCopyPreferences(workingCopyManager);
        cleanupConfigurationBlock = new NuxeoCleanUpConfigurationBlock(getProject(), fAccess);
        codeFormatterConfigurationBlock = new NuxeoCodeFormatterConfigurationBlock(getProject(), fAccess);
        templateStore = new ProjectTemplateStore(getProject());

        // building pageContainer
        Composite pageContainer = new Composite(composite, SWT.NONE);
        pageContainer.setLayout(new GridLayout(1, false));

        Label text = new Label(pageContainer, SWT.NONE);
        text.setText("Set Nuxeo contributors default java development settings: formatter, cleanup, cleanup on save, code templates and Java editor code templates");
        text.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

        Button button = new Button(pageContainer, SWT.NONE);
        button.setText("Setting Nuxeo contributors settings");
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));

        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                performNuxeoConfigurationImport();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        return pageContainer;
    }

    protected void performNuxeoConfigurationImport() {
        cleanupConfigurationBlock.setNuxeoCleanupProfile();
        codeFormatterConfigurationBlock.setNuxeoCodeFormatterProfile();
        importNuxeoTemplates();
        importNuxeoJavaEditorTemplates();
        // set save participant preference for cleanup
        setCleanupSaveParticipant();
    }

    @Override
    protected boolean hasProjectSpecificOptions(IProject project) {
        return false;
    }

    @Override
    protected String getPreferencePageID() {
        return PREF_ID;
    }

    @Override
    protected String getPropertyPageID() {
        return PROP_ID;
    }

    protected void importNuxeoTemplates() {
        TemplatePersistenceData[] datas;
        try {
            datas = readTemplatesFrom("nuxeo_codetemplates.xml");
        } catch (IOException e1) {
            UI.showError("An error occurred while importing nuxeo templates", e1);
            return;
        }

        for (int i = 0; i < datas.length; i++) {
            updateTemplate(datas[i]);
        }
    }

    protected void updateTemplate(TemplatePersistenceData data) {
        TemplatePersistenceData[] datas = templateStore.getTemplateData();
        for (int i = 0; i < datas.length; i++) {
            String id = datas[i].getId();
            if (id != null && id.equals(data.getId())) {
                datas[i].setTemplate(data.getTemplate());
                break;
            }
        }
    }

    protected void importNuxeoJavaEditorTemplates() {
        TemplatePersistenceData[] datas;
        try {
            datas = readTemplatesFrom("nuxeo_javaeditortemplates.xml");
        } catch (IOException e1) {
            UI.showError("An error occurred while importing nuxeo templates", e1);
            return;
        }
        for (int i = 0; i < datas.length; i++) {
            updateJavaEditorTemplate(datas[i]);
        }

    }

    protected void updateJavaEditorTemplate(TemplatePersistenceData data) {
        TemplateStore javaEditorTemplateStore = JavaPlugin.getDefault().getTemplateStore();
        // Commented, weird behavior, template data is null
        // Template oldTemplate =
        // javaEditorTemplateStore.findTemplate(data.getTemplate().getName());
        // if (oldTemplate != null) {
        // javaEditorTemplateStore.delete(javaEditorTemplateStore.getTemplateData(oldTemplate.getName()));
        // }
        javaEditorTemplateStore.add(data);
    }

    protected void setCleanupSaveParticipant() {
        IScopeContext context = fAccess.getInstanceScope();
        IEclipsePreferences node = context.getNode(JavaUI.ID_PLUGIN);

        Set<String> keys = JavaPlugin.getDefault().getCleanUpRegistry().getDefaultOptions(
                CleanUpConstants.DEFAULT_SAVE_ACTION_OPTIONS).getKeys();
        for (String key : keys) {
            node.put(CleanUpPreferenceUtil.SAVE_PARTICIPANT_KEY_PREFIX + key, node.get(key, CleanUpOptions.FALSE));
        }
        node.putBoolean(EDITOR_SAVE_PARTICIPANT_PREFIX + CleanUpPostSaveListener.POSTSAVELISTENER_ID, true);
        node.putBoolean(CleanUpPreferenceUtil.SAVE_PARTICIPANT_KEY_PREFIX
                + CleanUpConstants.CLEANUP_ON_SAVE_ADDITIONAL_OPTIONS, true);
        try {
            fAccess.applyChanges();
        } catch (BackingStoreException e) {
            UI.showError("An error occurred while applying Nuxeo post save cleanup changes", e);
        }

    }

    protected TemplatePersistenceData[] readTemplatesFrom(String fileName) throws IOException {
        TemplateReaderWriter reader = new TemplateReaderWriter();
        PreferenceFilesStreamProvider preferenceFilesStreamProvider = new PreferenceFilesStreamProvider(fileName);
        TemplatePersistenceData[] datas;
        try {
            datas = reader.read(preferenceFilesStreamProvider.getInputStream(), null);
        } catch (IOException e) {
            // fallback
            datas = reader.read(preferenceFilesStreamProvider.getFallbackStream(), null);
        }
        return datas;

    }
}
