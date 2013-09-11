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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAnnotatable;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegionList;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.w3c.dom.Node;

/**
 * Main class for Nuxeo components and extension point completion proposal in
 * Eclipse WST XML Editor.
 *
 * @author Sun Seng David TAN (sunix@sunix.org)
 *
 */
@SuppressWarnings("restriction")
public class NuxeoXmlComponentProposalComputer extends
        DefaultXMLCompletionProposalComputer {

    public static final String XOBJECT = "org.nuxeo.common.xmap.annotation.XObject";

    public static final String XNODEMAP = "org.nuxeo.common.xmap.annotation.XNodeMap";

    public static final String XNODELIST = "org.nuxeo.common.xmap.annotation.XNodeList";

    public static final String XNODE = "org.nuxeo.common.xmap.annotation.XNode";

    protected ExtensionProposalProcessor extensionProposalProcessor = null;

    protected ExtensionProposalProcessor getExtensionProposalProcessor(
            CompletionProposalInvocationContext context) {
        if (extensionProposalProcessor == null) {
            extensionProposalProcessor = new ExtensionProposalProcessor(context);
        }
        return extensionProposalProcessor;
    }

    protected ExtensionPointNodeResolver xpresolver = null;

    protected ExtensionPointNodeResolver getExtensionPointNodeResolver() {
        if (xpresolver == null) {
            xpresolver = new ExtensionPointNodeResolver();
        }
        return xpresolver;
    }

    @Override
    protected void addAttributeNameProposals(
            ContentAssistRequest contentAssistRequest,
            CompletionProposalInvocationContext context) {

        IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
        int offset = getOffset(context);
        List<Node> nodePath = getNodePath(contentAssistRequest, true);
        String prefix = extractPrefix(context.getViewer(), offset);

        // detect current element
        if ("component".equals(node.getNodeName()) && nodePath.size() == 2) {
            // find open region
            IStructuredDocumentRegion open = node.getFirstStructuredDocumentRegion();
            ITextRegionList openRegions = open.getRegions();
            int i = openRegions.indexOf(contentAssistRequest.getRegion());
            if (i < 0) {
                return;
            }

            contentAssistRequest.addProposal(new CompletionProposal(
                    "name=\"\" ", offset - prefix.length(), prefix.length(), 6,
                    SDKPlugin.getDefault().getImageRegistry().get(
                            "icons/comp/component.gif"),
                    "name - Component name ", null, "The name of the component"));
            return;
        }

        if ("extension".equals(node.getNodeName()) && nodePath.size() == 3) {

            getExtensionProposalProcessor(context).findAndAddExtensionProposal(
                    prefix, contentAssistRequest, offset);
            return;
        }

        ExtensionPointNodeResolver extensionPointNodeResolver = getExtensionPointNodeResolver();
        extensionPointNodeResolver.resolve(nodePath, context,
                contentAssistRequest, getExtensionProposalProcessor(context));

        IType nodeDescriptorType = extensionPointNodeResolver.getNodeDescriptorType();
        String currentNodePath = extensionPointNodeResolver.getCurrentNodePath();

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
                IAnnotation xnodeAnnotation = annotatableElement.getAnnotation(XNODE);
                if (xnodeAnnotation != null) {
                    proposeAttributeName(contentAssistRequest, offset, prefix,
                            currentNodePath, xnodeAnnotation);
                }
            }

        }

    }

    protected void proposeAttributeName(
            ContentAssistRequest contentAssistRequest, int offset,
            String prefix, String currentNodePath, IAnnotation xnodeAnnotation) {
        String elementName = getAnnotationValue(xnodeAnnotation);

        // specific completion proposal for attribute name
        if (elementName == null) {
            return;
        }
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
                    SDKPlugin.getDefault().getImageRegistry().get(
                            "icons/comp/xpoint.gif"), elementName.substring(1)
                            + " - attribute", null, null);
            contentAssistRequest.addProposal(newProposal);

        }
    }

    @Override
    protected void addEntityProposals(
            ContentAssistRequest contentAssistRequest,
            ITextRegion completionRegion, IDOMNode treeNode,
            CompletionProposalInvocationContext context) {
        addTagNameProposals(contentAssistRequest, 0, context);
    }

    @Override
    protected void addTagNameProposals(
            ContentAssistRequest contentAssistRequest, int childPosition,
            CompletionProposalInvocationContext context) {
        int offset = getOffset(context);
        List<Node> nodePath = getNodePath(contentAssistRequest, false);
        String prefix = extractPrefix(context.getViewer(), offset);

        Node node = nodePath.get(nodePath.size() - 1);
        // maybe this should be detected in the nodeResolver
        if ("component".equals(node.getNodeName()) && nodePath.size() == 2) {
            // can propose extension
            proposeTag(contentAssistRequest, offset, "extension".length(),
                    prefix, "extension", "extension", "icons/comp/xpoint.gif",
                    "Extends Nuxeo by using an extension point provided by the platform.");
            proposeTag(
                    contentAssistRequest,
                    offset,
                    22,
                    prefix,
                    "extension-point",
                    "extension-point name=\"\"><object class=\"\"/></extension-point>",
                    "icons/comp/xpoint.gif",
                    "Create your own extension point that can be extended by other components and plugin. The object class is the extension descriptor that will be mapped to contributions using Nuxeo Xmap");

            proposeTag(contentAssistRequest, offset, 14, prefix,
                    "documentation", "documentation></documentation>",
                    "icons/comp/component.gif",
                    "Defines the current component documentation");
            proposeTag(
                    contentAssistRequest,
                    offset,
                    22,
                    prefix,
                    "implementation",
                    "implementation class=\"\"/>",
                    "icons/comp/component.gif",
                    "Defines an implementation of the current component. Should extends DefaultComponent.");
            proposeTag(
                    contentAssistRequest,
                    offset,
                    8,
                    prefix,
                    "require",
                    "require></require>",
                    "icons/comp/component.gif",
                    "Declares requirement on other components. Declares another existing component (given its unique name) that this component will require to be loaded. Useful when overriding or merging contributions to be sure that of the contributions registrations order");
            proposeTag(contentAssistRequest, offset, 28, prefix, "service",
                    "service><provide interface=\"\" /></service>",
                    "icons/comp/component.gif",
                    "Declares Services provided by this component");

        }

        ExtensionPointNodeResolver extensionPointNodeResolver = getExtensionPointNodeResolver();
        extensionPointNodeResolver.resolve(nodePath, context,
                contentAssistRequest, getExtensionProposalProcessor(context));
        IType nodeDescriptorType = extensionPointNodeResolver.getNodeDescriptorType();
        String currentNodePath = extensionPointNodeResolver.getCurrentNodePath();
        List<IType> descriptorCandidate = extensionPointNodeResolver.getDescriptorCandidates();

        if (descriptorCandidate != null) {
            for (IType iType : descriptorCandidate) {
                // get the xobject field of each type
                IAnnotation xobjectAnnotation = iType.getAnnotation(XOBJECT);

                if (xobjectAnnotation == null || !xobjectAnnotation.exists()) {
                    continue;
                }
                String xobjectName = getAnnotationValue(xobjectAnnotation);

                proposeTag(contentAssistRequest, offset, prefix, xobjectName,
                        "icons/comp/xpoint.gif");
            }
            return;
        }
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

                IAnnotation xnodeAnnotation = annotatableElement.getAnnotation(XNODE);
                if (xnodeAnnotation == null || !xnodeAnnotation.exists()) {
                    xnodeAnnotation = annotatableElement.getAnnotation(XNODEMAP);
                }
                if (xnodeAnnotation == null || !xnodeAnnotation.exists()) {
                    xnodeAnnotation = annotatableElement.getAnnotation(XNODELIST);
                }
                if (xnodeAnnotation == null || !xnodeAnnotation.exists()) {
                    continue;
                }

                if (xnodeAnnotation != null) {
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
                    if (elementName.toLowerCase().startsWith(
                            prefix.toLowerCase())
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
                                elementName.length(), prefix, elementName
                                        + " - (" + originalElement + ")",
                                elementName, "icons/comp/xpoint.gif",
                                elementName + " - (" + originalElement + ")");

                    }
                }

            }

        }

    }

    protected void proposeTag(ContentAssistRequest contentAssistRequest,
            int offset, int cursorPosition, String prefix,
            String displayString, String replacementString, String icon,
            String description) {
        if (displayString.startsWith(prefix)) {
            Node currentNode = contentAssistRequest.getNode();
            if (currentNode.getNodeType() == Node.TEXT_NODE
                    && !"<".equals(currentNode.getTextContent().trim())) {
                replacementString = "<" + replacementString;
                cursorPosition++;
            }
            contentAssistRequest.addProposal(new CompletionProposal(
                    replacementString, offset - prefix.length(),
                    prefix.length(), cursorPosition,
                    SDKPlugin.getDefault().getImageRegistry().get(icon),
                    displayString, null, description));
        }
    }

    protected void proposeTag(ContentAssistRequest contentAssistRequest,
            int offset, String prefix, String elementName, String icon) {
        proposeTag(contentAssistRequest, offset, elementName.length(), prefix,
                elementName, elementName, icon, null);
    }

    protected int getOffset(CompletionProposalInvocationContext context) {
        int offset = context.getInvocationOffset();
        ITextViewer viewer = context.getViewer();
        ITextSelection selection = (ITextSelection) viewer.getSelectionProvider().getSelection();

        // adjust offset to end of normalized selection
        if (selection.getOffset() == offset) {
            offset = selection.getOffset() + selection.getLength();
        }
        return offset;
    }

    protected ITextSelection getSelection(
            CompletionProposalInvocationContext context) {
        ITextViewer viewer = context.getViewer();
        return (ITextSelection) viewer.getSelectionProvider().getSelection();
    }

    protected List<Node> getNodePath(ContentAssistRequest contentAssistRequest,
            boolean addFirst) {
        Node tmpNode = contentAssistRequest.getNode();
        List<Node> nodePath = new ArrayList<Node>();
        if (addFirst) {
            nodePath.add(tmpNode);
        }
        while (tmpNode.getParentNode() != null) {
            tmpNode = tmpNode.getParentNode();
            nodePath.add(tmpNode);
        }
        Collections.reverse(nodePath);
        return nodePath;
    }

    // copied from
    // org.eclipse.jface.text.templates.TemplateCompletionProcessor.extractPrefix(ITextViewer,
    // int)
    protected String extractPrefix(ITextViewer viewer, int offset) {
        int i = offset;
        IDocument document = viewer.getDocument();
        if (i > document.getLength()) {
            return ""; //$NON-NLS-1$
        }

        try {
            while (i > 0) {
                char ch = document.getChar(i - 1);
                if (!Character.isJavaIdentifierPart(ch)) {
                    break;
                }
                i--;
            }

            return document.get(i, offset - i);
        } catch (BadLocationException e) {
            return ""; //$NON-NLS-1$
        }
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
