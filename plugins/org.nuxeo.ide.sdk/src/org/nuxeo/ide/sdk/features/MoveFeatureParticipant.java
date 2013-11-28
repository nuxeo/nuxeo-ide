/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
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
package org.nuxeo.ide.sdk.features;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.ltk.core.refactoring.resource.RenameResourceChange;
import org.nuxeo.ide.common.IOUtils;
import org.nuxeo.ide.sdk.NuxeoNature;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.nuxeo.ide.sdk.model.ExtensionModel;
import org.nuxeo.ide.sdk.model.ManifestWriter;

;

/**
 * 
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class MoveFeatureParticipant extends MoveParticipant {

    protected FeatureType type;

    protected IPackageFragment pkg;

    protected String id;

    protected String newId;

    public MoveFeatureParticipant() {
    }

    @Override
    protected boolean initialize(Object element) {
        Object obj = getArguments().getDestination();
        if (obj instanceof IPackageFragment) {
            pkg = (IPackageFragment) obj;
            try {
                if (!pkg.getJavaProject().getProject().isNatureEnabled(
                        NuxeoNature.ID)) {
                    return false;
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        type = FeatureType.fromElement(element);
        if (type == null || !type.file.exists()) {
            return false;
        }
        id = type.type.getFullyQualifiedName();
        newId = pkg.getElementName() + "." + type.type.getElementName();
        return true;
    }

    @Override
    public String getName() {
        return "Move Feature Synchronizer";
    }

    protected IFile getExtensionSrc() {
        if (type == null) {
            return null;
        }
        return type.getProject().getFile(
                ExtensionModel.getPath(type.type.getFullyQualifiedName()));
    }

    protected IFile getExtensionDst() {
        if (pkg == null && type == null) {
            return null;
        }
        String dstId = pkg.getElementName() + "." + type.type.getElementName();
        return pkg.getJavaProject().getProject().getFile(
                ExtensionModel.getPath(dstId));
    }

    protected IFile getExtensionRuntimeDst() {
        if (pkg == null && type == null) {
            return null;
        }
        String dstId = pkg.getElementName() + "." + type.type.getElementName();
        return pkg.getJavaProject().getProject().getFile(
                ExtensionModel.getRuntimePath(dstId));
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm,
            CheckConditionsContext context) throws OperationCanceledException {
        if (type == null || pkg == null) {
            return new RefactoringStatus();
        }
        if (pkg.isDefaultPackage()) {
            throw new OperationCanceledException("cannot locate feature in default package not supported");
        }
        IFile mf = type.getProject().getFile(ManifestWriter.PATH);
        IFile mvExt = getExtensionDst();
        ResourceChangeChecker checker = (ResourceChangeChecker) context.getChecker(ResourceChangeChecker.class);
        IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();
        RefactoringStatus status = new RefactoringStatus();
        checkResourceContentConditions(deltaFactory, status);
        deltaFactory.change(mvExt);
        deltaFactory.change(mf);
        return status;
    }

    protected void checkResourceContentConditions(
            final IResourceChangeDescriptionFactory deltaFactory,
            final RefactoringStatus status) {
        final String fqn = type.type.getFullyQualifiedName();
        final IFolder resources = type.getProject().getFolder(
                "src/main/resources");
        try {
            resources.accept(new ResourceVisitor(fqn) {

                @Override
                public void visitResource(IFile file, String suffix,
                        @SuppressWarnings("hiding") ContentType type) {
                    String newName = newId + suffix;
                    deltaFactory.move(file,
                            file.getParent().getLocation().append(newName));
                }

            });
        } catch (CoreException e) {
            status.addError("Cannot visit binary resources");
        }
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException,
            OperationCanceledException {
        String content = getContent(getExtensionSrc());
        if (content == null) {
            return new NullChange("No extension found for renamed class");
        }
        content = content.replace(id, newId);
        CompositeChange result = new CompositeChange("Moving feature " + id
                + " to " + pkg);
        IFile mf = type.getProject().getFile(ManifestWriter.PATH);
        if (mf.exists()) {
            ManifestChange change = new ManifestChange(mf);
            change.remove("Nuxeo-Component", ExtensionModel.getRuntimePath(id));
            result.add(change);
        }
        createResourcesChange(result);
        result.add(new ExtensionChange(getExtensionDst(), content, false));
        mf = pkg.getJavaProject().getProject().getFile(ManifestWriter.PATH);
        ManifestChange change = new ManifestChange(mf);
        change.append("Nuxeo-Component",
                getExtensionRuntimeDst().getProjectRelativePath().toString());
        result.add(change);
        return result;
    }

    protected void createResourcesChange(final CompositeChange result)
            throws CoreException {
        final String fqn = type.type.getFullyQualifiedName();
        final IFolder resources = type.getProject().getFolder(
                "src/main/resources");

        resources.accept(new ResourceVisitor(fqn) {

            @Override
            public void visitResource(IFile file, String suffix,
                    @SuppressWarnings("hiding") ContentType type) {
                if (ContentType.BinaryContent.equals(type)) {
                    result.add(new RenameResourceChange(file.getFullPath(), newId
                            + suffix));
                } else {
                    result.add(new RenameResourceAndReplaceContentChange(file, id, newId, suffix));             
                }
            }

        });

    }

    public static String getContent(IFile file) throws CoreException {
        if (file.exists()) {
            InputStream in = file.getContents(true);
            try {
                return IOUtils.read(in);
            } catch (IOException e) {
                throw new CoreException(new Status(IStatus.ERROR,
                        SDKPlugin.PLUGIN_ID, "Failed to read extension file: "
                                + file, e));
            }
        }
        return null;
    }
}
