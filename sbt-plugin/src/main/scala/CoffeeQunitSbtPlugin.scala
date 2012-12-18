package info.schleichardt.playcoffeequnit.sbt

import sbt._
import sbt.Keys._
import sbt.PlayExceptions.CompilationException
import sbt.PlayProject._
import scala.{Array, Some}
import org.apache.commons.io.FileUtils
import scala.collection.JavaConversions._


object CoffeeQunitSbtPlugin extends Plugin
{
  val coffeescriptEntryPointsForTests = SettingKey[PathFinder]("play-coffeescript-entry-points-test")

  /*
   this is mostly copied from
   https://github.com/playframework/Play20/blob/2.0.x/framework/src/sbt-plugin/src/main/scala/PlayCommands.scala#L271
   to let test sources in folder test, I needed {@code sourceDirectory in Test} instead of {@code sourceDirectory in Compile}.
   In addition coffee files does not necessarily to be in test/assets.
  */
  def TestAssetsCompiler(name: String,
                         watch: File => PathFinder,
                         filesSetting: sbt.SettingKey[PathFinder],
                         naming: (String, Boolean) => String,
                         compile: (File, Seq[String]) => (String, Option[String], Seq[File]),
                         optionsSettings: sbt.SettingKey[Seq[String]]) =
    (sourceDirectory in Test, resourceManaged in Compile /* in Test wouldn't work, no mapping*/, cacheDirectory, optionsSettings, filesSetting) map {
      (src, resources, cache, options, files) =>
        import java.io._
        val cacheFile = cache / name
        val currentInfos = watch(src).get.map(f => f -> FileInfo.lastModified(f)).toMap
        val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)
        val outOfDate = previousInfo != currentInfos
        if (outOfDate) {
          previousRelation._2s.foreach(IO.delete)// Delete previous generated files
          val generated = (files x relativeTo(Seq(src))).flatMap {
            case (sourceFile, name) => {
              val (debug, min, dependencies) = compile(sourceFile, options)
              val targetFolder = "public/"
              val out = new File(resources, targetFolder + naming(name, false))
              val outMin = new File(resources, targetFolder + naming(name, true))
              IO.write(out, debug)
              dependencies.map(_ -> out) ++ min.map {
                minified =>
                  IO.write(outMin, minified)
                  dependencies.map(_ -> outMin)
              }.getOrElse(Nil)
            }
          }
          Sync.writeInfo(cacheFile, Relation.empty[File, File] ++ generated, currentInfos)(FileInfo.lastModified.format)
          generated.map(_._2).distinct.toList// Return new files
        } else {
          previousRelation._2s.toSeq// Return previously generated files
        }
    }


  val CoffeescriptCompilerForTests = {
    val compilerName = "coffeescript-test"
    val watch: (sbt.File) => PathFinder = _ ** "*.coffee"
    val naming: (String, Boolean) => String = (name, min) => name.replace(".coffee", if (min) ".min.js" else ".js")
    val compileAsset: (File, Seq[String]) => (String, Option[String], Seq[File]) = {
      (coffeeFile, options) =>
        import scala.util.control.Exception._
        val jsSource = play.core.coffeescript.CoffeescriptCompiler.compile(coffeeFile, options)
        val minified = catching(classOf[CompilationException])
          .opt(play.core.jscompile.JavascriptCompiler.minify(jsSource, Some(coffeeFile.getName())))
        (jsSource, minified, Seq(coffeeFile))
    }

    TestAssetsCompiler(compilerName, watch, coffeescriptEntryPointsForTests, naming, compileAsset, coffeescriptOptions)
  }

  val deleteCoffeeTestAssets = TaskKey[Unit]("play-coffee-qunit-delete-generated-js")
  val deleteCoffeeTestAssetsTask = (managedResources in Compile, streams) map { (res, s) =>
    println(res.foreach{ file =>
      print(file.name + " ")
      if (file.name.endsWith(".test.js") || file.name.endsWith(".test.min.js")) {
        IO.delete(file)
      }
    })
  }

  //TODO generalize for multiple test scopes
  val qUnitRunner = sourceManaged in Test map {
    dir =>
      val file = dir / "QunitRunner.scala"
      IO.write(file,
        """package qunit
          | class QunitRunner extends qunit.QUnitTestsRunner""".stripMargin)
      Seq(file)
  }

  def testTemplatesIndex = (sourceDirectory: File, srcManaged: File) => {
    val testFiles = FileUtils.listFiles(sourceDirectory, Array("scala.html"), true)
    val absPathLength: Int = (sourceDirectory.absolutePath + "/views/").length
    val paths = testFiles.map(_.absolutePath.substring(absPathLength))
    def toClassName(path: String): String = {
      val pathElements = path.split("/").toList
      "views.html" + pathElements.init.mkString(".", ".", ".") + pathElements.last.replace(".scala.html", "")
    }

    val export = paths.map(path => toClassName(path)).map(path => """  "%s" -> %s.asInstanceOf[Template0[Result]]""".format(path, path)).mkString(", ")

    val file = srcManaged / "controllers" / "QUnit.scala"
    IO.write(file,
      """package controllers
        |
        |import play.api.mvc._
        |import controllers._
        |import play.api.templates._
        |
        |object QUnit extends QUnitBase {
        |   val testTemplateNameToClassMap: Map[String, Template0[Result]] = Map(
        |      %s
        |   )
        |}
      """.format(export).stripMargin)
    Seq(file)
  }


  //TODO is it possible to apply settings only by specific command line arguments or scopes to disable test stuff in staging?
  override lazy val settings: Seq[sbt.Project.Setting[_]] = Seq(
      deleteCoffeeTestAssets <<= deleteCoffeeTestAssetsTask,
      playStage <<= playStage.dependsOn(deleteCoffeeTestAssets),
      coffeescriptEntryPointsForTests <<= (sourceDirectory in Test)(testDir => testDir ** "*.coffee"),
      resourceGenerators in Compile <+= CoffeescriptCompilerForTests,
      sourceGenerators in Test <+= qUnitRunner,
      sourceGenerators in Compile <+= (sourceDirectory in Test, sourceManaged in Compile, templatesTypes, templatesImport) map ScalaTemplates,
      sourceGenerators in Compile <+= (sourceDirectory in Test, sourceManaged in Compile) map testTemplatesIndex,
      resolvers += "schleichardts Github" at "http://schleichardt.github.com/jvmrepo/",
      libraryDependencies +=  "info.schleichardt" %% "play-coffee-qunit" % "0.3-SNAPSHOT"
    )
}
