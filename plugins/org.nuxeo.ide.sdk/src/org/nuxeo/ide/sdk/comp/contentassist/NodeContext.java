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

import org.eclipse.jdt.core.IType;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;

/**
 * While traversing the node path with {@link ExtensionPointNodeResolver}, an
 * instance of {@link NodeContext} will pass and update some useful information from node to
 * node. These information will provides all the informations needed to propose
 * tag names or attributes for a current node.
 *
 * @author Sun Seng David TAN (sunix@sunix.org)
 *
 */
public class NodeContext {
    IType nodeDescriptorType = null;

    String currentNodePath = null;

    List<IType> descriptorCandidates = null;

    protected ExtensionProposalProcessor extensionProposalProcessor;

    protected CompletionProposalInvocationContext context;

    /**
     * The node path, relative to a last Descriptor node.
     */
    public String getCurrentNodePath() {
        return currentNodePath;
    }

    /**
     * Only used for &lt;extension&gt; tag, provides the possible descriptor to
     * be used.
     *
     * @return
     */
    public List<IType> getDescriptorCandidates() {
        return descriptorCandidates;
    }

    /**
     * the last Descriptor type (java).
     *
     * @return
     */
    public IType getNodeDescriptorType() {
        return nodeDescriptorType;
    }

    public void setCurrentNodePath(String currentNodePath) {
        this.currentNodePath = currentNodePath;
    }

    public void setDescriptorCandidates(List<IType> descriptorCandidates) {
        this.descriptorCandidates = descriptorCandidates;
    }

    public void setNodeDescriptorType(IType nodeDescriptorType) {
        this.nodeDescriptorType = nodeDescriptorType;
    }

    public ExtensionProposalProcessor getExtensionProposalProcessor() {
        return extensionProposalProcessor;
    }

    public void setExtensionProposalProcessor(
            ExtensionProposalProcessor extensionProposalProcessor) {
        this.extensionProposalProcessor = extensionProposalProcessor;

    }

    public void setCompletionProposalInvocationContext(
            CompletionProposalInvocationContext context) {
        this.context = context;
    }

    public CompletionProposalInvocationContext getCompletionProposalInvocationContext() {
        return context;
    }
}
