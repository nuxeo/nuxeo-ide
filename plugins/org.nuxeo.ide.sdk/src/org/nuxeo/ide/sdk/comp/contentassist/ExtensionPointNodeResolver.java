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

import java.util.List;

import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.nuxeo.ide.sdk.comp.contentassist.contextualproposal.AbstractNodeContextualProposalComputer;
import org.nuxeo.ide.sdk.comp.contentassist.contextualproposal.RootNodeContextualProposalComputer;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class ExtensionPointNodeResolver {

    protected int lastBeginPosition = 0;

    private AbstractNodeContextualProposalComputer nodeContextualProposalComputer;

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
     * @return
     */
    public AbstractNodeContextualProposalComputer resolve(List<Node> nodePath,
            CompletionProposalInvocationContext context,
            ContentAssistRequest contentAssistRequest,
            ExtensionProposalProcessor extensionProposalProcessor) {
        // to avoid recomputing all each time
        int replacementBeginPosition = contentAssistRequest.getReplacementBeginPosition();
        if (lastBeginPosition == replacementBeginPosition) {
            return nodeContextualProposalComputer;
        }
        lastBeginPosition = replacementBeginPosition;

        NodeContext nodeContext = new NodeContext();
        nodeContext.setExtensionProposalProcessor(extensionProposalProcessor);
        nodeContext.setCompletionProposalInvocationContext(context);
        nodeContextualProposalComputer = new RootNodeContextualProposalComputer(
                nodeContext);

        for (int i = 0; i < nodePath.size(); i++) {
            Node currentNode = nodePath.get(i);
            nodeContextualProposalComputer = nodeContextualProposalComputer.getNextNodeContextualProposal(
                    currentNode, i);
            if (nodeContextualProposalComputer == null) {
                break;
            }
        }
        return nodeContextualProposalComputer;

    }

}
