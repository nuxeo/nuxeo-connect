/*
 * (C) Copyright 2012-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Nuxeo - initial API and implementation
 *     Yannis JULIENNE
 */

package org.nuxeo.connect.packages.dependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.equinox.p2.cudf.Parser;
import org.eclipse.equinox.p2.cudf.metadata.InstallableUnit;
import org.eclipse.equinox.p2.cudf.solver.ProfileChangeRequest;
import org.eclipse.equinox.p2.cudf.solver.SimplePlanner;
import org.eclipse.equinox.p2.cudf.solver.SolverConfiguration;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.packages.PackageManager;
import org.nuxeo.connect.update.PackageDependency;
import org.nuxeo.connect.update.Version;

/**
 * This implementation uses the p2cudf resolver to solve complex dependencies. <br>
 * Predefined CUDF criteria to match different behaviours :
 * <ul>
 * <li>mp-install -> {@link #SOLVER_CRITERIA_LESS_CHANGES}</li>
 * <li>mp-remove/uninstall -> {@link #SOLVER_CRITERIA_LESS_VERSION_CHANGES}</li>
 * <li>mp-upgrade -> {@link #SOLVER_CRITERIA_LESS_OUTDATED}</li>
 * <li>mp-set -> {@link #SOLVER_CRITERIA_LESS_OUTDATED_WITH_REMOVE}</li>
 * </ul>
 *
 * @since 1.4
 */
public class P2CUDFDependencyResolver implements DependencyResolver {

    /**
     * Solver criteria requesting the less changes. Used for mp-install.
     *
     * @since TODO
     */
    public static final String SOLVER_CRITERIA_LESS_CHANGES = "-removed,-changed,-notuptodate,-new,-versionchanged";

    /**
     * Solver criteria requesting the less version changes. Used for mp-remove and mp-uninstall.
     *
     * @since TODO
     */
    public static final String SOLVER_CRITERIA_LESS_VERSION_CHANGES = "-versionchanged,-removed,-changed,-notuptodate,-new";

    /**
     * Solver criteria requesting the less outdated packages. Used for mp-upgrade.
     *
     * @since TODO
     */
    public static final String SOLVER_CRITERIA_LESS_OUTDATED = "-removed,-notuptodate,-changed,-new,-versionchanged";

    /**
     * Solver criteria requesting the more removed and the less outdated packages. Used for mp-set.
     *
     * @since TODO
     */
    public static final String SOLVER_CRITERIA_LESS_OUTDATED_WITH_REMOVE = "+removed,-notuptodate,-changed,-new,-versionchanged";

    protected static Log log = LogFactory.getLog(P2CUDFDependencyResolver.class);

    protected PackageManager pm;

    protected CUDFHelper cudfHelper;

    protected P2CUDFDependencyResolver() {
    }

    public P2CUDFDependencyResolver(PackageManager pm) {
        this.pm = pm;
    }

