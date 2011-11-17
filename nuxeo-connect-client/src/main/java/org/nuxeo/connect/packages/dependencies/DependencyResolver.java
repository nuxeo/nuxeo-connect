/*
 * (C) Copyright 2006-2009 Nuxeo SAS (http://nuxeo.com/) and contributors.
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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.connect.packages.InternalPackageManager;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;

/**
* Main entry point for Dependency resolution.
*
* This implementation does only try to resolve the Dependencies management problem in simple cases :
*  - no deep dependencies tree
*  - limited number of packages and versions
*
* If in the future we need a real dep solver, we will probably align
* Connect Package dep system of something that already exists (Debian, Maven, OSGi ...)
*
* @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
*
*/
public class DependencyResolver {

    protected InternalPackageManager pm;

    protected static Log log = LogFactory.getLog(DependencyResolver.class);

    public DependencyResolver(InternalPackageManager pm) {
        this.pm=pm;
    }

    public DependencyResolution resolve(String pkgId, String targetPlatform)  throws DependencyException{

        // compute possible dependecy sets
        log.debug("Computing possible dependency sets");
        RecursiveDependencyResolver choices = computeAvailableChoices(pkgId, targetPlatform);
        log.debug("Resulting choices : ");
        log.debug(choices.toString());
        log.debug("Max possibilities : " + choices.getNaxPossibilities());

        // sort in order to avoid downloads and updates
        log.debug("Sorting choices");
        choices.sort(pm);
        log.debug("Sorted choices : ");
        log.debug(choices.toString());

        // try to resolve
        DependencyResolution res = choices.tryResolve();
        if (res!=null) {
            res.sort(pm);
            return res;
        }

        throw new DependencyException("Unable to resolve dependencies");
    }

    // walk dep tree to find all possible needed versions of packages
    protected RecursiveDependencyResolver computeAvailableChoices(String pkgId, String targetPlatform) throws DependencyException{
        RecursiveDependencyResolver dc = new RecursiveDependencyResolver(pkgId,pm, targetPlatform);
        String path = "/" + pkgId;
        recurseOnAvailableChoices(pkgId, targetPlatform, dc, path);
        return dc;
    }

    protected void recurseOnAvailableChoices(String pkgId, String targetPlatform, RecursiveDependencyResolver dc, String path) throws DependencyException {
        Package pkg = pm.findPackageById(pkgId);
        if (pkg==null) {
            throw new DependencyException("Unable to find package " + pkgId);
        }
        for (PackageDependency dep :  pkg.getDependencies()) {
            List<Version> versions = pm.getAvailableVersion(dep.getName(), dep.getVersionRange(), targetPlatform);
            if (versions.size()==0) {
                throw new DependencyException("Unable to find a compatible version for package " + dep.getName() + " (" + dep.getVersionRange().toString()+")");
            }
            if (path.contains("/" + dep.getName() + "/")) {
                throw new DependencyException(String.format("Detected loop in dependencies: pkg=%s,dep=%s,path=%s'", pkgId, dep.getName(), path));
            }
            dc.addDep(dep.getName(), versions);
            for (Version v : versions) {
                recurseOnAvailableChoices(dep.getName()+ "-" + v.toString(),targetPlatform, dc, path + dep.getName() + "/");
            }
        }
    }
}
