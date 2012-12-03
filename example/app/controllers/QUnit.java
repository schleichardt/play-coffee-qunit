package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class QUnit extends Controller {
    public static Result index() {
        return ok("needle");
    }
}