    @Override
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform) throws DependencyException {
        return resolve(pkgInstall, pkgRemove, pkgUpgrade, targetPlatform, CUDFHelper.defaultAllowSNAPSHOT);
    }

    @Override
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT) throws DependencyException {
        return resolve(pkgInstall, pkgRemove, pkgUpgrade, targetPlatform, allowSNAPSHOT, true);
    }

    @Override
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT, boolean doKeep) throws DependencyException {
        // By default, criteria are made for install, prioritizing the solution with the less changes
        String solverCriteria = SOLVER_CRITERIA_LESS_CHANGES;
        if (!doKeep) {
            // When setting a new batch of packages (doKeep=false), criteria prioritizes the
            // solution with the less outdated packages but prefering remove over unchanged
            solverCriteria = SOLVER_CRITERIA_LESS_OUTDATED_WITH_REMOVE;
        }
        if (CollectionUtils.isNotEmpty(pkgUpgrade)) {
            // For an upgrade request, criteria prioritizes the
            // solution with the less outdated packages
            solverCriteria = SOLVER_CRITERIA_LESS_OUTDATED;
        } else if (CollectionUtils.isNotEmpty(pkgRemove)) {
            // For a remove request, criteria prioritizes the solution with the less version changed packages
            // otherwise, it would upgrade/downgrade a package instead of removing it when trying to remove a specific
            // version
            solverCriteria = SOLVER_CRITERIA_LESS_VERSION_CHANGES;
        }
        return resolve(pkgInstall, pkgRemove, pkgUpgrade, targetPlatform, allowSNAPSHOT, true, solverCriteria);
    }

    @Override
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, String solverCriteria) throws DependencyException {
        return resolve(pkgInstall, pkgRemove, pkgUpgrade, targetPlatform, CUDFHelper.defaultAllowSNAPSHOT, true,
                solverCriteria);
    }

    @Override
    public DependencyResolution resolve(List<String> pkgInstall, List<String> pkgRemove, List<String> pkgUpgrade,
            String targetPlatform, boolean allowSNAPSHOT, boolean doKeep, String solverCriteria)
            throws DependencyException {
        cudfHelper = new CUDFHelper(pm);
        cudfHelper.setTargetPlatform(targetPlatform);
        cudfHelper.setAllowSNAPSHOT(allowSNAPSHOT);
        cudfHelper.setKeep(doKeep);
        // generate CUDF package universe and request stanza
        String cudf = cudfHelper.getCUDFFile(str2PkgDep(pkgInstall), str2PkgDep(pkgRemove), str2PkgDep(pkgUpgrade));
        log.debug("CUDF request:\n" + cudf);

        // pass to p2cudf for solving
        ProfileChangeRequest req = new Parser().parse(IOUtils.toInputStream(cudf));
        SolverConfiguration configuration = new SolverConfiguration(solverCriteria);
        // Upgrade + verbose + explain is unsupported
        // verbose + explain changes results
        // if (log.isTraceEnabled()) {
        // configuration.verbose = true;
        // configuration.explain = true;
        // }
        SimplePlanner planner = new SimplePlanner();
        planner.getSolutionFor(req, configuration);
        planner.stopSolver();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Hazardous wait for the solver stop
        }
        Collection<InstallableUnit> solution = planner.getBestSolutionFoundSoFar();
        if (log.isTraceEnabled()) {
            log.trace(planner.getExplanation());
        }
        if (!planner.isSolutionOptimal()) {
            log.warn("The solution found might not be optimal");
        }
        DependencyResolution resolution = cudfHelper.buildResolution(solution, planner.getSolutionDetails());
        if (!doKeep) {
            // Make sub-resolution to remove all packages that are not part of
            // our target list
            List<String> subInstall = new ArrayList<>();
            List<String> subRemove = new ArrayList<>();
            for (Map.Entry<String, Version> e : resolution.localPackagesToInstall.entrySet()) {
                subInstall.add(e.getKey() + '-' + e.getValue().toString());
            }
            for (Map.Entry<String, Version> e : resolution.localUnchangedPackages.entrySet()) {
                subInstall.add(e.getKey() + '-' + e.getValue().toString());
            }
            for (Map.Entry<String, Version> e : resolution.newPackagesToDownload.entrySet()) {
                subInstall.add(e.getKey() + '-' + e.getValue().toString());
            }
            for (DownloadablePackage pkg : pm.listInstalledPackages()) {
                String pkgId = pkg.getId();
                if (!subInstall.contains(pkgId)) {
                    subRemove.add(pkgId);
                }
            }
            resolution = resolve(subInstall, subRemove, null, targetPlatform, allowSNAPSHOT, true);
        }
        return resolution;
    }

    private PackageDependency[] str2PkgDep(List<String> pkgList) {
        List<PackageDependency> list = new ArrayList<>();
        if (pkgList == null || pkgList.size() == 0) {
            return list.toArray(new PackageDependency[0]);
        }
        Map<String, DownloadablePackage> packagesByID = pm.getAllPackagesByID();
        for (String pkgStr : pkgList) {
            if (packagesByID.containsKey(pkgStr)) {
                DownloadablePackage pkg = packagesByID.get(pkgStr);
                list.add(new PackageDependency(pkg.getName(), pkg.getVersion(), pkg.getVersion()));
            } else {
                list.add(new PackageDependency(pkgStr));
            }
        }
        return list.toArray(new PackageDependency[list.size()]);
    }

    @Override
    @Deprecated
    public DependencyResolution resolve(String pkgIdOrName, String targetPlatform) throws DependencyException {
        List<String> pkgInstall = new ArrayList<>();
        pkgInstall.add(pkgIdOrName);
        return resolve(pkgInstall, null, null, targetPlatform);
    }

}
