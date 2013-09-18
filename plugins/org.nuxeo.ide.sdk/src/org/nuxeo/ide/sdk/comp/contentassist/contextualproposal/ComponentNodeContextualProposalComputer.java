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

import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.nuxeo.ide.sdk.comp.contentassist.NodeContext;
import org.w3c.dom.Node;

/**
 * Propose tag names and attributes for the &lt;component&gt; tag.
 *
 * @author Sun Seng David TAN (sunix@sunix.org)
 *
 */
@SuppressWarnings("restriction")
public class ComponentNodeContextualProposalComputer extends
        AbstractNodeContextualProposalComputer {

    public ComponentNodeContextualProposalComputer(NodeContext nodeContext) {
        super(nodeContext);
    }

    @Override
    public AbstractNodeContextualProposalComputer getNextNodeContextualProposal(
            Node currentNode, int position) {
        if ("extension".equals(currentNode.getNodeName()) && position == 2) {
            nodeContext.setDescriptorCandidates(nodeContext.getExtensionProposalProcessor().getDescriptorCandidates(
                    currentNode));
            return new ExtensionNodeContextualProposalComputer(nodeContext);
        }
        return null;
    }

    @Override
    public void addAttributeNameProposals(
            ContentAssistRequest contentAssistRequest, int offset, String prefix) {

        contentAssistRequest.addProposal(new CompletionProposal("name=\"\" ",
                offset - prefix.length(), prefix.length(), 6,
                SDKPlugin.getDefault().getImageRegistry().get(
                        "icons/comp/component.gif"), "name - Component name ",
                null, "The name of the component"));
        return;
    }

    @Override
    public void addTagNameProposal(ContentAssistRequest contentAssistRequest,
            int offset, String prefix) {
        // can propose extension
        proposeTag(contentAssistRequest, offset, "extension".length(), prefix,
                "extension", "extension", "icons/comp/xpoint.gif",
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

        proposeTag(contentAssistRequest, offset, 14, prefix, "documentation",
                "documentation></documentation>", "icons/comp/component.gif",
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
}
