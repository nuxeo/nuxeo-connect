nuxeo-connect-client
=============

Nuxeo Connect Client is the Java client used by the Nuxeo platform and the `nuxeoctl` command line tool to communicate with the Nuxeo Connect Server.

# Building

    mvn clean install

# Release

This project uses jgitver for versioning, see https://github.com/jgitver/jgitver.

To release this project just push an annotated Git tag following the 'vX.Y.Z' pattern on any branch
```
    git tag -a vX.Y.Z -m "Release X.Y.Z"
    git push origin vX.Y.Z
```

# About Nuxeo

Nuxeo dramatically improves how content-based applications are built, managed and deployed, making customers more agile, innovative and successful. Nuxeo provides a next generation, enterprise ready platform for building traditional and cutting-edge content oriented applications. Combining a powerful application development environment with SaaS-based tools and a modular architecture, the Nuxeo Platform and Products provide clear business value to some of the most recognizable brands including Verizon, Electronic Arts, Sharp, FICO, the U.S. Navy, and Boeing. Nuxeo is headquartered in New York and Paris. More information is available at www.nuxeo.com.
