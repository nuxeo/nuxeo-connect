package org.nuxeo.connect.packages;

import java.util.List;

import org.nuxeo.connect.data.DownloadablePackage;
import org.nuxeo.connect.update.Package;
import org.nuxeo.connect.update.Version;
import org.nuxeo.connect.update.VersionRange;

/**
 * Interface used by the Dependency resolution system to access the {@link Package}
 *
 * @author <a href="mailto:td@nuxeo.com">Thierry Delprat</a>
 */
public interface InternalPackageManager extends BasePackageManager{

    /**
     * Return the available {@link Version} for a given {@link Package} name.
     * Versions are sorted in the "prefered order" :
     *  - already installed version (means no upgrade and no download)
     *  - already downloaded version (means no download)
     *  - remote versions sorted by version number (higher => last)
     *
     * @param pkgName
     * @return
     */
    List<Version> getPreferedVersions(String pkgName);

    /**
     * Returns all remote {@link Package} versions for a given name
     *
     * @param packageName
     * @return
     */
    List<DownloadablePackage> findRemotePackages(String packageName);

    /**
     * Find a {@link Package} by it's id
     * (will find masked versions on the contrary of {@link PackageManager} getPackage
     *
     * @param packageId
     * @return
     */
    DownloadablePackage findPackageById(String packageId);

    /**
     * Returns all local {@link Package} versions for a given name
     *
     * @param packageName
     * @return
     */
    List<Version> findLocalPackageVersions(String packageName);

    /**
     * Returns all local {@link Package} installed versions for a given name
     *
     * @param packageName
     * @return
     */
    List<Version> findLocalPackageInstalledVersions(String packageName);

    /**
     * Returns all {@link Package} versions for a given name and {@link VersionRange}
     *
     * @param packageName
     * @return
     */
    List<Version> getAvailableVersion(String pkgName, VersionRange range, String targetPlatform);
}
