Releasing
=========

1. Update `CHANGELOG.md`.

2. Set versions:

    ```
    export RELEASE_VERSION=X.Y.Z
    export NEXT_VERSION=X.Y.Z-SNAPSHOT
    ```

3. Update, build, and upload:

    ```
    sed -i "" \
      "s/VERSION_NAME=.*/VERSION_NAME=$RELEASE_VERSION/g" \
      gradle.properties
    sed -i "" \
      "s/\"app.cash.barber:\([^\:]*\):[^\"]*\"/\"app.cash.barber:\1:$RELEASE_VERSION\"/g" \
      `find . -name "README.md"`
    ./gradlew clean uploadArchives
    ```

4. Visit [Sonatype Nexus](https://oss.sonatype.org/) to promote (close then release) the artifact. Or drop it if there is a problem!

5. Tag the release, prepare for the next one, and push to GitHub.

    ```
    git commit -am "Prepare for release $RELEASE_VERSION."
    git tag -a barber-$RELEASE_VERSION -m "Version $RELEASE_VERSION"
    sed -i "" \
      "s/VERSION_NAME=.*/VERSION_NAME=$NEXT_VERSION/g" \
      gradle.properties
    git commit -am "Prepare next development version."
    git push && git push --tags
    ```


Prerequisites
-------------

Generate a GPG key (RSA, 4096 bit, 3650 day) expiry, or use an existing one.
```
$ gpg --full-generate-key 
```

Upload the GPG keys to public servers

```
$ gpg --list-keys --keyid-format LONG
/Users/johnbarber/.gnupg/pubring.kbx
------------------------------
pub   rsa4096/XXXXXXXXXXXXXXXX 2019-07-16 [SC] [expires: 2029-07-13]
      YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY
uid           [ultimate] John Barber <jbarber@cash.app>
sub   rsa4096/ZZZZZZZZZZZZZZZZ 2019-07-16 [E] [expires: 2029-07-13]

$ gpg --send-keys --keyserver keyserver.ubuntu.com XXXXXXXXXXXXXXXX
```

In `~/.gradle/gradle.properties`, set the following:

 * `SONATYPE_NEXUS_USERNAME` - Sonatype username for releasing to `app.cash`.
 * `SONATYPE_NEXUS_PASSWORD` - Sonatype password for releasing to `app.cash`.
 * `signing.keyId` - key ID for GPG key. Example: `1A2345F8`. Get with the following command: 
   ```
   $ gpg --list-keys --keyid-format SHORT
   ```
 * `signing.password` - password for GPG key, recommended to be empty.
 * `signing.secretKeyRingFile` - absoluate file path for `secring.gpg`. Example: `/Users/johnbarber/.gnupg/secring.gpg`.
   * You may need to export this file manually with the following command where `XXXXXXXX` is the same `keyId` as above:
     ```
     $ gpg --keyring secring.gpg --export-secret-key XXXXXXXX > ~/.gnupg/secring.gpg
     ```