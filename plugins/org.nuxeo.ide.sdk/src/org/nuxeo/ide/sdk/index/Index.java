/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
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
 *     bstefanescu, jcarsique
 */
package org.nuxeo.ide.sdk.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IStatus;
import org.nuxeo.ide.common.UI;
import org.nuxeo.ide.sdk.SDKPlugin;
import org.nuxeo.ide.sdk.model.Artifact;
import org.nuxeo.ide.sdk.model.PomModel;
import org.nuxeo.ide.sdk.userlibs.UserLib;
import org.nuxeo.ide.sdk.userlibs.UserLibPreferences;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public class Index {

    public static Index load(File file, String builtinIndex) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            final Pattern filePattern = Pattern.compile(file.getName());
            File[] files = file.getParentFile().listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File directory, String filename) {
                    return filePattern.matcher(filename).matches();
                }
            });
            for (File indexFile : files) {
                map.putAll(loadIndex(indexFile));
            }
            if (files.length == 0 && builtinIndex != null) {
                SDKPlugin.log(IStatus.WARNING,
                        "Fallback on builtin index file.");
                map = loadBuiltinIndex(builtinIndex);
            }
        } catch (Throwable t) {
            SDKPlugin.log(IStatus.ERROR, "Couldn't load index.", t);
        }
        return new Index(map);
    }

    public static Map<String, String> loadIndex(URL url) throws IOException {
        InputStream in = url.openStream();
        try {
            return loadIndex(in);
        } finally {
            in.close();
        }
    }

    public static Map<String, String> loadIndex(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        try {
            return loadIndex(in);
        } finally {
            in.close();
        }
    }

    public static Map<String, String> loadIndex(InputStream in)
            throws IOException {
        HashMap<String, String> index = new HashMap<String, String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String line = reader.readLine();
        while (line != null) {
            line = line.trim();
            if (line.length() != 0 && !line.startsWith("#")) {
                int i = line.indexOf('=');
                if (i > -1) {
                    index.put(line.substring(0, i).trim(),
                            line.substring(i + 1).trim());
                }
            }
            line = reader.readLine();
        }
        return index;
    }

    public static Map<String, String> loadBuiltinIndex(String indexName)
            throws IOException {
        URL url = Index.class.getResource(indexName);
        if (url != null) {
            return loadIndex(url);
        }
        return null;
    }

    protected Map<String, String> index;

    public Index(Map<String, String> map) {
        index = map;
    }

    public final Map<String, String> getIndex() {
        return index;
    }

    public final String get(String key) {
        return index.get(key);
    }

    public Artifact resolve(Dependency dep) {
        if (dep.isBinary()) {
            String name = dep.getJar().getName();
            String gav = getIndex().get(name);
            if (gav != null) {
                return Artifact.fromGav(gav);
            } else { // try user libraries
                try {
                    UserLib lib = UserLibPreferences.load().getUserLibs().get(
                            name);
                    if (lib != null) {
                        return lib.getArtifact();
                    }
                } catch (Exception e) {
                    UI.showError("Cannot resolve dependency", e);
                }
            }
        } else {
            try {
                PomModel pom = PomModel.getPomModel(dep.getProject().getProject());
                String groupId = pom.getGroupId();
                if (groupId != null) {
                    String artifactId = pom.getArtifactId();
                    if (artifactId != null) {
                        return new Artifact(groupId, artifactId,
                                pom.getArtifactVersion());
                    }
                }
            } catch (Exception e) {
                UI.showError("Cannot resolve dependency", e);
            }
        }
        return null;
    }

    public DependencyEntry[] resolve(Collection<Dependency> deps) {
        DependencyEntry unresolved = new DependencyEntry(new Artifact("*", "*"));
        TreeMap<String, DependencyEntry> map = new TreeMap<String, DependencyEntry>();
        for (Dependency dep : deps) {
            Artifact artifact = resolve(dep);
            if (artifact != null) {
                DependencyEntry entry = map.get(artifact.getId());
                if (entry == null) {
                    entry = new DependencyEntry(artifact);
                    map.put(artifact.getId(), entry);
                }
                entry.addDependency(dep);
            } else {
                unresolved.addDependency(dep);
            }
        }
        if (!unresolved.isEmpty()) {
            map.put(unresolved.getArtifact().getId(), unresolved);
        }
        return map.values().toArray(new DependencyEntry[map.size()]);
    }

    public void merge(Index artifactIndex) {
        index.putAll(artifactIndex.index);
    }

}
