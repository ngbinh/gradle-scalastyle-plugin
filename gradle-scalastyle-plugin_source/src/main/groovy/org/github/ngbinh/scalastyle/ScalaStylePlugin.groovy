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

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author Binh Nguyen
 * @since 12/16/2014
 * @author Muhammad Ashraf
 * @since 5/11/13
 */
class ScalaStylePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.configurations.create("scalaStyle")
                .setVisible(false)
                .setTransitive(true)
                .setDescription('Scala Style libraries to be used for this project.')

        project.task(type: ScalaStyleTask, 'scalaStyle')
        project.tasks.scalaStyle.outputs.upToDateWhen { false }
    }
}
