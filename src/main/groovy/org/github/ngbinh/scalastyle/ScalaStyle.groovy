package org.github.ngbinh.scalastyle

import org.gradle.api.NamedDomainObjectContainer

abstract class BaseScalaStyle {
    Boolean skip = false
    File config
    String outputEncoding = "UTF-8"
    Boolean failOnViolation = true
    Boolean failOnWarning = false
    Boolean verbose = false
    Boolean quiet = false
    String inputEncoding = "UTF-8"
}

class ScalaStyleSourceSet extends BaseScalaStyle {
    String name
    File output

    ScalaStyleSourceSet(String name) {
        this.name = name
    }
}

class ScalaStyle extends BaseScalaStyle {

    NamedDomainObjectContainer<ScalaStyleSourceSet> sourceSets

    ScalaStyle(NamedDomainObjectContainer<ScalaStyleSourceSet> sourceSets) {
        this.sourceSets = sourceSets
    }

    def sourceSets(final Closure configureClosure) {
        sourceSets.configure(configureClosure)
    }
}
