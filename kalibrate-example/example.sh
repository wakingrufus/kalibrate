#!/usr/bin/env bash

# Grab passwords via bash so they are hidden, fails on no input for either password
# Doesn't prompt for passwords if -h or --help are used

../gradlew run -Dargs="$*"