package com.typesafe.sbt.traceur

import com.typesafe.sbt.jse.{SbtJsEngine, SbtJsTask}
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.web._

import sbt.Keys._
import sbt._
import spray.json._

object Import {

  val traceur = TaskKey[Pipeline.Stage]("traceur", "Run Traceur compiler")

  object TraceurKeys {

    val inputDir = SettingKey[File]("traceur-input-dir", "The top level directory that contains the input file names")
    val outputDir = SettingKey[File]("traceur-output-dir", "Directory where to put the output file")
    val inputFileNames = SettingKey[Seq[String]]("traceur-input", "Files to compile. Should just be the 'root' modules, traceur will pull the rest. So for example if A.js requires B.js requires C.js, only list A.js here. Default main.js")
    val outputFileName = SettingKey[String]("traceur-output", "Name of the output file. Default main.js")
    val experimental = SettingKey[Boolean]("traceur-experimental", "Turns on all experimental features. Default false")
    val sourceMaps = SettingKey[Boolean]("traceur-source-maps", "Enable source maps generation")
    val includeRuntime = SettingKey[Boolean]("traceur-include-runtime", "If traceur-runtime.js code should be included in the output file. Default true")
    val extraOptions = SettingKey[Seq[String]]("traceur-extra-options", "Extra options to pass to traceur command line")
  }

}

object SbtTraceur extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import Import._
  import Import.TraceurKeys._
  import com.typesafe.sbt.jse.SbtJsTask.autoImport.JsTaskKeys._
  import com.typesafe.sbt.web.Import.WebKeys._
  import com.typesafe.sbt.web.SbtWeb.autoImport._
  import SbtJsEngine.autoImport.JsEngineKeys._

  override def projectSettings = Seq(
    inputDir := (resourceManaged in traceur).value / "app",
    outputDir := (resourceManaged in traceur).value / "build",
    includeFilter := GlobFilter("*.js"),
    inputFileNames := Seq("main.js"),
    outputFileName := "main.js",
    experimental := false,
    sourceMaps := true,
    includeRuntime := true,
    extraOptions := Seq(),
    traceur := runTraceur.dependsOn(webJarsNodeModules in Plugin).value
  )

  def boolToParam(condition:Boolean, param:String): Seq[String] = {
    if (condition) Seq(param) else Seq()
  }

  private def runTraceur: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>

      val include = includeFilter.value
      val compilerMappings = mappings.filter(f => !f._1.isDirectory && include.accept(f._1))
      SbtWeb.syncMappings(
        streams.value.cacheDirectory,
        compilerMappings,
        inputDir.value
      )

      val commandlineParameters = (
        boolToParam(experimental.value, "--experimental")
        ++ boolToParam(sourceMaps.value, "--source-maps=file")
        ++ extraOptions.value
        ++ Seq("--out", (outputDir.value / outputFileName.value).toString)
        ++ boolToParam(includeRuntime.value,
          ((webJarsNodeModulesDirectory in Plugin).value / "traceur" / "bin" / "traceur-runtime.js").toString
        )
        ++ (inputFileNames.value.map(file => (inputDir.value / file).toString))
      )

      val cacheDirectory = streams.value.cacheDirectory / traceur.key.label
      val runCompile = FileFunction.cached(cacheDirectory, FilesInfo.hash) {
        _ =>
          streams.value.log.info("Compiling with Traceur")

          SbtJsTask.executeJs(
            state.value,
            // For now traceur only works with node
            EngineType.Node,
            None,
            Nil,
            (webJarsNodeModulesDirectory in Plugin).value / "traceur" / "src" / "node" / "command.js",
            commandlineParameters,
            (timeoutPerSource in traceur).value * compilerMappings.size
          )

          outputDir.value.***.get.toSet
      }

      val optimizedMappings = runCompile(inputDir.value.***.get.toSet).filter(_.isFile).pair(relativeTo(outputDir.value))
      (mappings.toSet -- compilerMappings.toSet ++ compilerMappings).toSeq
  }
}