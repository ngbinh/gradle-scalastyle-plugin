package org.github.ngbinh.scalastyle

import org.gradle.testkit.runner.BuildResult

class ScalaStyleCheckFailsOnNoConfigSpec extends ScalaStyleFunSpec {

    def "should run scalaStyleCheck and fail if no scalastyle.xml config provided"() {
        setup:
        prepareTest("noscalastyle")

        when:
        BuildResult result = executeGradleAndFail('scalaStyleCheck')

        then:
        result.output.contains "scalastyle.xml file does not exist"
    }
}
