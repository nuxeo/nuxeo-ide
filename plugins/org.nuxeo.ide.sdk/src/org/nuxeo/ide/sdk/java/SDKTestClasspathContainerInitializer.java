package org.nuxeo.ide.sdk.java;

import org.nuxeo.ide.sdk.NuxeoSDK;


public class SDKTestClasspathContainerInitializer extends
		SDKClasspathContainerInitializer {

	@Override
	protected SDKClasspathContainer container() {
		return NuxeoSDK.getDefault().testClasspathContainer;
	}
}
