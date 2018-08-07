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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceTask
import org.scalastyle.ScalastyleConfiguration
import org.scalastyle.TextOutput
import org.scalastyle.XmlOutput

/**
 * @author Binh Nguyen
 * @since 12/16/2014
 * @author Muhammad Ashraf
 * @since 5/11/13
 */
class ScalaStylePlugin implements Plugin<Project> {
    private Project project
    private ScalaStyle extension

    private def scalaStyleUtils = new ScalaStyleUtils()

    void apply(Project project) {
        this.project = project
        this.extension = new ScalaStyle(project.container(ScalaStyleSourceSet))

        project.extensions.add('scalaStyle', extension)
        project.configurations.create('scalaStyle')
                .setVisible(false)
                .setTransitive(true)
                .setDescription('Scalastyle libraries to be used for this project.')

        setupScalaStyle()
    }

    private def setupScalaStyle() {
        project.task('scalaStyleCheck') {
            group = 'verification'
            description = "Runs scalastyle checks."

            project.afterEvaluate {
                ScalastyleConfiguration scalaStyleConfig
                if (extension.config != null) {
                    logger.info("Loading scalastyle configuration: ${extension.config.absolutePath}")
                    scalaStyleConfig = loadScalaStyleConfig(extension.config)
                }

                def srcSets = project.sourceSets.findAll { !it.scala.srcDirs.isEmpty() }
                        .collectEntries { [it.name, it.scala.srcDirs] }

                def scalaStyleTasks = srcSets.findResults {
                    def srcSetName = it.key as String
                    def srcDirs = it.value as List<File>

                    createScalaStyleTask(srcSetName, srcDirs, scalaStyleConfig)
                }

                dependsOn scalaStyleTasks
            }
        }
    }

    private def createScalaStyleTask(String srcSetName, List<File> srcDirs, ScalastyleConfiguration scalaStyleConfig) {
        def overrides = extension.sourceSets.find { it.name == srcSetName } ?:
                new ScalaStyleSourceSet(srcSetName) // create dummy object to not fail with NPE

        def skip = overrides.skip != null ? overrides.skip : extension.skip
        if (!skip) {
            project.task(type: SourceTask, "scalaStyle${srcSetName.capitalize()}Check") {
                group = 'verification'
                description = "Runs scalastyle checks on source set ${srcSetName}."

                source = srcDirs

                def outputFile = overrides.output ?
                        project.file(overrides.output) :
                        project.file("${project.buildDir.absolutePath}/scalastyle/${srcSetName}/scalastyle-check.xml")
                outputs.files(outputFile)

                def usedScalaStyleConfig = scalaStyleConfig
                if (overrides.config != null) {
                    logger.info("Loading overriden scalastyle configuration for ${srcSetName} source set: ${overrides.config.absolutePath}")
                    usedScalaStyleConfig = loadScalaStyleConfig(overrides.config)
                }

                def options = extractOptions(overrides)

                doLast {
                    try {
                        def startMs = System.currentTimeMillis()

                        def messages = scalaStyleUtils.checkSources(usedScalaStyleConfig, srcDirs, options.encoding)

                        def config = scalaStyleUtils.readConfig()
                        def outputResult = new TextOutput(config, options.verbose, options.quiet).output(messages)

                        logger.debug("Saving to outputFile={}", outputFile.canonicalPath)

                        XmlOutput.save(config, outputFile.absolutePath, options.outputEncoding, messages)

                        def stopMs = System.currentTimeMillis()
                        if (!options.quiet) {
                            logger.lifecycle("Processed {} file(s)", outputResult.files())
                            logger.warn("Found {} warnings", outputResult.warnings())
                            logger.error("Found {} errors", outputResult.errors())
                            logger.lifecycle("Finished in {} ms", stopMs - startMs)
                        }

                        def violations = outputResult.errors() + ((options.failOnWarning) ? outputResult.warnings() : 0)
                        if (violations > 0) {
                            if (options.failOnViolation) {
                                throw new GradleException("You have $violations Scalastyle violation(s).")
                            } else {
                                logger.warn("Scalastyle:check violations detected but failOnViolation set to false")
                            }
                        } else {
                            logger.debug("Scalastyle:check no violations found")
                        }
                    } catch (Exception ex) {
                        throw new GradleException("Scalastyle check error", ex)
                    }
                }
            }
        } else {
            null
        }
    }

    private def extractOptions(ScalaStyleSourceSet overrides) {
        [
                encoding       : overrides.inputEncoding ?: extension.inputEncoding,
                outputEncoding : overrides.outputEncoding ?: extension.outputEncoding,
                verbose        : overrides.verbose != null ? overrides.verbose : extension.verbose,
                quiet          : overrides.quiet != null ? overrides.quiet : extension.quiet,
                failOnWarning  : overrides.failOnWarning != null ?
                        overrides.failOnWarning :
                        extension.failOnWarning,
                failOnViolation: overrides.failOnViolation != null ?
                        overrides.failOnViolation :
                        extension.failOnViolation
        ]
    }

    private def loadScalaStyleConfig(File config) {
        if (config == null) {
            throw new GradleException("No Scalastyle configuration file provided")
        }

        if (!config.exists()) {
            throw new GradleException("Scalastyle configuration $config file does not exist")
        }

        def scalaStyleConfig = ScalastyleConfiguration.readFromXml(config.absolutePath)
        if (!scalaStyleConfig) {
            throw new GradleException("Failed to read scalastyle configuration from ${config.absolutePath}")
        }

        scalaStyleConfig
    }
}

