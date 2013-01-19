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
    val file = Play.current.getFile("test/" + testFile)
    val isCoffeeScript = testFile.endsWith("coffee")
    if (isCoffeeScript) {
      Ok.sendFile(file).as("text/coffeescript")
    } else {
      Ok.sendFile(file).as("text/javascript")
    }
  }

  def runTest(templateName: String) = {
    testTemplateNameToClassMap.get(templateName) match {
      case Some(r: Template0[_]) => Ok(r.render().asInstanceOf[Html])
      case None => NotFound("No test found for " + templateName)
    }
  }
}