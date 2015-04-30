/*
 * (C) Copyright 2006-2012 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
 *     bstefanescu
 */
package org.nuxeo.ide.sdk.java;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.nuxeo.ide.sdk.IConnectProvider;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.osgi.service.prefs.BackingStoreException;

/**
 *
 * Scan class-path for binaries
 */
public class ProjectDeployer {


    protected final IJavaProject project;

    protected final IFolder devFolder;

    protected final IFolder mainFolder;

    protected final IFolder seamFolder;


    protected ProjectDeployer(IJavaProject project) throws JavaModelException {
        this.project = project;
        devFolder = devbundleFolder(project);
        mainFolder = devFolder.getFolder("main");
        seamFolder = devFolder.getFolder("seam");
    }

    /**
     * For a given @param depName java unit import, retrieves all unit classes
     * having given dependencies Parse all resources from src/main folder (could
     * be src/main/java, src/main/seam...)
     */
    public static void copy(IJavaProject project, IProgressMonitor monitor, Appendable devbundle)
            throws Exception {
        new ProjectDeployer(project).copy(monitor, devbundle);
    }

    protected  IFolder devbundleFolder(IJavaProject project) throws JavaModelException {
        IFolder targetFolder = (IFolder) findMember(project.getOutputLocation().removeLastSegments(1));
        return targetFolder.getFolder("devbundle");
    }

    protected IResource findMember(IPath location) {
        return project.getProject().getWorkspace().getRoot().findMember(location);
    }

    protected Set<IPath> mergedOuputLocations = new HashSet<IPath>();

    protected void copy(IProgressMonitor monitor, Appendable devbundle)
            throws CoreException, IOException, StorageException,
            BackingStoreException {
        devFolder.delete(true, monitor);
        devFolder.create(IResource.FORCE|IResource.DERIVED, true, monitor);
        mainFolder.create(IResource.FORCE|IResource.DERIVED, true, monitor);
        seamFolder.create(IResource.FORCE|IResource.DERIVED, true, monitor);
        mergeOutputLocation(project.getOutputLocation(), monitor);
        for (IPackageFragmentRoot root : project.getPackageFragmentRoots()) {
            if (root.getKind() != IPackageFragmentRoot.K_SOURCE) {
                continue;
            }
            if (!"main".equals(root.getCorrespondingResource().getParent().getName())) {
                continue;
            }
            IPath outputLocation = root.getRawClasspathEntry().getOutputLocation();
            if (outputLocation != null) {
                if (outputLocation.lastSegment().equals("test-classes")) {
                    continue;
                }
                mergeOutputLocation(outputLocation, monitor);
            }
            copyRoot(root, monitor);
        }
        if (mainFolder.members().length != 0) {
            devbundle.append("bundle:" + mainFolder.getLocation().toOSString()).append(
                    "\n");
        }
        if (seamFolder.members().length != 0) {
            devbundle.append("seam:" + seamFolder.getLocation().toOSString()).append(
                    "\n");
        }
        IFolder l10n = project.getProject().getFolder(
                "src/main/resources/OSGI-INF/l10n");
        if (l10n.exists()) {
            for (IResource m : l10n.members()) {
                if (IResource.FILE == m.getType()) {
                    devbundle.append("resourceBundleFragment:").append(
                            m.getLocation().toOSString()).append("\n");
                }
            }
        }
        IConnectProvider connectProvider = SDKPlugin.getDefault().getConnectProvider();
        if (connectProvider != null) {
            for (IConnectProvider.Infos infos : SDKPlugin.getDefault().getConnectProvider().getLibrariesInfos(
                    project.getProject(), monitor)) {
                devbundle.append("bundle:").append(infos.file.getPath()).append(
                        "\n");
            }
        }
    }

    protected void mergeOutputLocation(IPath outputLocation, IProgressMonitor monitor) throws CoreException {
        if (outputLocation == null) {
            return;
        }
        if (mergedOuputLocations.contains(outputLocation)) {
            return;
        }
        mergeFolder(findMember(outputLocation), mainFolder, monitor);
        mergedOuputLocations.add(outputLocation);
    }

