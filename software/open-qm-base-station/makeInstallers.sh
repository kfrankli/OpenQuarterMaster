#!/bin/bash
#
# Script to make installers for Station Master.
#
# Intended to be run from the dir this resides
#
# Author: Greg Stewart
#
# Requires packages:
#  Ubuntu:
#   - dpkg-dev
#   - rpm
#   - rpmlint
#   - jq
#

configFile="installerProperties.json"
buildDir="build/installers"

debDir="StationCaptainDeb"

#
# Clean
#

rm -rf "$buildDir"
#
# Setup
#

mkdir -p "$buildDir"

#
# Debian build
#

mkdir "$buildDir/$debDir"
mkdir "$buildDir/$debDir/DEBIAN"
mkdir -p "$buildDir/$debDir/etc/systemd/system/"

cp oqm_base_station.service "$buildDir/$debDir/etc/systemd/system/"

# TODO:: license information https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
# https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-binarycontrolfiles
cat <<EOT >> "$buildDir/$debDir/DEBIAN/control"
Package: $(cat "$configFile" | jq -r '.packageName')
Version: $(./gradlew -q printVersion)
Section: Open QuarterMaster
Maintainer: $(cat "$configFile" | jq -r '.maintainer.name')
Architecture: all
Description: $(cat "$configFile" | jq -r '.description')
Homepage: $(cat "$configFile" | jq -r '.homepage')
Depends: $(cat "$configFile" | jq -r '.dependencies.deb')
Licence: $(cat "$configFile" | jq -r '.copyright.licence')
EOT

cat <<EOT >> "$buildDir/$debDir/DEBIAN/copyright"
Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
Upstream-Name: Open QuarterMaster Base Station
Upstream-Contact: $(cat "$configFile" | jq -r '.copyright.contact')
Source: $(cat "$configFile" | jq -r '.homepage')

Files: *
Copyright: $(cat "$configFile" | jq -r '.copyright.copyright')
License: $(cat "$configFile" | jq -r '.copyright.licence')
EOT


cat <<EOT >> "$buildDir/$debDir/DEBIAN/preinst"
#!/bin/bash


EOT

chmod +x "$buildDir/$debDir/DEBIAN/preinst"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/postinst"
#!/bin/bash

systemctl daemon-reload
systemctl enable oqm_base_station.service
systemctl start oqm_base_station.service
EOT
chmod +x "$buildDir/$debDir/DEBIAN/postinst"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/prerm"
#!/bin/bash

systemctl disable oqm_base_station.service
systemctl stop oqm_base_station.service
EOT
chmod +x "$buildDir/$debDir/DEBIAN/prerm"

cat <<EOT >> "$buildDir/$debDir/DEBIAN/postrm"
#!/bin/bash

systemctl daemon-reload
# Remove docker image
if [[ "$(docker images -q oqm_base_station 2> /dev/null)" != "" ]]; then
        docker rmi oqm_base_station
        echo "Removed docker image."
else
        echo "Docker image was already gone."
fi
if [ $( docker ps -a | grep oqm_base_station | wc -l ) -gt 0 ]; then
        docker rm oqm_base_station
        echo "Removed docker container."
else
        echo "Docker container was already gone."
fi


EOT
chmod +x "$buildDir/$debDir/DEBIAN/postrm"

dpkg-deb --build "$buildDir/$debDir" "$buildDir"



#
# RPM build
#

