/*
 * (C) Copyright 2006-2010 Nuxeo SA (http://nuxeo.com/) and others.
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
package org.nuxeo.connect.update;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.nuxeo.connect.update.PackageDependency.PackageDependencyXMLAdapter;

/**
 * Example of dependencies:
 * <ul>
 * <li>Any version of my-package (the last available version will be used)
 * <code>my-package</code>
 * <li>Minimum 1.0 version my-package: <code>my-package:1</code>
 * <li>Maximum 2.0 version my-package: <code>my-package:0:1</code>
 * <li>Versions between 1.1 and 2.3 of my-package:
 * <code>my-package:1.1:2.3</code>
 * </ul>
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 *
 */
@XmlJavaTypeAdapter(PackageDependencyXMLAdapter.class)
public class PackageDependency {

    /**
     * @since 1.4.5
     */
    public static class PackageDependencyXMLAdapter extends
            XmlAdapter<String, PackageDependency> {

        @Override
        public String marshal(PackageDependency dependency) throws Exception {
            return dependency.toString();
        }

        @Override
        public PackageDependency unmarshal(String dependency) throws Exception {
            return new PackageDependency(dependency);
        }
    }

    protected String name;

    protected VersionRange range = VersionRange.ANY;

    public PackageDependency(String expr) {
        int p = expr.indexOf(':');
        if (p == -1) {
            name = expr;
        } else {
            name = expr.substring(0, p);
            range = new VersionRange(expr.substring(p + 1));
        }
    }

    public PackageDependency(String name, Version minVersion) {
        this(name, new VersionRange(minVersion));
    }

    public PackageDependency(String name, Version minVersion, Version maxVersion) {
        this(name, new VersionRange(minVersion, maxVersion));
    }

    public PackageDependency(String name, VersionRange range) {
        this.name = name;
        this.range = range;
    }

    public String getName() {
        return name;
    }

    public VersionRange getVersionRange() {
        return range;
    }

    @Override
    public String toString() {
        String vr = range.toString();
        if (vr.length() == 0) {
            return name;
        } else {
            return name + ':' + vr;
        }
    }
}
