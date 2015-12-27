/*
 * (C) Copyright 2006-2009 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;

/**
*
* Because Connect Dependencies are exposed as {@link VersionRange} there can be several versions
* of a package to fulfill dependencies.
* The system will evaluate the different dep versions set.
*
* This calls is used to represent a set of Versionned Packages.
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*
*/
public class DependencySet {

    protected Map<String, Version> deps = new HashMap<String, Version>();

    protected Set<String> pkgNames;

    public DependencySet(Set<String> pkgNames) {
        this.pkgNames=pkgNames;
    }

    protected DependencySet(Set<String> pkgNames, Map<String, Version> deps) {
        this.pkgNames=pkgNames;
        this.deps=deps;
    }

    public void set(String pkgName, Version v) {
        assert pkgNames.contains(pkgName);
        deps.put(pkgName, v);
    }

    public Version getTargetVersion(String pkgName) {
        return deps.get(pkgName);
    }

    public boolean isComplete() {
        return deps.keySet().containsAll(pkgNames);
    }

    public String getNextPackageName() {
        for (String pkgName : pkgNames) {
            if (!deps.containsKey(pkgName)) {
                return pkgName;
            }
        }
        return null;
    }

    public DependencySet clone() {
        Map<String, Version> cpDeps = new HashMap<String, Version>();
        cpDeps.putAll(deps);
        return new DependencySet(pkgNames,cpDeps);
    }
}
