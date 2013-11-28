/*
 * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     bstefanescu
 */
package org.nuxeo.ide.sdk.java;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.nuxeo.ide.sdk.NuxeoSDK;
import org.nuxeo.ide.sdk.SDKRegistry;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class SDKClasspathContainer implements IClasspathContainer {

    public static final String USERLIB_ID = "org.nuxeo.ide.SDK_USERLIB_CONTAINER";

	public static final String TEST_ID = "org.nuxeo.ide.SDK_TEST_CONTAINER";

	public static final String MAIN_ID = "org.nuxeo.ide.SDK_CONTAINER";
	
    public static final IClasspathEntry[] EMPTY_CP = new IClasspathEntry[0];

	public static final String[] IDS = new String[] {
		MAIN_ID, TEST_ID, USERLIB_ID
	};
    
    public final String id;
    
    public final IPath path;
    
    public final String desc;
        
    public SDKClasspathContainer(String id, String desc) {
    	this.id = id;
    	this.path = new Path(id);
    	this.desc = desc;
    }
    
    
    public static IPath path(String id) {
    	return new Path(id);
    }
    
    
    @Override
    public IClasspathEntry[] getClasspathEntries() {
        if (!SDKRegistry.useSDKClasspath()) {
            return EMPTY_CP;
        }
        NuxeoSDK sdk = NuxeoSDK.getDefault();
        if (sdk == null) {
        	return EMPTY_CP;
        }
        return sdk.getClasspathEntries(this);
    }

    @Override
    public String getDescription() {
    	return "Nuxeo SDK ("+desc+")";
    }

    @Override
    public int getKind() {
        return K_APPLICATION;
    }

    @Override
    public IPath getPath() {
        return path;
    }


	@Override
	public String toString() {
		return "SDKClasspathContainer [id=" + id + "]";
	}

}
