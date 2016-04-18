package controllers

import play.api._
import play.api.mvc._
import play.twirl.api.Html
import play.api.Environment

class Application extends Controller {

    def index = Action {
        println("\n\t" +Environment.simple()+ "\n")
        Ok(views.html.layout.main("Your new application is ready.")(Html("")))
    }

}
