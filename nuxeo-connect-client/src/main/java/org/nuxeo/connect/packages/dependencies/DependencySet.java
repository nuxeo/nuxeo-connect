package org.nuxeo.connect.packages.dependencies;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.nuxeo.connect.update.Version;

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
