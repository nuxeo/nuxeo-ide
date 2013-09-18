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
package org.nuxeo.ide.sdk.comp.contentassist.contextualproposal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.nuxeo.ide.sdk.comp.contentassist.NodeContext;
import org.nuxeo.ide.sdk.comp.contentassist.NuxeoXmlComponentProposalComputer;
import org.w3c.dom.Node;

/**
 * Propose tag names and attributes for elements inside the &lt;extension&gt;
 * tag. Introspecting xmap annotations from Nuxeo extension point descriptors.
 *
 * @author Sun Seng David TAN (sunix@sunix.org)
 *
 */
@SuppressWarnings("restriction")
public class ExtensionDescriptorNodeContextualProposalComputer extends
        AbstractNodeContextualProposalComputer {

    public ExtensionDescriptorNodeContextualProposalComputer(
            NodeContext nodeContext) {
        super(nodeContext);
    }

    @Override
    public AbstractNodeContextualProposalComputer getNextNodeContextualProposal(
            Node currentNode, int position) {

        if (nodeContext.getNodeDescriptorType() != null) {
            if (nodeContext.getCurrentNodePath() == null) {
                nodeContext.setCurrentNodePath("");
            } else if (nodeContext.getCurrentNodePath().equals("")) {
                nodeContext.setCurrentNodePath(currentNode.getNodeName());
            } else {
                nodeContext.setCurrentNodePath(nodeContext.getCurrentNodePath()
                        + "/" + currentNode.getNodeName());
            }
            // for all fields and methods that has the XNode, XNodeList and
            // XNodeMap annotations
            try {

                List<IAnnotatable> annotatableElements = new ArrayList<IAnnotatable>();

                annotatableElements.addAll(Arrays.asList(nodeContext.getNodeDescriptorType().getFields()));
                annotatableElements.addAll(Arrays.asList(nodeContext.getNodeDescriptorType().getMethods()));

                for (IAnnotatable annotatableElement : annotatableElements) {
                    IAnnotation xnodeAnnotation = extractXmapAnnotation(annotatableElement);
                    if (xnodeAnnotation == null) {
                        continue;
                    }

                    // here we are trying to match the xpath extracted from
                    // the annotation value and the current node path.

                    // node1/node2 -> use the currentNodePath string to
                    // figure out the next current path node
                    // @ -> ignore
                    String xnodeAnnotationValue = getAnnotationValue(xnodeAnnotation);

                    if (xnodeAnnotationValue != null
                            && xnodeAnnotationValue.equals(nodeContext.getCurrentNodePath())) {

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
                                nodeContext.getCompletionProposalInvocationContext().getDocument()).findType(
                                newDescriptorTypeStr);
                        if (newDescriptorType != null
                                && newDescriptorType.getAnnotation(NuxeoXmlComponentProposalComputer.XOBJECT) != null) {
                            nodeContext.setNodeDescriptorType(newDescriptorType);
                            nodeContext.setCurrentNodePath("");
                            return new ExtensionDescriptorNodeContextualProposalComputer(
                                    nodeContext);
                        }
                        // For XNodeMap or XNodeList, take the description
                        // type from the annotation attribute
                        // "componentType"
                        String annotationValue = getAnnotationValue(
                                xnodeAnnotation, "componentType");
                        if (annotationValue == null) {
                            return null;
                        }
                        newDescriptorType = getJavaProjectForDocument(
                                nodeContext.getCompletionProposalInvocationContext().getDocument()).findType(
                                annotationValue);
                        if (newDescriptorType != null
                                && newDescriptorType.getAnnotation(NuxeoXmlComponentProposalComputer.XOBJECT) != null) {
                            nodeContext.setNodeDescriptorType(newDescriptorType);
                            nodeContext.setCurrentNodePath("");
                            return new ExtensionDescriptorNodeContextualProposalComputer(
                                    nodeContext);
                        }
                    }
                }
                return this;
            } catch (JavaModelException e) {
                SDKPlugin.log(IStatus.ERROR, "couldn't get fields for type "
                        + nodeContext.getNodeDescriptorType(), e);
            }

        }
        return null;
    }

    @Override
    public void addAttributeNameProposals(
            ContentAssistRequest contentAssistRequest, int offset, String prefix) {

        IType nodeDescriptorType = nodeContext.getNodeDescriptorType();
        String currentNodePath = nodeContext.getCurrentNodePath();

        if (nodeDescriptorType != null) {
            // propose the current object attribute

            List<IAnnotatable> annotatableElements = new ArrayList<IAnnotatable>();
            try {
                annotatableElements.addAll(Arrays.asList(nodeDescriptorType.getFields()));
                annotatableElements.addAll(Arrays.asList(nodeDescriptorType.getMethods()));
            } catch (JavaModelException e) {
                SDKPlugin.log(
                        IStatus.WARNING,
                        "exception while trying to get fields and methods from the node type descriptor",
                        e);
                return;
            }

            for (IAnnotatable annotatableElement : annotatableElements) {
                IAnnotation xnodeAnnotation = extractXmapAnnotation(annotatableElement);
                if (xnodeAnnotation == null) {
                    continue;
                }
                String elementName = getAnnotationValue(xnodeAnnotation);
                if (elementName == null) {
                    continue;
                }
                proposeAttributeName(contentAssistRequest, offset, prefix,
                        currentNodePath, elementName, "icons/comp/xpoint.gif");
            }
        }
    }

    protected void proposeAttributeName(
            ContentAssistRequest contentAssistRequest, int offset,
            String prefix, String currentNodePath, String elementName,
            String icon) {
        if (currentNodePath != null && !elementName.startsWith(currentNodePath)) {
            return;
        }
        elementName = currentNodePath == null ? elementName
                : elementName.substring(currentNodePath.length());
        if (elementName.toLowerCase().contains(prefix.toLowerCase())
                && elementName.startsWith("@")) {
            String replacementString = elementName.substring(1) + "=\"\"";
            CompletionProposal newProposal;
            newProposal = new CompletionProposal(replacementString, offset
                    - prefix.length(), prefix.length(),
                    replacementString.length() - 1,
                    SDKPlugin.getDefault().getImageRegistry().get(icon),
                    elementName.substring(1) + " - attribute", null, null);
            contentAssistRequest.addProposal(newProposal);

        }
    }

    @Override
    public void addTagNameProposal(ContentAssistRequest contentAssistRequest,
            int offset, String prefix) {
        IType nodeDescriptorType = nodeContext.getNodeDescriptorType();
        String currentNodePath = nodeContext.getCurrentNodePath();
        if (nodeDescriptorType != null) {
            // propose the current object attribute
            List<IAnnotatable> annotatableElements = new ArrayList<IAnnotatable>();
            try {
                annotatableElements.addAll(Arrays.asList(nodeDescriptorType.getFields()));
                annotatableElements.addAll(Arrays.asList(nodeDescriptorType.getMethods()));
            } catch (JavaModelException e) {
                SDKPlugin.log(
                        IStatus.WARNING,
                        "exception while trying to get fields and methods from the node type descriptor",
                        e);
                return;
            }

            for (IAnnotatable annotatableElement : annotatableElements) {

                IAnnotation xnodeAnnotation = extractXmapAnnotation(annotatableElement);
                if (xnodeAnnotation == null) {
                    continue;
                }

                String elementName = getAnnotationValue(xnodeAnnotation);
                // specific completion proposal for attribute name
                if (elementName == null) {
                    continue;
                }
                if (currentNodePath != null
                        && !elementName.startsWith(currentNodePath)) {
                    continue;
                }
                elementName = currentNodePath == null ? elementName
                        : elementName.substring(currentNodePath.length());
                if (elementName.toLowerCase().startsWith(prefix.toLowerCase())
                        && !elementName.startsWith("@")) {
                    // escaping trailing path, maybe there is somehow a
                    // better/simpler way to do:
                    if (elementName.startsWith("/")) {
                        elementName = elementName.substring(1);
                    }
                    String originalElement = elementName;
                    int indexOf = elementName.indexOf("/");
                    if (indexOf > 0) {
                        elementName = elementName.substring(0, indexOf);
                    }
                    indexOf = elementName.indexOf("@");
                    if (indexOf > 0) {
                        elementName = elementName.substring(0, indexOf);
                    }

                    proposeTag(contentAssistRequest, offset,
                            elementName.length(), prefix, elementName + " - ("
                                    + originalElement + ")", elementName,
                            "icons/comp/xpoint.gif", elementName + " - ("
                                    + originalElement + ")");

                }
            }

        }
    }

}
