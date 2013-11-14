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
package org.nuxeo.ide.connect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.QualifiedName;
import org.nuxeo.ide.connect.studio.DocumentSchema;
import org.nuxeo.ide.connect.studio.DocumentType;
import org.nuxeo.ide.connect.studio.StudioProject;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 * 
 */
public class StudioProjectBinding {

    public static QualifiedName STUDIO_BINDING_P = new QualifiedName(
            "org.nuxeo.ide", "studio.binding");
    
    public static final StudioProjectBinding EMPTY = new StudioProjectBinding();
    
    protected String[] projectIds;

    /**
     * Cache of schema paths.
     */
    private volatile String[] xpaths;

    /**
     * Projects cache
     */
    private volatile StudioProject[] projects;

    public StudioProjectBinding(String... projectIds) {
        this.projectIds = projectIds;
    }

    protected synchronized void flush() {
        projects = null;
        xpaths = null;
    }

    public String[] getProjectIds() {
        return projectIds;
    }

    protected static StudioProject[] fetchProjects(String[] projectIds) {
        ArrayList<StudioProject> projects = new ArrayList<StudioProject>(
                projectIds.length);
        HashSet<String> set = new HashSet<String>();
        for (String pid : projectIds) {
            set.add(pid);
        }
        StudioProject[] allProjects = ConnectPlugin.getStudioProvider().getProjects();
        for (StudioProject project : allProjects) {
            if (set.contains(project.getId())) {
                projects.add(project);
            }
        }
        return projects.toArray(new StudioProject[projects.size()]);
    }

    public String[] getSchemaPaths() {
        String[] _xpaths = xpaths;
        if (_xpaths == null) {
            synchronized (this) {
                if (xpaths == null) {
                    HashSet<String> result = new HashSet<String>();
                    collectSchemaPaths(result);
                    xpaths = result.toArray(new String[result.size()]);
                    Arrays.sort(xpaths);
                }
                _xpaths = xpaths;
            }
        }
        return _xpaths;
    }

    public StudioProject[] getProjects() {
        StudioProject[] _projects = projects;
        if (_projects == null) {
            synchronized (this) {
                if (projects == null) {
                    projects = fetchProjects(projectIds);
                }
                _projects = projects;
            }
        }
        return _projects;
    }

    /**
     * This is not cached since not used in completion or other sensitive tasks.
     * 
     * @return
     */
    public DocumentSchema[] getSchemas() {
        Set<DocumentSchema> result = new HashSet<DocumentSchema>();
        for (StudioProject project : getProjects()) {
            for (DocumentSchema ds : project.getPlatform().getSchemas().values()) {
                result.add(ds);
            }
            for (DocumentSchema ds : project.getDocumentSchemas()) {
                result.add(ds);
            }
        }
        DocumentSchema[] schemas = result.toArray(new DocumentSchema[result.size()]);
        Arrays.sort(schemas);
        return schemas;
    }

    public DocumentType[] getTypes() {
        Set<DocumentType> result = new HashSet<DocumentType>();
        for (StudioProject project : getProjects()) {
            for (DocumentType dt : project.getPlatform().getDoctypes().values()) {
                result.add(dt);
            }
            for (DocumentType dt : project.getDocumentTypes()) {
                result.add(dt);
            }
        }
        DocumentType[] types = result.toArray(new DocumentType[result.size()]);
        Arrays.sort(types);
        return types;
    }

    private final void collectSchemaPaths(Set<String> result) {
        for (StudioProject project : getProjects()) {
            for (DocumentSchema ds : project.getPlatform().getSchemas().values()) {
                collectSchemaPaths(ds, result);
            }
            for (DocumentSchema ds : project.getDocumentSchemas()) {
                collectSchemaPaths(ds, result);
            }
        }
    }

    private final void collectSchemaPaths(DocumentSchema ds, Set<String> result) {
        String prefix = ds.getPrefix().concat(":");
        for (DocumentSchema.Field field : ds.getFields()) {
            collectField(prefix, field, result);
        }
    }

    private final void collectField(String prefix, DocumentSchema.Field field,
            Set<String> result) {
        prefix = prefix.concat(field.getId());
        result.add(prefix);
        if (field.hasChildren()) {
            prefix = prefix.concat("/");
            for (DocumentSchema.Field f : field.getChildren()) {
                collectField(prefix, f, result);
            }
        } else if ("blob".equals(field.getType())) {
            // TODO must be sent by studio to avoid hard coding.
            prefix = prefix.concat("/");
            result.add(prefix.concat("encoding"));
            result.add(prefix.concat("mime-type"));
            result.add(prefix.concat("data"));
            result.add(prefix.concat("name"));
            result.add(prefix.concat("length"));
            result.add(prefix.concat("digest"));
        }
    }

}
