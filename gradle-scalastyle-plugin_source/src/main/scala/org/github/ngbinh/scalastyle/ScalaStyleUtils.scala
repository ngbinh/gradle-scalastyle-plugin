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

import org.scalastyle.{Directory, FileSpec, Message}
import org.scalastyle.ScalastyleChecker
import org.scalastyle.ScalastyleConfiguration
import com.typesafe.config.ConfigFactory
import com.typesafe.config.Config
import java.io.File
import java.util.{List => jList}
import scala.collection.JavaConverters._

/**
  * @author Binh Nguyen
  * @since 12/16/2014
  * @author Muhammad Ashraf
  * @since 5/11/13
  */
class ScalaStyleUtils {
  def getFilesToProcess(sourceFiles: jList[File], inputEncoding: String): List[FileSpec] =
    getFiles("sourceDirectory", sourceFiles.asScala.toList, inputEncoding)

  private def getFiles(name: String, files: List[File], encoding: String): List[FileSpec] =
    Directory.getFiles(Option[String](encoding), files)

  def checkFiles(configuration: ScalastyleConfiguration, files: List[FileSpec]): jList[Message[FileSpec]] =
    new ScalastyleChecker(Some(this.getClass.getClassLoader)).checkFiles(configuration, files).toBuffer.asJava

  def readConfig(): Config = ConfigFactory.load(this.getClass.getClassLoader)
}
