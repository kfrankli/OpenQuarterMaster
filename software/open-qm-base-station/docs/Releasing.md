# Releasing

These are the steps to take to perform a release of the software:

1. Run _all_ tests `./gradlew clean test ` # TODO:: when integration/native tests work: `quarkusIntTest quarkusIntTest -Dquarkus.package.type=native -Dquarkus.native.container-build=true -Dquarkus.container-image.build=true`
2. Increment version of service accordingly in `build.gradle`
3. Ensure everything committed and pushed to github. Check workflows.
4. Be logged into docker hub `docker login`
5. Deploy jvm version
   1. Clean/build project `./gradlew clean build -Pquarkus.container-image.build=true`
   2. Retag image: `docker image tag gstewart/open-qm-base-station:1.0.0-DEV ebprod/open-qm-base-station-jvm:1.0.0-DEV`
   3. Push to DockerHub: `docker push ebprod/open-qm-base-station-jvm:1.0.0-DEV`
6. Deploy native version (when above TODO fixed)
   1. Clean/build project (jvm) `./gradlew clean build -Pquarkus.container-image.build=true -Pquarkus.package.type=native`
7. Make installers: `./makeInstallers.sh`
8. Make release for version on Github, attach all installers to it (`build/installers`)