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
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.nuxeo.ide.sdk.comp.contentassist.NodeContext;
import org.nuxeo.ide.sdk.comp.contentassist.NuxeoXmlComponentProposalComputer;
import org.w3c.dom.Node;

/**
 * Provides proposals for a specific node context. Each kind of node that will
 * add new proposals will have a specific proposalComputer implementation.
 *
 * @author Sun Seng David TAN (sunix@sunix.org)
 *
 */
@SuppressWarnings("restriction")
public abstract class AbstractNodeContextualProposalComputer {

    protected NodeContext nodeContext;

    public AbstractNodeContextualProposalComputer(NodeContext nodeContext) {
        this.nodeContext = nodeContext;
    }

    /**
     * Based on the current context, the new current node and the position,
     * choose the new nodeContextualProposalComputer implementation to use.
     *
     * @param currentNode
     * @param position
     * @return
     */
    public abstract AbstractNodeContextualProposalComputer getNextNodeContextualProposal(
            Node currentNode, int position);

    /**
     * is called when a new a new attribute name is being edited: override this
     * to add new attribute name proposals.
     *
     * @param contentAssistRequest is used to add new proposals.
     * @param offset
     * @param prefix
     */
    public void addAttributeNameProposals(
            ContentAssistRequest contentAssistRequest, int offset, String prefix) {
    }

    /**
     * is called when a new element is being edited: override this to add new
     * element proposals.
     *
     * @param contentAssistRequest
     * @param offset
     * @param prefix
     */
    public void addTagNameProposal(ContentAssistRequest contentAssistRequest,
            int offset, String prefix) {
    }

    /*
     * reusable methods, utils
     */

    protected IAnnotation extractXmapAnnotation(IAnnotatable annotatableElement) {
        IAnnotation xnodeAnnotation = annotatableElement.getAnnotation(NuxeoXmlComponentProposalComputer.XNODE);
        if (xnodeAnnotation == null || !xnodeAnnotation.exists()) {
            xnodeAnnotation = annotatableElement.getAnnotation(NuxeoXmlComponentProposalComputer.XNODEMAP);
        }
        if (xnodeAnnotation == null || !xnodeAnnotation.exists()) {
            xnodeAnnotation = annotatableElement.getAnnotation(NuxeoXmlComponentProposalComputer.XNODELIST);
        }
        if (xnodeAnnotation == null || !xnodeAnnotation.exists()) {
            return null;
        }
        return xnodeAnnotation;
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
