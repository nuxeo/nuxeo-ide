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

import java.util.List;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IType;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.nuxeo.ide.sdk.comp.contentassist.DescriptorNodeProposalProcessor;
import org.nuxeo.ide.sdk.comp.contentassist.NodeContext;
import org.nuxeo.ide.sdk.comp.contentassist.NuxeoXmlComponentProposalComputer;
import org.w3c.dom.Node;

/**
 * Propose tag names and attributes for the &lt;extension&gt; tag.
 *
 * @author Sun Seng David TAN (sunix@sunix.org)
 *
 */
@SuppressWarnings("restriction")
public class ExtensionNodeContextualProposalComputer extends
        AbstractNodeContextualProposalComputer {

    public ExtensionNodeContextualProposalComputer(NodeContext nodeContext) {
        super(nodeContext);
    }

    @Override
    public AbstractNodeContextualProposalComputer getNextNodeContextualProposal(
            Node currentNode, int position) {
        if (nodeContext.getDescriptorCandidates() != null
                && !nodeContext.getDescriptorCandidates().isEmpty()) {
            DescriptorNodeProposalProcessor descriptorNodeProposalProcessor = new DescriptorNodeProposalProcessor();
            nodeContext.setNodeDescriptorType(descriptorNodeProposalProcessor.findMatchingDescriptorType(
                    nodeContext.getDescriptorCandidates(), currentNode));
            // so the next node, we don't get into this
            nodeContext.setDescriptorCandidates(null);
            nodeContext.setCurrentNodePath("");
            return new ExtensionDescriptorNodeContextualProposalComputer(
                    nodeContext);
        }
        return null;
    }

    @Override
    public void addAttributeNameProposals(
            ContentAssistRequest contentAssistRequest, int offset, String prefix) {
        nodeContext.getExtensionProposalProcessor().findAndAddExtensionProposal(
                prefix, contentAssistRequest, offset);
    }

    @Override
    public void addTagNameProposal(ContentAssistRequest contentAssistRequest,
            int offset, String prefix) {
        List<IType> descriptorCandidates = nodeContext.getDescriptorCandidates();
        if (descriptorCandidates != null) {
            for (IType iType : descriptorCandidates) {
                // get the xobject field of each type
                IAnnotation xobjectAnnotation = iType.getAnnotation(NuxeoXmlComponentProposalComputer.XOBJECT);

                if (xobjectAnnotation == null || !xobjectAnnotation.exists()) {
                    continue;
                }
                String xobjectName = getAnnotationValue(xobjectAnnotation);

                proposeTag(contentAssistRequest, offset, prefix, xobjectName,
                        "icons/comp/xpoint.gif");
            }
        }

    }

}
