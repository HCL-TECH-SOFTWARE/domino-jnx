#!/usr/bin/env bash
#
# ==========================================================================
# Copyright (C) 2019-2022 HCL America, Inc. ( http://www.hcl.com/ )
#                            All rights reserved.
# ==========================================================================
# Licensed under the  Apache License, Version 2.0  (the "License").  You may
# not use this file except in compliance with the License.  You may obtain a
# copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
#
# Unless  required  by applicable  law or  agreed  to  in writing,  software
# distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
# WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the  specific language  governing permissions  and limitations
# under the License.
# ==========================================================================
#


# This script should be run from the host machine. It builds the Docker container
#   and executes the Maven build within it

set -e

cd "$(dirname "${BASH_SOURCE[0]}")"

# Build the image and execute a Maven install
docker build .. -f build-nobind.Dockerfile -t jnx-build
docker run \
    --rm \
    --user $(id -u):$(id -g) \
    jnx-build \
	mvn -f /build/pom.xml clean install -Prun-tests
