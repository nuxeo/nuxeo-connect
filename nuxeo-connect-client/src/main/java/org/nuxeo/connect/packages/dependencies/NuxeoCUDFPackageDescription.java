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
 * Contributors:
 *     jcarsique
 */
package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.List;

import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;

/**
 * Simple representation of a {@link NuxeoCUDFPackage} with only String fields.
 *
 * @since 1.4.20
 */
public class NuxeoCUDFPackageDescription extends NuxeoCUDFPackage implements CUDFPackage,
        Comparable<NuxeoCUDFPackageDescription> {

    private List<PackageDependency> dependencies = new ArrayList<>();

    private List<PackageDependency> conflicts = new ArrayList<>();

    private List<PackageDependency> provides = new ArrayList<>();

    public void setCUDFName(String name) {
        super.cudfName = name;
    }

    @Override
    public String getNuxeoName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Version getNuxeoVersion() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NuxeoCUDFPackageDescription [CUDFName=").append(getCUDFName()) //
        .append(", CUDFVersion=").append(getCUDFVersion()) //
        .append(", installed=").append(isInstalled());
        if (dependencies != null && !dependencies.isEmpty()) {
            builder.append("dependencies=").append(dependencies).append(", ");
        }
        if (conflicts != null && !conflicts.isEmpty()) {
            builder.append("conflicts=").append(conflicts).append(", ");
        }
        if (provides != null && !provides.isEmpty()) {
            builder.append("provides=").append(provides).append(", ");
        }
        builder.append("]");
        return builder.toString();
    }

    public void setDependencies(List<PackageDependency> dependencies) {
        this.dependencies = dependencies;
    }

    @Override
    public PackageDependency[] getDependencies() {
        return dependencies.toArray(new PackageDependency[dependencies.size()]);
    }

    public void setConflicts(List<PackageDependency> conflicts) {
        this.conflicts = conflicts;
    }

    @Override
    public PackageDependency[] getConflicts() {
        return conflicts.toArray(new PackageDependency[conflicts.size()]);
    }

    public void setProvides(List<PackageDependency> provides) {
        this.provides = provides;
    }

    @Override
    public PackageDependency[] getProvides() {
        return provides.toArray(new PackageDependency[provides.size()]);
    }

    @Override
    public String getNuxeoId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCUDFName() == null) ? 0 : getCUDFName().hashCode());
        result = prime * result + getCUDFVersion();
        result = prime * result + (isInstalled() ? 0 : 1);
        result = prime * result + ((conflicts == null) ? 0 : conflicts.hashCode());
        result = prime * result + ((dependencies == null) ? 0 : dependencies.hashCode());
        result = prime * result + ((provides == null) ? 0 : provides.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NuxeoCUDFPackageDescription other = (NuxeoCUDFPackageDescription) obj;
        if (conflicts == null) {
            if (other.conflicts != null) {
                return false;
            }
        } else if (!conflicts.equals(other.conflicts)) {
            return false;
        }
        if (dependencies == null) {
            if (other.dependencies != null) {
                return false;
            }
        } else if (!dependencies.equals(other.dependencies)) {
            return false;
        }
        if (provides == null) {
            if (other.provides != null) {
                return false;
            }
        } else if (!provides.equals(other.provides)) {
            return false;
        }
        if (!getCUDFName().equals(other.getCUDFName())) {
            return false;
        }
        if (getCUDFVersion() != other.getCUDFVersion()) {
            return false;
        }
        if (isInstalled() != other.isInstalled()) {
            return false;
        }

        return true;
    }

    @Override
    public int compareTo(NuxeoCUDFPackageDescription o) {
        int result = getCUDFName().compareTo(o.getCUDFName());
        if (result == 0) {
            result = getCUDFVersion() - o.getCUDFVersion();
        }
        return result;
    }

}
