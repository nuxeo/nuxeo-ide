/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the GNU Lesser General Public License (LGPL)
 * version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Contributors: Sun Seng David TAN (sunix@sunix.org)
 */
package org.nuxeo.ide.sdk.comp.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.nuxeo.ide.sdk.comp.ComponentIndex;
import org.nuxeo.ide.sdk.comp.ComponentIndexManager;
import org.nuxeo.ide.sdk.comp.ComponentModel;
import org.nuxeo.ide.sdk.comp.ComponentRef;
import org.nuxeo.ide.sdk.comp.ExtensionPointModel;
import org.nuxeo.ide.sdk.server.ui.DocumentationFormat;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Helper to propose available extension that can be used.
 *
 * @author Sun Seng David TAN (sunix@sunix.org)
 *
 */
@SuppressWarnings("restriction")
public class ExtensionProposalProcessor {

    protected ComponentIndex componentIndex = NuxeoSDK.getDefault().getComponentIndex();

    protected ComponentIndexManager componentManager = NuxeoSDK.getDefault().getComponentIndexManager();

    IJavaProject javaproject;

    /**
     * Cache extension point models in the proposal computer (bind to the
     * current editor) to improve performance.
     */
    protected List<ExtensionPointModel> cachedExtensionPointModel = new ArrayList<ExtensionPointModel>();

    public ExtensionProposalProcessor(
            CompletionProposalInvocationContext context) {
        javaproject = getJavaProjectForDocument(context.getDocument());

        // Caching extension points model to improve performance.
        ComponentRef[] components = componentIndex.getComponents();
        for (ComponentRef componentRef : components) {
            ComponentModel componentModel;
            try {
                componentModel = componentManager.getComponent(componentRef);
            } catch (Exception e) {
                throw new Error(
                        "An error occured while getting the component model from the component ref",
                        e);
            }
            if (componentModel == null) {
                continue;
            }
            ExtensionPointModel[] xps = componentModel.getExtensionPoints();
            for (ExtensionPointModel extensionPointModel : xps) {
                cachedExtensionPointModel.add(extensionPointModel);
            }
        }
    }

    /**
     * Get all the existing extension points and get only the ones that contains
     * the prefix, add the proposal to the content Assist Request
     *
     * @param prefix
     * @param contentAssistRequest
     * @param offset
     */
    public void findAndAddExtensionProposal(String prefix,
            ContentAssistRequest contentAssistRequest, int offset) {
        // directly propose the extension point to select
        for (ExtensionPointModel extensionPointModel : cachedExtensionPointModel) {
            if (extensionPointModel.getLabel().toLowerCase().contains(
                    prefix.toLowerCase())
                    || prefix.isEmpty()) {
                String replacementStr = "target=\""
                        + extensionPointModel.getComponent() + "\" point=\""
                        + extensionPointModel.getName() + "\" ";

                contentAssistRequest.addProposal(new CompletionProposal(
                        replacementStr,
                        offset - prefix.length(),
                        prefix.length(),
                        replacementStr.length(),
                        extensionPointModel.getImage(),
                        extensionPointModel.getLabel(),
                        null,
                        DocumentationFormat.format(
                                extensionPointModel.getDocumentation()).replaceAll(
                                "\n", "<br/>\n")));
            }
        }
    }

    /**
     * For a extension node, get the possible java descriptors
     *
     * @param extensionNode
     * @return
     */
    public List<IType> getDescriptorCandidates(Node extensionNode) {
        ExtensionPointModel currentExtensionPointModel = getExtensionPointModel(extensionNode);
        if(currentExtensionPointModel == null){
            return null;
        }
        // get the possible solutions from descriptors:
        String[] descriptors = currentExtensionPointModel.getContributionTypes();

        List<IType> descriptorCandidates = new ArrayList<IType>();
        for (String descriptor : descriptors) {
            try {
                descriptorCandidates.add(javaproject.findType(descriptor));
            } catch (JavaModelException e) {
                SDKPlugin.log(IStatus.ERROR,
                        "Couldn't find type " + descriptor, e);
            }
        }
        return descriptorCandidates;
    }

    protected ExtensionPointModel getExtensionPointModel(Node extensionNode) {
        NamedNodeMap attrs = extensionNode.getAttributes();

        Node targetNamedItem = attrs.getNamedItem("target");
        if(targetNamedItem == null){
            return null;
        }
        String targetComp = targetNamedItem.getNodeValue();
        Node pointNamedItem = attrs.getNamedItem("point");
        if(pointNamedItem == null){
            return null;
        }
        String point = pointNamedItem.getNodeValue();

        ComponentRef componentRef = componentIndex.getComponent(targetComp);
        ComponentModel componentModel;
        try {
            componentModel = componentManager.getComponent(componentRef);
        } catch (Exception e) {
            SDKPlugin.getDefault();
            SDKPlugin.log(
                    IStatus.ERROR,
                    "Couldn't get the component model from "
                            + componentRef.getLabel(), e);
            return null;
        }
        if (componentModel == null) {
            return null;
        }

        ExtensionPointModel[] xpoints = componentModel.getExtensionPoints();
        for (ExtensionPointModel extensionPointModel : xpoints) {
            if (extensionPointModel.getName().equals(point)) {
                return extensionPointModel;
            }
        }
        return null;
    }

    protected IJavaProject getJavaProjectForDocument(IDocument document) {
        IStructuredModel model = StructuredModelManager.getModelManager().getExistingModelForRead(
                document);
        if (model == null) {
            return null;
        }
        Path path = new Path(model.getBaseLocation());
        IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        if (!file.exists()) {
            return null;
        }

        IProject project = file.getProject();
        if (project != null) {
            IJavaProject javaProject = JavaCore.create(project);
            if (javaProject != null && !javaProject.exists()) {
                return null;
            }
            return javaProject;
        }
        return null;

    }

}
