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


  override lazy val settings: Seq[sbt.Project.Setting[_]] = Seq(
      sourceGenerators in Test <+= qUnitRunner,
      sourceGenerators in Compile <+= (sourceDirectory in Test, sourceManaged in Compile, templatesTypes, templatesImport) map ScalaTemplates,
      sourceGenerators in Compile <+= (sourceDirectory in Test, sourceManaged in Compile) map testTemplatesIndex,
    )
}
