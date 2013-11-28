/*
 * (C) Copyright 2006-2008 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     bstefanescu
 */
package org.nuxeo.ide.sdk;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.nuxeo.ide.sdk.java.ClasspathEditor;
import org.nuxeo.ide.sdk.java.SDKClasspathContainer;
import org.osgi.service.prefs.BackingStoreException;

/**
 * A meta Project Nature marking projects as being handled by Nuxeo IDE.
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class NuxeoNature implements IProjectNature {

    public static boolean isNuxeoProject(IProject project) throws CoreException {
        return project.isOpen() && project.isNatureEnabled(ID);
    }

    public static NuxeoNature get(IProject project) throws CoreException {
        return (NuxeoNature)project.getNature(ID);
    }
    
    public final static String ID = "org.nuxeo.ide.NuxeoNature";

    protected IProject project;

    /**
     * 
     */
    public NuxeoNature() {
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public void configure() throws CoreException {
        // IFolder folder = project.getFolder("studio.project");
        // if (!folder.exists()) {
        // folder.create(IResource.VIRTUAL, true, new NullProgressMonitor());
        // }

        // IProjectDescription desc = project.getDescription();
        // ICommand[] commands = desc.getBuildSpec();
        // boolean found = false;
        //
        // for (int i = 0; i < commands.length; ++i) {
        // if (commands[i].getBuilderName().equals(ExtensionsBuilder.ID)) {
        // found = true;
        // break;
        // }
        // }
        // if (!found) {
        // // add builder to project
        // ICommand command = desc.newCommand();
        // command.setBuilderName(ExtensionsBuilder.ID);
        // ICommand[] newCommands = new ICommand[commands.length + 1];
        //
        // // Add it after other builders.
        // System.arraycopy(commands, 0, newCommands, 0, commands.length);
        // newCommands[commands.length] = command;
        // desc.setBuildSpec(newCommands);
        // project.setDescription(desc, null);
        // }
    }

    public void deconfigure() throws CoreException {
        // IFolder folder = project.getFolder("studio.project");
        // if (folder.exists()) {
        // folder.delete(true, new NullProgressMonitor());
        // }

        // IProjectDescription desc = project.getDescription();
        // ICommand[] commands = desc.getBuildSpec();
        // ArrayList<ICommand> newCommands = new ArrayList<ICommand>();
        // for (ICommand cmd : commands) {
        // if (!ExtensionsBuilder.ID.equals(cmd.getBuilderName())) {
        // newCommands.add(cmd);
        // }
        // }
        // if (newCommands.size() != commands.length) {
        // desc.setBuildSpec(newCommands.toArray(new
        // ICommand[newCommands.size()]));
        // project.setDescription(desc, null);
        // }
    }
    
    public void addClasspath() throws CoreException {
        ClasspathEditor editor = new ClasspathEditor(project);
        editor.addContainers(SDKClasspathContainer.IDS);
        editor.flush();
        try {
            editor.setLibsAsSdkUserLibs();
        } catch (BackingStoreException cause) {
            throw SDKPlugin.coreException("Cannot load user libraries preferences", cause);
        }
        editor.flush();
    }

    public void reloadClasspath() throws JavaModelException {
        ClasspathEditor editor = new ClasspathEditor(project);
        editor.removeContainers(SDKClasspathContainer.IDS);
        editor.flush();
        if (NuxeoSDK.getDefault() != null) {
            editor.addContainers(SDKClasspathContainer.IDS);
            editor.flush();
        }        
    }
    
    public void removeClasspath() throws JavaModelException {
        ClasspathEditor editor = new ClasspathEditor(project);
        editor.removeContainers(SDKClasspathContainer.IDS);
        editor.flush();
    }
    
}
