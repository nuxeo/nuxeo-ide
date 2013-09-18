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
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;
import org.nuxeo.ide.sdk.comp.contentassist.contextualproposal.AbstractNodeContextualProposalComputer;
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
        int offset = getOffset(context);
        String prefix = extractPrefix(context.getViewer(), offset);

        List<Node> nodePath = getNodePath(contentAssistRequest, true);

        ExtensionPointNodeResolver extensionPointNodeResolver = getExtensionPointNodeResolver();
        AbstractNodeContextualProposalComputer nodeContextualProposalComputer = extensionPointNodeResolver.resolve(
                nodePath, context, contentAssistRequest,
                getExtensionProposalProcessor(context));

        nodeContextualProposalComputer.addAttributeNameProposals(
                contentAssistRequest, offset, prefix);

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

        ExtensionPointNodeResolver extensionPointNodeResolver = getExtensionPointNodeResolver();
        AbstractNodeContextualProposalComputer nodeCtxProposalComputer = extensionPointNodeResolver.resolve(
                nodePath, context, contentAssistRequest,
                getExtensionProposalProcessor(context));
        nodeCtxProposalComputer.addTagNameProposal(contentAssistRequest,
                offset, prefix);

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

}
