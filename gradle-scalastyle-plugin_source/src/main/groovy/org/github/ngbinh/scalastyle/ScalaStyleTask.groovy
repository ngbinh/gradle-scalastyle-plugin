/*
 *    Copyright 2014. Binh Nguyen
 *
 *    Copyright 2013. Muhammad Ashraf
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.github.ngbinh.scalastyle

import org.gradle.api.file.FileTree
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.scalastyle.ScalastyleConfiguration
import org.scalastyle.TextOutput
import org.scalastyle.XmlOutput
/**
 * @author Binh Nguyen
 * @since 12/16/2014
 * @author Muhammad Ashraf
 * @since 5/11/13
 */
class ScalaStyleTask extends SourceTask {
    File buildDirectory
    String configLocation
    String testConfigLocation
    String outputFile
    String outputEncoding = "UTF-8"
    Boolean failOnViolation = true
    Boolean failOnWarning = false
    Boolean skip = false
    Boolean verbose = false
    Boolean quiet = false
    Boolean includeTestSourceDirectory = true
    String inputEncoding = "UTF-8"
    ScalaStyleUtils scalaStyleUtils = new ScalaStyleUtils()
    String testSource
    FileTree testSourceDir

    ScalaStyleTask() {
        super()
        setDescription("Scalastyle examines your Scala code and indicates potential problems with it.")
    }

    @TaskAction
    def scalaStyle() {
        extractAndValidateProperties()
        if (!skip) {
            try {
                def startMs = System.currentTimeMillis()
                def configuration = ScalastyleConfiguration.readFromXml(configLocation)
                def fileToProcess = scalaStyleUtils.getFilesToProcess(source.getFiles().toList(), testSourceDir.getFiles().toList(), inputEncoding, includeTestSourceDirectory)
                def messages = scalaStyleUtils.checkFiles(configuration, fileToProcess)

                if (testConfigLocation != null) {
                  def testConfiguration = ScalastyleConfiguration.readFromXml(testConfigLocation)
                  if (testConfiguration != null) {
                      def testFilesToProcess = scalaStyleUtils.getTestFilesToProcess(testSourceDir.getFiles().toList(), inputEncoding)
                      messages.addAll(scalaStyleUtils.checkFiles(testConfiguration, testFilesToProcess))
                  }
                }

                def config = scalaStyleUtils.configFactory()

                def outputResult = new TextOutput(config, verbose, quiet).output(messages)

                project.getLogger().debug("Saving to outputFile={}", project.file(outputFile).getCanonicalPath());
                XmlOutput.save(config, outputFile, outputEncoding, messages)

                def stopMs = System.currentTimeMillis()
                if (!quiet) {
                    project.getLogger().info("Processed {} file(s)", outputResult.files())
                    project.getLogger().warn("Found {} warnings", outputResult.warnings())
                    project.getLogger().error("Found {} errors", outputResult.errors())
                    project.getLogger().info("Finished in {} ms", stopMs - startMs)
                }

                def violations = outputResult.errors() + ((failOnWarning) ? outputResult.warnings() : 0)

                processViolations(violations)
            } catch (Exception e) {
                throw new Exception("Scala check error", e)
            }
        } else {
            project.getLogger().info("Skipping Scalastyle")
        }
    }

    private void processViolations(int violations) {
        if (violations > 0) {
            if (failOnViolation) {
                throw new Exception("You have " + violations + " Scalastyle violation(s).")
            } else {
                project.getLogger().warn("Scalastyle:check violations detected but failOnViolation set to " + failOnViolation)
            }
        } else {
            project.getLogger().debug("Scalastyle:check no violations found")
        }
    }

    private void extractAndValidateProperties() {
        if (configLocation == null) {
            throw new Exception("No Scalastyle configuration file provided")
        }

        if (source == null) {
            throw new Exception("Specify Scala source set")
        }

        if (testSource == null) {
            testSourceDir = project.fileTree(project.projectDir.absolutePath + "/src/test/scala")
        } else {
            testSourceDir = project.fileTree(project.projectDir.absolutePath + "/" + testSource)
        }

        if (testConfigLocation != null && !new File(testConfigLocation).exists()) {
            throw new Exception("testConfigLocation " + testConfigLocation + " does not exist")
        }

        if (!new File(configLocation).exists()) {
            throw new Exception("configLocation " + configLocation + " does not exist")
        }

        if (buildDirectory == null) {
            buildDirectory = project.buildDir
        }

        if (outputFile == null) {
            outputFile = buildDirectory.absolutePath + "/scala_style_result.xml"
        }

        if (!skip && !project.file(outputFile).exists()) {
            project.file(outputFile).getParentFile().mkdirs()
            project.file(outputFile).createNewFile()
        }

        if (verbose) {
            project.getLogger().info("configLocation: {}", configLocation)
            project.getLogger().info("testConfigLocation: {}", testConfigLocation)
            project.getLogger().info("buildDirectory: {}", buildDirectory)
            project.getLogger().info("outputFile: {}", outputFile)
            project.getLogger().info("outputEncoding: {}", outputEncoding)
            project.getLogger().info("failOnViolation: {}", failOnViolation)
            project.getLogger().info("failOnWarning: {}", failOnWarning)
            project.getLogger().info("verbose: {}", verbose)
            project.getLogger().info("quiet: {}", quiet)
            project.getLogger().info("skip: {}", skip)
            project.getLogger().info("includeTestSourceDirectory: {}", includeTestSourceDirectory)
            project.getLogger().info("inputEncoding: {}", inputEncoding)
        }
    }
}
