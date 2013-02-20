package controllers

import play.api._
import play.api.mvc._
import play.api._
import io.Source
import play.templates.BaseScalaTemplate
import play.api.templates._
import qunit.QUnitTestsRunner
import play.api.Play.current
import qunit.QUnitTestsRunner._
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor
import java.io.{FileReader, StringReader, StringWriter}

abstract class QUnitBase extends Controller {

  val testTemplateNameToClassMap: Map[String, Template0[Result]]
  val MimeJavaScript = "text/javascript"

  def index(templateName: String, asset: String, testFile: String) = Action { implicit request =>
    if (play.api.Play.isProd) {
      NotFound
    } else if(templateName != "") {
      runTest(templateName)
    } else if (asset != "") {
      loadAsset(asset, request)
    } else if (testFile != "") {
      loadTestFile(testFile, request)
    } else {
      Ok(views.html.qunit.index(classUrlPathList, classNameList))
    }
  }


  def loadAsset(asset: String, request: Request[AnyContent]) = {
    controllers.Assets.at(path = "/public", file = asset)(request)
  }

  def loadTestFile(testFile: String, request: Request[AnyContent]) = {
    val necessaryToCompile = testFile.endsWith("precompiled.js")
    val filePathToLoad = if (necessaryToCompile) testFile.replace("precompiled.js", "coffee") else testFile
    val originalFile = Play.current.getFile("test/" + filePathToLoad)
    if (necessaryToCompile) {
      val input = new FileReader(originalFile)
      val writer = new StringWriter()
      (new RhinoCoffeeScriptProcessor).process(input, writer)
      Ok(writer.toString).as(MimeJavaScript)
    } else {
      Ok.sendFile(originalFile).as(MimeJavaScript)
    }
  }

  def runTest(templateName: String) = {
    testTemplateNameToClassMap.get(templateName) match {
      case Some(r: Template0[_]) => Ok(r.render().asInstanceOf[Html])
      case None => NotFound("No test found for " + templateName)
    }
  }
}