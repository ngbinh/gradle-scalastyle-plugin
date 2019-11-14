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
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
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
    File buildDirectory = project.buildDir
    String configLocation
    String testConfigLocation
    @OutputFile
    String outputFile = buildDirectory.absolutePath + "/scala_style_result.xml"
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

    @InputFiles
    @SkipWhenEmpty
    List<File> getTestSourceFiles() {
        if (includeTestSourceDirectory || testConfigLocation != null) {
            if (testSource == null) {
                testSourceDir = project.fileTree(project.projectDir.absolutePath + "/src/test/scala")
            } else {
                testSourceDir = project.fileTree(project.projectDir.absolutePath + "/" + testSource)
            }
            testSourceDir.files.toList()
        } else {
            Collections.emptyList()
        }
    }

    @TaskAction
    def scalaStyle() {
        extractAndValidateProperties()
        if (!skip) {
            try {
                def startMs = System.currentTimeMillis()
                def configuration = ScalastyleConfiguration.readFromXml(configLocation)
                def filesToProcess = scalaStyleUtils.getFilesToProcess(source.files.toList(), testSourceFiles, inputEncoding, includeTestSourceDirectory)
                def messages = scalaStyleUtils.checkFiles(configuration, filesToProcess)

                if (testConfigLocation != null) {
                    def testConfiguration = ScalastyleConfiguration.readFromXml(testConfigLocation)
                    if (testConfiguration != null) {
                        def testFilesToProcess = scalaStyleUtils.getTestFilesToProcess(testSourceFiles, inputEncoding)
                        messages.addAll(scalaStyleUtils.checkFiles(testConfiguration, testFilesToProcess))
                    }
                }

                def config = scalaStyleUtils.configFactory()

                def outputResult = new TextOutput(config, verbose, quiet).output(messages)

                logger.debug("Saving to outputFile={}", project.file(outputFile).canonicalPath)
                XmlOutput.save(config, outputFile, outputEncoding, messages)

                def stopMs = System.currentTimeMillis()
                if (!quiet) {
                    logger.info("Processed {} file(s)", outputResult.files())
                    if (outputResult.warnings() > 0) {
                        logger.warn("Found {} warnings", outputResult.warnings())
                    } else {
                        logger.info("Found 0 warnings")
                    }
                    if (outputResult.errors() > 0) {
                        logger.error("Found {} errors", outputResult.errors())
                    } else {
                        logger.info("Found 0 errors")   
                    }
                    logger.info("Finished in {} ms", stopMs - startMs)
                }

                def violations = outputResult.errors() + ((failOnWarning) ? outputResult.warnings() : 0)

                processViolations(violations)
            } catch (Exception e) {
                throw new Exception("Scala check error", e)
            }
        } else {
            logger.info("Skipping Scalastyle")
        }
    }

    private void processViolations(int violations) {
        if (violations > 0) {
            if (failOnViolation) {
                throw new Exception("You have " + violations + " Scalastyle violation(s).")
            } else {
                logger.warn("Scalastyle:check violations detected but failOnViolation set to " + failOnViolation)
            }
        } else {
            logger.debug("Scalastyle:check no violations found")
        }
    }

    private void extractAndValidateProperties() {
        if (configLocation == null) {
            throw new Exception("No Scalastyle configuration file provided")
        }

        if (source == null) {
            throw new Exception("Specify Scala source set")
        }

        if (testConfigLocation != null && !new File(testConfigLocation).exists()) {
            throw new Exception("testConfigLocation " + testConfigLocation + " does not exist")
        }

        if (!new File(configLocation).exists()) {
            throw new Exception("configLocation " + configLocation + " does not exist")
        }

        if (!skip && !project.file(outputFile).exists()) {
            project.file(outputFile).parentFile.mkdirs()
            project.file(outputFile).createNewFile()
        }

        if (verbose) {
            logger.info("configLocation: {}", configLocation)
            logger.info("testConfigLocation: {}", testConfigLocation)
            logger.info("buildDirectory: {}", buildDirectory)
            logger.info("outputFile: {}", outputFile)
            logger.info("outputEncoding: {}", outputEncoding)
            logger.info("failOnViolation: {}", failOnViolation)
            logger.info("failOnWarning: {}", failOnWarning)
            logger.info("verbose: {}", verbose)
            logger.info("quiet: {}", quiet)
            logger.info("skip: {}", skip)
            logger.info("includeTestSourceDirectory: {}", includeTestSourceDirectory)
            logger.info("inputEncoding: {}", inputEncoding)
        }
    }
}
