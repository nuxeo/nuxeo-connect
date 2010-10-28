package org.nuxeo.connect.packages.dependencies;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.packages.PackageManagerImpl;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;

public class DependencyResolver {

    protected PackageManagerImpl pm;

    protected final static int MAX_DEPTH=10;

    protected static Log log = LogFactory.getLog(DependencyResolver.class);

    public DependencyResolver(PackageManagerImpl pm) {
        this.pm=pm;
    }

    public DependencyResolution resolve(String pkgId)  throws DependencyException{

        // compute possible dependecy sets
        log.info("Computing possible dependency sets");
        DependencyChoicesResolver choices = computeAvailableChoices(pkgId);
        log.info("Resulting choices : ");
        log.info(choices.toString());
        log.info("Max possibilities : " + choices.getNaxPossibilities());

        // sort in order to avoid downloads and updates
        log.info("Sorting choices");
        choices.sort(pm);
        log.info("Sorted choices : ");
        log.info(choices.toString());

        // try to resolve
        DependencyResolution res = choices.tryResolve();
        if (res!=null) {
            res.sort(pm);
            return res;
        }

        throw new DependencyException("Unable to resolve dependencies");
    }

    // walk dep tree to find all possible needed versions of packages
    protected DependencyChoicesResolver computeAvailableChoices(String pkgId) throws DependencyException{
        DependencyChoicesResolver dc = new DependencyChoicesResolver(pkgId,pm);
        recurseOnAvailableChoices(pkgId, dc, 1);
        return dc;
    }

    protected void recurseOnAvailableChoices(String pkgId, DependencyChoicesResolver dc, int depth) throws DependencyException {
        Package pkg = pm.findPackageById(pkgId);
        if (pkg==null) {
            throw new DependencyException("Unable to find package " + pkgId);
        }
        for (PackageDependency dep :  pkg.getDependencies()) {
            List<Version> versions = pm.getAvailableVersion(dep.getName(), dep.getVersionRange());
            if (versions.size()==0) {
                throw new DependencyException("Unable to find a compatible version for package " + dep.getName() + " (" + dep.getVersionRange().toString()+")");
            }
            dc.addDep(dep.getName(), versions);
            if (depth>=MAX_DEPTH) {
                throw new DependencyException("Maximum depth reached, check that you don't have a loop in dependencies");
            }
            for (Version v : versions) {
                recurseOnAvailableChoices(dep.getName()+ "-" + v.toString(), dc, depth+1);
            }
        }
    }

}
