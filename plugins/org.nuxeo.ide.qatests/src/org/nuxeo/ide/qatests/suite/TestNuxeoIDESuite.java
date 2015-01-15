/*
 * (C) Copyright 2015 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors: Nuxeo contributors
 */
package org.nuxeo.ide.qatests.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.nuxeo.ide.qatests.suite.usecases.TestProjectCreation;
import org.nuxeo.ide.qatests.suite.usecases.TestSDKConfiguration;
import org.nuxeo.ide.qatests.suite.usecases.TestSeamWizard;

@RunWith(Suite.class)
@SuiteClasses({ TestSDKConfiguration.class, TestProjectCreation.class, TestSeamWizard.class })
public class TestNuxeoIDESuite {

}
