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
 *     slacoin
 */
package org.nuxeo.ide.connect;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.nuxeo.ide.common.IOUtils;
import org.nuxeo.ide.common.UI;

public class RepositoryDownloadTask implements IRunnableWithProgress {

    protected final RepositoryManager.Entry entry;

    static final String HEADER_PROPERTY = "Last-Modified";

    static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";

    public RepositoryDownloadTask(RepositoryManager.Entry entry) {
        this.entry = entry;
    }

    protected static final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,
    		Locale.US);
    
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        monitor.beginTask(
                "Download jar for " + entry.projectId + " on connect", 1);
        try {
            Map<String, List<String>> header = Connector.getDefault().getHeadJarArtifact(
                    entry.projectId);
            long lastModified = dateFormat.parse(header.get(HEADER_PROPERTY).get(0).toString()).getTime();
            if (entry.file.lastModified() < lastModified) {
                InputStream iss = Connector.getDefault().downloadJarArtifact(
                        entry.projectId);
                IOUtils.copyToFile(iss, entry.file, true);
                entry.file.setLastModified(lastModified);
            }
        } catch (Exception e) {
            UI.showError("Cannot download jar for " + entry.projectId
                    + " on connect", e);
        }
        monitor.done();
    }

    protected String convertStreamToString(InputStream inputStream)
            throws IOException {
        if (inputStream != null) {
            StringBuilder sb = new StringBuilder();
            String line;
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream,
                                StandardCharsets.UTF_8));
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            } finally {
                inputStream.close();
            }
            return sb.toString();
        } else {
            return "";
        }
    }

}
