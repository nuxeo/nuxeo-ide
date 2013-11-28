package org.nuxeo.ide.sdk.java;

import org.nuxeo.ide.sdk.NuxeoSDK;


public class SDKUserlibClasspathContainerInitializer extends
		SDKClasspathContainerInitializer {

	@Override
	protected SDKClasspathContainer container() {
		return NuxeoSDK.getDefault().userlibClasspathContainer;
	}
}
