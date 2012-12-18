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

  def index(templateName: String, asset: String) = Action { implicit request =>
    if (play.api.Play.isProd) {
      NotFound
    } else if(templateName != "") {
      runTest(templateName)
    } else if (asset != "") {
      loadAsset(asset, request)
    } else {
      Ok(views.html.qunit.index(classUrlPathList, classNameList))
    }
  }


  def loadAsset(asset: String, request: Request[AnyContent]) = {
    controllers.Assets.at(path = "/public", file = asset)(request)
  }

  def runTest(templateName: String) = {
    testTemplateNameToClassMap.get(templateName) match {
      case Some(r: Template0[_]) => Ok(r.render().asInstanceOf[Html])
      case None => NotFound("No test found for " + templateName)
    }
  }
}