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

import org.nuxeo.ide.sdk.comp.contentassist.NodeContext;
import org.w3c.dom.Node;

public class RootNodeContextualProposalComputer extends
        AbstractNodeContextualProposalComputer {

    public RootNodeContextualProposalComputer(NodeContext nodeContext) {
        super(nodeContext);
    }

    @Override
    public AbstractNodeContextualProposalComputer getNextNodeContextualProposal(
            Node currentNode, int position) {
        if ("component".equals(currentNode.getNodeName()) && position == 1) {
            return new ComponentNodeContextualProposalComputer(nodeContext);
        }
        if (position < 1) {
            return this;
        }
        return null;
    }

}
