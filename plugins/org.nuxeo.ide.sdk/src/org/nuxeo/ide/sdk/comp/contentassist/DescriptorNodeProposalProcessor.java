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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.w3c.dom.Node;

/**
 * Helper to find the right descriptor from a list of descriptor candidates that
 * matches a current node by checking its xmap annotations.
 *
 * @author Sun Seng David TAN (sunix@sunix.org)
 *
 */
public class DescriptorNodeProposalProcessor {

    /**
     * Find the right descriptor from a list of descriptor candidates that
     * matches a current node by checking its xmap annotations.
     *
     * @param descriptorCandidates
     * @param currentNode
     * @return
     */
    public IType findMatchingDescriptorType(List<IType> descriptorCandidates,
            Node currentNode) {
        for (IType type : descriptorCandidates) {
            if (isMatchingDescriptorType(type, currentNode)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Check if the type is matching with the current node by checking its xmap
     * annotation
     *
     * @param type
     * @param currentNode
     * @return
     */
    protected boolean isMatchingDescriptorType(IType type, Node currentNode) {
        try {

            IAnnotation[] annotations = type.getAnnotations();
            for (IAnnotation iAnnotation : annotations) {

                if (NuxeoXmlComponentProposalComputer.XOBJECT.equals(iAnnotation.getElementName())) {
                    String elementName = null;
                    IMemberValuePair[] memberValuePairs = iAnnotation.getMemberValuePairs();
                    for (IMemberValuePair member : memberValuePairs) {
                        if ("value".equals(member.getMemberName())) {
                            elementName = (String) member.getValue();
                            break;
                        }
                    }
                    if (currentNode.getNodeName().equals(elementName)) {
                        return true;
                    }

                }

            }
        } catch (JavaModelException e) {
            SDKPlugin.log(IStatus.ERROR,
                    "An error occured while parsing the type xmap annotation",
                    e);
        }
        return false;
    }

}
