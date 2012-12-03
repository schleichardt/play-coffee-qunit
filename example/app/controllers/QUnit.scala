package controllers

import play.api._
import play.api.mvc._
import io.Source

object QUnit extends Controller {

  def index(htmlFile: String) = Action {
    if(htmlFile != "") {
      val source = scala.io.Source.fromFile(htmlFile, "utf-8")
      val htmlContent = source .mkString
      source.close()
      Ok(htmlContent).as("text/html")
    } else {
      Ok("needle")
    }
  }

}