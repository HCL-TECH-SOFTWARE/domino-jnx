/*
 * ==========================================================================
 * Copyright (C) 2024 HCL America, Inc. ( http://www.hcl.com/ )
 *                            All rights reserved.
 * ==========================================================================
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may
 * not use this file except in compliance with the License.  You may obtain a
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.
 *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the  specific language  governing permissions  and limitations
 * under the License.
 * ==========================================================================
 */
package com.hcl.maven;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class PomManipulator {

    public static final String ORIGINAL_ARTIFACT_ID = "original.artifact.id";

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out
                    .println("Usage: java -jar multi-version-tool.jar <startDir> <version suffix>");
            System.exit(1);
        }

        final String startDir = args[0];
        final String versionSuffix = args[1].toLowerCase();

        boolean testflight = args.length > 2;

        final PomManipulator pomManipulator =
                new PomManipulator(startDir, versionSuffix, testflight);
        boolean result = pomManipulator.manipulatePoms();
        System.exit(result ? 0 : 1);
    }

    private final String startDir;
    private final String versionSuffix;
    private final boolean testflight;
    private final Map<String, DependencyInfo> dependencyInfoMap = new HashMap<>();
    private final Map<Path, Model> modelMap = new HashMap<>();
    private final Map<String, String> moduleMap = new HashMap<>();
    private final Set<String> groupIds = new HashSet<>();

    public PomManipulator(String startDir, String versionSuffix, boolean testflight) {
        this.startDir = startDir;
        this.versionSuffix = versionSuffix;
        this.testflight = testflight;
    }

    public boolean manipulatePoms() {

        try (Stream<Path> allFiles = Files.walk(Paths.get(this.startDir))) {
            // Load all POMs and update artefactIds
            allFiles.filter(p -> p.toString().endsWith("pom.xml"))
                    .forEach(this::loadOnePom);
            // Update dependencies with updated artefactIds
            modelMap.values().stream()
                    .forEach(this::updateDependencies);
            // Save the updated POMs
            if (this.testflight) {
                modelMap.keySet()
                        .forEach(key -> System.out.printf("TestFlight, saving pom %s%n", key));
                moduleMap.forEach(
                        (k, v) -> System.out.printf("Testflight, rename dir %s to %s%n", k, v));
            } else {
                modelMap.entrySet().forEach(this::saveOnePom);
                moduleMap.entrySet().forEach(this::renameModuleDir);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Filter out example and test modules
        Path pomFile = Paths.get(this.startDir, "pom.xml");
        try (FileReader reader = new FileReader(pomFile.toFile())) {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);
            model.setModules(model.getModules().stream()
                    .filter(module -> !module.startsWith("example") && !module.startsWith("test"))
                    .collect(Collectors.toList()));
                    if (!this.testflight) {
                        this.saveOnePom(Map.entry(pomFile, model));
                    } else {
                        System.out.printf("TestFlight, reduced modules %s%n", + model.getModules());
                    }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    void renameModuleDir(Map.Entry<String, String> entry) {
        String sourceName = this.startDir + File.separator + entry.getKey();
        String targetName = this.startDir + File.separator + entry.getValue();
        System.out.printf("Rename module %s to %s - ", sourceName, targetName);

        File source = new File(sourceName);
        File target = new File(targetName);

        boolean result = source.renameTo(target);
        System.out.println(result ? "success" : "failure");
    }

    void saveOnePom(Map.Entry<Path, Model> entry) {
        Path pomFile = entry.getKey();
        Model model = entry.getValue();
        try (FileWriter writer = new FileWriter(pomFile.toFile())) {
            MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
            xpp3Writer.write(writer, model);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void updateDependencies(Model model) {
        // Fix the project parent
        Parent parent = model.getParent();
        if (parent != null) {
            this.fixParent(parent);
        }

        // Fix the dependencies
        model.getDependencies().stream()
                .forEach(this::updateDependency);

        // fix dependencies in profiles
        model.getProfiles().stream()
                .flatMap(p -> p.getDependencies().stream())
                .forEach(this::updateDependency);

        // Fix module names
        model.setModules(model.getModules().stream()
                .map(this::updateModuleName)
                .collect(Collectors.toList()));

        // fix module names in profiles
        model.getProfiles().stream()
                .forEach(profile -> profile.setModules(profile.getModules().stream()
                        .map(this::updateModuleName)
                        .collect(Collectors.toList())));
    }

    String updateModuleName(String moduleName) {
        String newName = moduleName + "-" + this.versionSuffix;
        System.out.printf("Rename module %s to %s%n", moduleName, newName);
        this.moduleMap.put(moduleName, newName);
        return newName;
    }

    void fixParent(Parent parent) {
        String groupId = parent.getGroupId();
        String keyPrefix = "${project.groupId}".equals(groupId)
                ? this.groupIds.iterator().next()
                : groupId;
        String key = keyPrefix + ":" + parent.getArtifactId();
        DependencyInfo dependencyInfo = this.dependencyInfoMap.get(key);
        if (dependencyInfo != null) {
            System.out.println("Updating parent " + dependencyInfo.toString());
            parent.setArtifactId(dependencyInfo.updatedArtifactId);
        }
    }

    void updateDependency(Dependency dependency) {
        String groupId = dependency.getGroupId();
        String keyPrefix = "${project.groupId}".equals(groupId)
                ? this.groupIds.iterator().next()
                : groupId;
        String key = keyPrefix + ":" + dependency.getArtifactId();
        DependencyInfo dependencyInfo = this.dependencyInfoMap.get(key);
        if (dependencyInfo != null) {
            System.out.println("Updating dependency " + dependencyInfo.toString());
            dependencyInfo.updateDependency(dependency);
        }
    }

    void loadOnePom(Path pomFile) {
        System.out.println("Loading " + pomFile.toString());
        try (FileReader reader = new FileReader(pomFile.toFile())) {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);
            this.modelMap.put(pomFile, model);
            if (model.getParent() != null) {
                String groupId = model.getParent().getGroupId();
                this.groupIds.add(groupId);
            }
            // Get the property original.asset.name
            String originalArtifactId = model.getProperties().getProperty(ORIGINAL_ARTIFACT_ID);

            // We only update if we have the original.asset.name property
            if (originalArtifactId == null) {
                return;
            }

            String newArtifactId = originalArtifactId + "-" + this.versionSuffix;
            model.setArtifactId(newArtifactId);
            System.out.println(
                    "Updated artifactId from " + originalArtifactId + " to " + newArtifactId);
            String groupId = model.getGroupId() == null
                    ? model.getParent().getGroupId()
                    : model.getGroupId();
            DependencyInfo dependencyInfo =
                    new DependencyInfo(groupId, originalArtifactId, newArtifactId);
            this.dependencyInfoMap.put(dependencyInfo.getKey(), dependencyInfo);

            this.modelMap.put(pomFile, model);
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update POM with properties and profiles
     *
     * @param pomFile Path to file, only used to hammer versions into shape
     */
    void updateOnePom(Path pomFile) {
        System.out.println("Processing " + pomFile.toString());
        try (FileReader reader = new FileReader(pomFile.toFile())) {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            Model model = xpp3Reader.read(reader);

            String originalArtifactId = model.getArtifactId();
            model.getProperties().setProperty(ORIGINAL_ARTIFACT_ID, originalArtifactId);
            addJarNameToProperties(model.getProperties(), "R12");
            model.getBuild().setFinalName("${jar.finalName}");

            Map<String, Boolean> found = new HashMap<>();
            found.put("R12", false);
            found.put("R14", false);
            found.put("R145", false);

            List<Profile> profiles = model.getProfiles();
            profiles.stream()
                    .filter(profile -> found.containsKey(profile.getId()))
                    .forEach(profile -> {
                        found.put(profile.getId(), true);
                        addJarNameToProperties(profile.getProperties(), profile.getId());
                    });

            found.entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .forEach(entry -> {
                        Profile profile = new Profile();
                        profile.setId(entry.getKey());
                        addJarNameToProperties(profile.getProperties(), entry.getKey());
                        profiles.add(profile);
                    });

            model.setProfiles(profiles);

            try (FileWriter writer = new FileWriter(pomFile.toFile())) {
                MavenXpp3Writer xpp3Writer = new MavenXpp3Writer();
                xpp3Writer.write(writer, model);
            }

        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
    }

    void addJarNameToProperties(Properties properties, String id) {
        String suffix = "R12".equals(id) ? "" : "-" + id;
        String jarName = "${project.artifactId}" + suffix + "-${project.version}";
        properties.setProperty("jar.finalName", jarName);
    }


}
