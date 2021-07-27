#
# ==========================================================================
# Copyright (C) 2019-2021 HCL America, Inc. ( http://www.hcl.com/ )
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

FROM maven:3.6.3-adoptopenjdk-8
USER root

RUN apt update && apt install unzip

ENV HOME "/root"
ENV LD_LIBRARY_PATH "/opt/hcl/domino/notes/latest/linux"
ENV NotesINI "/local/notesdata/notes.ini"
ENV Notes_ExecDirectory "/opt/hcl/domino/notes/latest/linux"
ENV Directory "/local/notesdata"
ENV JNX_NOTERM "true"
ENV PATH "${PATH}:${Notes_ExecDirectory}:${Notes_ExecDirectory}/res/C"
ENV MAVEN_OPTS "-Djnx.noterm=true -Dmaven.repo.local=/root/.m2/repository"

# Configure the Maven environment and permissive root home directory
COPY docker/settings.xml /root/.m2/
RUN mkdir -p /root

# Bring in the Domino runtime
COPY --from=domino-docker:V1101_03212020prod /opt/hcl/domino/notes/11000100/linux /opt/hcl/domino/notes/latest/linux
RUN mkdir -p /local/notesdata
# TODO check if there's a way to do this in a single ADD
COPY --from=domino-docker:V1101_03212020prod /tmp/notesdata.tbz2 /local/notesdata/
RUN cd /local/notesdata && \
    tar xjf notesdata.tbz2 && \
    rm notesdata.tbz2

# Copy in our stock Notes ID and configuration files
COPY docker/notesdata/* /local/notesdata/
COPY docker/settings.xml /root/.m2/settings.xml
# Expand fakenames.nsf
RUN cd /local/notesdata && \
    unzip fakenames.nsf.zip && \
    rm fakenames.nsf.zip

# Prepare a permissive data environment
RUN chmod -R 777 /local/notesdata

# Bring in the dependencies to the local Maven repo
RUN mkdir /build/
COPY pom.xml /build/
COPY domino-jnx-api/pom.xml /tmpbuild/domino-jnx-api/
COPY domino-jnx-commons/pom.xml /tmpbuild/domino-jnx-commons/
COPY domino-jnx-console/pom.xml /tmpbuild/domino-jnx-console/
COPY domino-jnx-jna/pom.xml /tmpbuild/domino-jnx-jna/
COPY integration/domino-jnx-jakarta-security/pom.xml /tmpbuild/integration/domino-jnx-jakarta-security/
COPY integration/domino-jnx-jsonb/pom.xml /tmpbuild/integration/domino-jnx-jsonb/
COPY integration/domino-jnx-vertx-json/pom.xml /tmpbuild/integration/domino-jnx-vertx-json/
COPY integration/domino-jnx-lsxbeshim/pom.xml /tmpbuild/integration/domino-jnx-lsxbeshim/
COPY example/jnx-example-swt/pom.xml /tmpbuild/example/jnx-example-swt/
COPY example/jnx-example-webapp/pom.xml /tmpbuild/example/jnx-example-webapp/
COPY example/jnx-example-runjava/pom.xml /tmpbuild/example/jnx-example-runjava/
COPY example/jnx-example-domino-webapp-admin/pom.xml /tmpbuild/example/jnx-example-domino-webapp-admin/
COPY example/jnx-example-graalvm-native/pom.xml /tmpbuild/example/jnx-example-graalvm-native/
COPY test/it-domino-jnx/pom.xml /tmpbuild/test/it-domino-jnx/
RUN chmod -R 777 /build/
RUN mvn -f /build/pom.xml de.qaware.maven:go-offline-maven-plugin:1.2.5:resolve-dependencies

COPY . /build/
RUN chmod -R 777 /build/

RUN chmod -R 777 /root