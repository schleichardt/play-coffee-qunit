package controllers

import play.api._
import play.api.mvc._
import play.api._
import io.Source
import play.templates.BaseScalaTemplate
import play.api.templates._

abstract class QUnitBase extends Controller {

  val testTemplateNameToClassMap: Map[String, Template0[Result]]

  def index(templateName: String, asset: String) = Action { implicit request =>
    if(templateName != "") {
      testTemplateNameToClassMap.get(templateName) match {
        case Some(r: Template0[_]) => NotFound(r.render().asInstanceOf[Html]) //TODO remove cast
        case None => NotFound("No test found for " + templateName)
      }
    } else if (asset != "") {
      controllers.Assets.at(path="/public", file=asset)(request)
    } else {
      Ok("needle")
    }
  }
}