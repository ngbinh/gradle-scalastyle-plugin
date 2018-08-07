package org.github.ngbinh.scalastyle

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Shared

abstract class ScalaStyleFunSpec extends ScalaStyleSpec {

    File prepareTest(String projectDir, String scalaStyleOverrides = null) {
        createBuildFolder(projectDir)
        generateBuildGradleFile(scalaStyleOverrides)
    }

    GradleRunner createRunner(String task) {
        GradleRunner.create()
                .forwardOutput()
                .withProjectDir(testProjectDir.root)
                .withDebug(true)
                .withArguments('--stacktrace', '--info', task)
    }

    BuildResult executeGradle(String task) {
        createRunner(task).build()
    }

    BuildResult executeGradleAndFail(String task) {
        createRunner(task).buildAndFail()
    }

    BuildResult executeGradle(String task, String gradleVersion) {
        createRunner(task)
                .withGradleVersion(gradleVersion)
                .build()
    }
}
