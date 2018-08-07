package org.github.ngbinh.scalastyle

import org.gradle.testkit.runner.BuildResult

import static org.gradle.testkit.runner.TaskOutcome.*

class ScalaStyleMultiSourceSpec extends ScalaStyleFunSpec {

    private String baseConfig = """
sourceSets {
    garbage {
        scala {
            compileClasspath += main.output
            runtimeClasspath += main.output
        }
    }
}

configurations {
    garbageCompile.extendsFrom compile
    garbageRuntime.extendsFrom compile
}

scalaStyle {
    // global config, used in case not overriden specifically
    config = file("\$rootDir/scalastyle.xml")
    verbose = false
}
"""

    def "should succeed on scalaStyleTestCheck"() {
        setup:
        prepareTest("multi", baseConfig + """
scalaStyle {
    sourceSets {
        test {
            // override output folder
            output = file("\$buildDir/scalastyle-test-result.xml")
        }
    }
}
""")

        when:
        BuildResult result = executeGradle('scalaStyleTestCheck')

        then:
        result.task(":scalaStyleTestCheck").outcome == SUCCESS
        result.output.contains """
Processed 1 file(s)
Found 0 warnings
Found 0 errors"""

        and: "report is formed in custom location"
        def report = new File(testProjectBuildDir, "scalastyle-test-result.xml").text
        report == """<?xml version="1.0" encoding="UTF-8"?>
<checkstyle version="5.0">
      
    </checkstyle>
"""
    }

    def "should succeed on scalaStyleMainCheck"() {
        setup:
        prepareTest("multi", baseConfig + """
scalaStyle {
    sourceSets {
        main {
            // no overrides
        }
    }
}
""")

        when:
        def result = executeGradle('scalaStyleMainCheck')

        then:
        result.task(':scalaStyleMainCheck').outcome == SUCCESS
        result.output.contains """
Processed 1 file(s)
Found 0 warnings
Found 0 errors"""
    }

    def "should fail on scalaStyleMainCheck with custom configuration"() {
        setup:
        prepareTest("multi", baseConfig + """
scalaStyle {
    sourceSets {
        main {
            // override config
            config = file("\$rootDir/scalastyle_main.xml")
        }
    }
}
""")

        when:
        def result = executeGradleAndFail('scalaStyleMain')

        then:
        result.task(':scalaStyleMainCheck').outcome == FAILED
        result.output.contains "Task :scalaStyleMainCheck FAILED"
        result.output.contains """Processed 1 file(s)
Found 0 warnings
Found 1 errors"""
        result.output.contains """src/main/scala/Person.scala message=@deprecated should be used instead of @java.lang.Deprecated line=17 column=0"""
    }

    def "should not have a task scalaStyleGarbageCheck"() {
        setup:
        prepareTest("multi", baseConfig + """
scalaStyle {
    sourceSets {
        garbage {
            // do not even create scalaStyleGarbageCheck task for this source set
            skip = true
        }
    }
}
""")

        when:
        def result = executeGradleAndFail('scalaStyleGarbageCheck')

        then:
        result.output.contains "Task 'scalaStyleGarbageCheck' not found in root project"
    }
}
