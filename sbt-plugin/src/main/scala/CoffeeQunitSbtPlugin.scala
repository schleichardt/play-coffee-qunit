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
  def qUnitRunner = (testSrc: File, srcManaged: File) => {
    if(testSrc.exists) {
      val file = srcManaged / "QunitRunner.scala"
      IO.write(file,
        """package qunit
          | class QunitRunner extends qunit.QUnitTestsRunner""".stripMargin)
      Seq(file)
    } else {
      Seq[File]()
    }
  }

  def templatePathoToClassName(path: String): String = {
    val pathElements = path.split("/").toList
    val result = ("views.html." + pathElements.mkString(".")).replace(".scala.html", "")
    result
  }

  def testTemplatesIndex = (testSrc: File, srcManaged: File) => {
    import java.util.Collections
    val testFiles = if(testSrc.exists && testSrc.isDirectory) { FileUtils.listFiles(testSrc, Array("scala.html"), true) } else {Collections.emptyList()}
    val absPathLength: Int = (testSrc.absolutePath + "/views/").length
    val paths = testFiles.map(_.absolutePath.substring(absPathLength))


    val export = paths.map(path => templatePathoToClassName(path)).map(path => """  "%s" -> %s.asInstanceOf[Template0[Result]]""".format(path, path)).mkString(", ")

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

  def buildPipelineSettings(testScope: Configuration = Test, compileScope: Configuration = Compile): Seq[Project.Setting[_]] = Seq(
      sourceGenerators in testScope <+= (sourceDirectory in testScope, sourceManaged in testScope) map qUnitRunner
      , sourceGenerators in compileScope <+= (state, sourceDirectory in testScope, sourceManaged in compileScope, templatesTypes, templatesImport) map ScalaTemplates
      , sourceGenerators in compileScope <+= (sourceDirectory in testScope, sourceManaged in compileScope) map testTemplatesIndex
      , watchSources <++= (sourceDirectory in testScope) map { path => (path ** "*.coffee").get }
      , watchSources <++= (sourceDirectory in testScope) map { path => (path ** "*.js").get }
      , watchSources <++= (sourceDirectory in compileScope) map { path => (path ** "*.coffee").get }
      , watchSources <++= baseDirectory map { path => ((path / "public") ** "*.js").get }
      , watchSources <++= (sourceDirectory in testScope) map { path => (path ** "*.scala.html").get }
    )
}
