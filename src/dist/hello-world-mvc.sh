#!/bin/sh
# =============================================================================
# Hello World MVC - launcher for Linux, macOS and Git Bash on Windows.
# Authors: Aritz Navarro, Brayan Romero, Ekaitz Rivero
#
# Runs the application with the Java runtime bundled in the "jre" folder, so no
# Java installation is needed.
# =============================================================================

# Work from the folder of this script, so that config.properties, data/ and
# logs/ are found whatever folder the script is started from
cd "$(dirname "$0")" || exit 1

# The application jar is a module; the MySQL driver in lib/ stays on the class path
exec jre/bin/java \
    --enable-native-access=javafx.graphics \
    --module-path hello-world-mvc.jar \
    --class-path "lib/*" \
    --module tartanga.dami2.din.helloworldmvc/tartanga.dami2.din.helloworldmvc.App \
    "$@"