    protected void mergeFolder(IResource source, IResource target, IProgressMonitor monitor) throws CoreException {
        if (source == null) {
            return;
        }
        if (source.getType() == IResource.FOLDER) {
            IFolder sourceFolder = (IFolder)source;
            IFolder targetFolder = (IFolder)target;
            if (!target.exists()) {
                targetFolder.create(false, true, monitor);
            }
            for (IResource sourceMember : sourceFolder.members()) {
                IResource targetMember;
                if (sourceMember.getType() == IResource.FILE) {
                    targetMember = targetFolder.getFile(sourceMember.getName());
                } else {
                    targetMember = targetFolder.getFolder(sourceMember.getName());
                }
                mergeFolder(sourceMember, targetMember,
                        monitor);
            }
        } else {
            source.copy(target.getFullPath(), false, monitor);
        }
    }

    protected void copyRoot(IPackageFragmentRoot root, IProgressMonitor monitor) throws CoreException  {
        for (IJavaElement child : root.getChildren()) {
            switch (child.getElementType()) {
            case IJavaElement.PACKAGE_FRAGMENT:
                moveSeamPackageFragment(root, (IPackageFragment) child, monitor);
                break;
            case IJavaElement.COMPILATION_UNIT:
                moveSeamCompilationUnit(root, (ICompilationUnit) child, monitor);
                break;
            }
        }
//        int rootLength = root.getPath().segmentCount();
//        char[][] exclusionChars = ((ClasspathEntry) root.getRawClasspathEntry()).fullExclusionPatternChars();
//        char[][] inclusionChars = ((ClasspathEntry) root.getRawClasspathEntry()).fullInclusionPatternChars();
//        for (Object object : root.getNonJavaResources()) {
//            if (object instanceof IResource) {
//                IResource resource = (IResource)object;
//                if (org.eclipse.jdt.internal.core.util.Util.isExcluded(resource.getFullPath(), inclusionChars, exclusionChars, false)) {
//                    continue;
//                }
//                IPath path = resource.getFullPath().removeFirstSegments(rootLength);
//                copyResource(root, resource, mainFolder, path, monitor);
//            }
//        }
    }

    protected void moveSeamPackageFragment(IPackageFragmentRoot root,
            IPackageFragment fragment, IProgressMonitor monitor) throws CoreException {
        for (ICompilationUnit unit : fragment.getCompilationUnits()) {
            moveSeamCompilationUnit(root, unit, monitor);
        }
    }

    protected void moveSeamCompilationUnit(IPackageFragmentRoot root,
            ICompilationUnit unit, IProgressMonitor monitor) throws JavaModelException, CoreException {
        if (!isSeam(unit)) {
           return;
        }
        IPath outputLocation = root.getRawClasspathEntry().getOutputLocation();
        if (outputLocation == null) {
            outputLocation = project.getOutputLocation();
        }
        IPath path = unit.getPath().removeFirstSegments(
                root.getPath().segmentCount()).removeLastSegments(1);
        path = path.append(org.eclipse.jdt.internal.core.util.Util.getNameWithoutJavaLikeExtension(unit.getElementName())
                + ".class");
        IResource mainMember = mainFolder.findMember(path);
        if (mainMember == null) {
            throw new Error("Cannot find binary class " + path + " in project "
                    + project.getElementName());
        }
        IResource seamMember = seamFolder.findMember(path);
        if (seamMember != null) {
            seamMember.delete(true, monitor);
        }
        seamMember = seamFolder.getFile(path);
        mkdirs((IFolder) seamMember.getParent(), monitor);
        mainMember.move(seamMember.getFullPath(), IResource.FORCE|IResource.HIDDEN | IResource.DERIVED,
                monitor);
    }

    protected void mkdirs(IFolder folder, IProgressMonitor monitor)
            throws CoreException {
        if (folder.exists()) {
            return;
        }
        mkdirs((IFolder) folder.getParent(), monitor);
        folder.create(false, true, monitor);
        folder.setDerived(true, monitor);
    }

    protected boolean isSeam(ICompilationUnit unit) throws JavaModelException {
        for (IImportDeclaration imp : unit.getImports()) {
            if (imp.getElementName().equals("org.jboss.seam.annotations.Name")
                    && unit.getSource().contains("@Name")) {
                return true;
            }
        }
        return false;
    }

}