/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.ide.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
public final class JarUtils {

    // Utility class.
    private JarUtils() {
    }

    public static boolean isBundle(File file) {
        Manifest mf = getManifest(file);
        if (mf == null) {
            return false;
        }
        String bundleName = mf.getMainAttributes().getValue(
                "Bundle-SymbolicName");
        if (bundleName == null) {
            return false;
        }
        return true;
    }

    public static Manifest getManifest(File file) {
        try {
            if (file.isDirectory()) {
                return getDirectoryManifest(file);
            } else {
                return getJarManifest(file);
            }
        } catch (IOException ignored) {
            return null;
        }
    }

    public static Manifest getDirectoryManifest(File file) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(new File(file, "META-INF/MANIFEST.MF"));
            return new Manifest(fis);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    public static Manifest getJarManifest(File file) throws IOException {
        JarFile jar = null;
        try {
            jar = new JarFile(file);
            return jar.getManifest();
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
    }

    public static Manifest getManifest(URL url) {
        try (JarFile file=new JarFile(new File(url.getFile()))) {
            return file.getManifest();
        } catch (IOException e) {
            return null;
        }
    }


    public static InputStream getPom(File file) throws IOException, URISyntaxException {
        if (file.isDirectory()) {
            return getDirectoryPom(file);
        } else {
            return getJarPom(file);
        }
    }

    protected static class PomFinder extends SimpleFileVisitor<Path> {

        protected Path pom;

        boolean inMetaInf;

        @Override
        public FileVisitResult preVisitDirectory(Path dir,
                BasicFileAttributes attrs) throws IOException {
            if (inMetaInf) {
                return FileVisitResult.CONTINUE;
            }
            if ("meta-inf".equals(dir.getFileName().toString().toLowerCase())) {
                inMetaInf = true;
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                throws IOException {
            if (inMetaInf) {
                if ("meta-inf".equals(dir.getFileName().toString().toLowerCase())) {
                    return FileVisitResult.TERMINATE;
                }
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
            if (!"pom.properties".equals(file.getFileName().toString())) {
                return FileVisitResult.CONTINUE;
            }
            pom = file;
            return FileVisitResult.TERMINATE;
        }

    }
    public static InputStream getDirectoryPom(File file) throws IOException {
        PomFinder finder = new PomFinder();
        Files.walkFileTree(Paths.get(file.toURI()), finder);
        if (finder.pom == null) {
            throw new FileNotFoundException("Cannot find pom in " + file.getName());
        }
        return new FileInputStream(finder.pom.toFile());
    }

    public static InputStream getJarPom(File file) throws IOException, URISyntaxException {
        try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> it = jar.entries();
            while (it.hasMoreElements()) {
                JarEntry each = it.nextElement();
                if (each.getName().endsWith("/pom.xml")) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try (InputStream input = jar.getInputStream(each)) {
                        IOUtils.copy(input, bos);
                    }
                    return new ByteArrayInputStream(bos.toByteArray());
                }
            }
        }
        throw new FileNotFoundException("Cannot find pom in " + file.getName());
    }
}
