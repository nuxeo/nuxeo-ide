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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.IDocument;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class ExtensionPointNodeResolver {

    IType nodeDescriptorType = null;

    String currentNodePath = null;

    List<IType> descriptorCandidates = null;

    protected int lastBeginPosition = 0;

    /**
     * walk through the node path and resolve the current state of the current
     * node:
     * <ul>
     * <li>any Descriptor candidates ? usually needed at the extension tag node
     * to know which Descriptor can be used. Retrieved from the xml component
     * definition.</li>
     * <li>the current Descriptor type ? the current detected descriptor.</li>
     * <li>the current Node path ? the node xpath relative to the last node
     * descriptor.</li>
     * </ul>
     *
     * @param nodePath
     * @param context
     * @param contentAssistRequest
     */
    public void resolve(List<Node> nodePath,
            CompletionProposalInvocationContext context,
            ContentAssistRequest contentAssistRequest,
            ExtensionProposalProcessor extensionProposalProcessor) {
        // to avoid recomputing all each time
        int replacementBeginPosition = contentAssistRequest.getReplacementBeginPosition();
        if (lastBeginPosition == replacementBeginPosition) {
            return;
        }
        lastBeginPosition = replacementBeginPosition;

        descriptorCandidates = null;
        nodeDescriptorType = null;
        currentNodePath = null;

        loop: for (int i = 0; i < nodePath.size(); i++) {
            Node currentNode = nodePath.get(i);

            if ("extension".equals(currentNode.getNodeName()) && i == 2) {
                descriptorCandidates = extensionProposalProcessor.getDescriptorCandidates(currentNode);
                continue;
            }

            if (descriptorCandidates != null && !descriptorCandidates.isEmpty()) {
                DescriptorNodeProposalProcessor descriptorNodeProposalProcessor = new DescriptorNodeProposalProcessor();
                nodeDescriptorType = descriptorNodeProposalProcessor.findMatchingDescriptorType(
                        descriptorCandidates, currentNode);
                // so the next node, we don't get into this
                descriptorCandidates = null;
            }

            if (nodeDescriptorType != null) {
                if (currentNodePath == null) {
                    currentNodePath = "";
                } else if (currentNodePath.equals("")) {
                    currentNodePath = currentNode.getNodeName();
                } else {
                    currentNodePath = currentNodePath + "/"
                            + currentNode.getNodeName();
                }
                // for all fields and methods that has the XNode, XNodeList and
                // XNodeMap annotations
                try {

                    List<IAnnotatable> annotatableElements = new ArrayList<IAnnotatable>();

                    annotatableElements.addAll(Arrays.asList(nodeDescriptorType.getFields()));
                    annotatableElements.addAll(Arrays.asList(nodeDescriptorType.getMethods()));

                    for (IAnnotatable annotatableElement : annotatableElements) {
                        IAnnotation xnodeAnnotation = annotatableElement.getAnnotation(NuxeoXmlComponentProposalComputer.XNODE);
                        if (xnodeAnnotation == null
                                || !xnodeAnnotation.exists()) {
                            xnodeAnnotation = annotatableElement.getAnnotation(NuxeoXmlComponentProposalComputer.XNODEMAP);
                        }
                        if (xnodeAnnotation == null
                                || !xnodeAnnotation.exists()) {
                            xnodeAnnotation = annotatableElement.getAnnotation(NuxeoXmlComponentProposalComputer.XNODELIST);
                        }
                        if (xnodeAnnotation == null
                                || !xnodeAnnotation.exists()) {
                            continue;
                        }

                        // here we are trying to match the xpath extracted from
                        // the annotation value and the current node path.

                        // node1/node2 -> use the currentNodePath string to
                        // figure out the next current path node
                        // @ -> ignore
                        String xnodeAnnotationValue = getAnnotationValue(xnodeAnnotation);

                        if (xnodeAnnotationValue != null
                                && xnodeAnnotationValue.equals(currentNodePath)) {

                            // we have our new descriptor here or our element
                            // if descriptor,
                            // retrieve the type and set nodeDescriptorType,
                            // reset the other variable and ... go to the next
                            // node
                            String typeSignature = getTypeSignature(annotatableElement);
                            // a better way to do ?
                            String newDescriptorTypeStr = Signature.getSignatureQualifier(typeSignature)
                                    + "."
                                    + Signature.getSignatureSimpleName(typeSignature);
                            IType newDescriptorType = getJavaProjectForDocument(
                                    context.getDocument()).findType(
                                    newDescriptorTypeStr);
                            if (newDescriptorType != null
                                    && newDescriptorType.getAnnotation(NuxeoXmlComponentProposalComputer.XOBJECT) != null) {
                                nodeDescriptorType = newDescriptorType;
                                currentNodePath = "";
                                continue loop;
                            }
                            // For XNodeMap or XNodeList, take the description
                            // type from the annotation attribute
                            // "componentType"
                            String annotationValue = getAnnotationValue(
                                    xnodeAnnotation, "componentType");
                            if (annotationValue == null) {
                                continue loop;
                            }
                            newDescriptorType = getJavaProjectForDocument(
                                    context.getDocument()).findType(
                                    annotationValue);
                            if (newDescriptorType != null
                                    && newDescriptorType.getAnnotation(NuxeoXmlComponentProposalComputer.XOBJECT) != null) {
                                nodeDescriptorType = newDescriptorType;
                                currentNodePath = "";
                                continue loop;
                            }
                        }
                    }

                } catch (JavaModelException e) {
                    SDKPlugin.log(IStatus.ERROR,
                            "couldn't get fields for type "
                                    + nodeDescriptorType, e);
                }

            }

        }

    }

    protected String getTypeSignature(IAnnotatable annotatableElement)
            throws JavaModelException {
        if (annotatableElement instanceof IField) {
            return ((IField) annotatableElement).getTypeSignature();
        }
        if (annotatableElement instanceof IMethod) {
            return ((IMethod) annotatableElement).getReturnType();
        }
        return null;
    }

    public IType getNodeDescriptorType() {
        return nodeDescriptorType;
    }

    public String getCurrentNodePath() {
        return currentNodePath;
    }

    public List<IType> getDescriptorCandidates() {
        return descriptorCandidates;
    }

    protected String getAnnotationValue(IAnnotation xnodeAnnotation) {
        return getAnnotationValue(xnodeAnnotation, "value");
    }

    protected String getAnnotationValue(IAnnotation xnodeAnnotation,
            String memberName) {
        IMemberValuePair[] memberValuePairs;
        try {
            memberValuePairs = xnodeAnnotation.getMemberValuePairs();
        } catch (JavaModelException e) {
            SDKPlugin.log(IStatus.ERROR,
                    "Couldn't get member value pairs for annotation"
                            + xnodeAnnotation, e);
            return null;
        }
        for (IMemberValuePair member : memberValuePairs) {
            if (memberName.equals(member.getMemberName())) {
                return (String) member.getValue();
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
