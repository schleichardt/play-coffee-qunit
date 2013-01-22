$ ->
  title = window.top.document.title
  browser = ""

  # extract the browser
  browser = /browser=([^&]+)/.exec(document.location.search)[1]  if /browser=/.exec(document.location.search)

  # ajax status
  $("#loading").ajaxStart(->
    $(this).show()
  ).ajaxStop ->
    $(this).hide()


  # get the url of the base qunit
  baseURL = ->
    url = "http://" + document.location.host
    url += ":" + document.location.port  if document.location.port and url.indexOf(":") is -1
    url += document.location.pathname
    url

  run = ->
    updateSelected()
    $(document.body).addClass "running"
    $(".test, #header").removeClass("passed").removeClass "failed"
    runNextTest()


  # run a single QUnit test file
  runTest = (testId, test) ->
    test.addClass "passing"
    $(".touch", test).html "&nbsp;"
    $("#qunit,#qunit-mask").show()
    $("#qunit-runner").attr "src", baseURL() + "?templateName=" + testId.replace(/-/g, ".") #TODO not nice


  # runs the next test
  runNextTest = ->
    if $(document.body).is(".running")
      test = $(".test.selected:not(.passed,.failed):first")
      if test.size()
        testId = test.attr("id")
        runTest testId, test
      else
        result()


  # show the test result (= stop)
  result = ->
    $("#qunit-mask, #qunit").hide()
    $("#qunit-runner").attr "src", "about:blank"
    $(document.body).removeClass "running"
    $(".passing").removeClass "passing"
    if $(".test.failed").size()
      $("#header").addClass "failed"
    else
      $("#header").addClass "passed"
    areFailedTests = $(".test.failed").size()
    if areFailedTests
      skip = $("#header").outerHeight()
      skip += $("#results").outerHeight()
      $.scrollTo $(".test.failed").offset().top - skip, 500


  ###
  Should be called when a test has successfully finished.
  @param test The jQuery object of the failed test
  @param result The html of the result to display
  ###
  testSuccess = (test, result) ->
    test.removeClass("passing").addClass "passed"
    $(".touch", test).html "+"
    $(".testResult", test).html result
    window.top.document.title = title
    runNextTest()


  ###
  Should be called when a test is failed.
  @param test The jQuery object of the failed test
  @param result The html of the result to display
  ###
  testFail = (test, result) ->
    test.removeClass("passing").addClass "failed"
    $(".touch", test).html "-"
    $(".testResult", test).html(result).show()
    window.top.document.title = title
    runNextTest()


  ###
  Posts back the result, so that the server writes an xunit file
  ###
  writeXUnit = (testId, result, callback) ->
    result.browser = browser
    result.test = testId
    $.each result.tests, (test) ->
      @actual = @actual.toString()  if typeof @actual is "object"
      @expected = @expected.toString()  if typeof @expected is "object"

    $.ajax
      url: baseURL() + "?result"
      type: "POST"
      data:
        result: JSON.stringify(result)

      dataType: "json"
      error: ->
        console.log "error"  if typeof console isnt "undefined"
        callback arguments

      success: ->
        callback arguments



  ###
  Callback function that is called, when a QUnit test is finished in the runner
  @param result The test result of the test.
  ###
  window.testFinished = (result) ->
    test = $(".test.selected:not(.passed,.failed):first")

    #If no test was run, display an error
    if result.summary.total is 0
      result.summary.failed = 1
      result.summary.total = 1
      result.tests = [
        name: "No tests available"
        result: false
        runtime: 0
        actual: "0"
        expected: ">=1"
        message: "No tests have been executed"
        source: ""
      ]
    $("#qunit,#qunit-mask").hide()
    html = ((if (result.summary.failed is 0) then "No" else ("<strong>" + result.summary.failed + "</strong>"))) + " test(s) failed of total " + result.summary.total + " tests" + " Runtime: " + result.summary.runtime + " ms" + "<table><tbody>"
    $.each result.tests, (index, test) ->
      html += "<tr>" + "<td width=\"20%\" class=\"" + ((if (test.result) then "passed" else "failed")) + "\" valign=\"top\">" + "<span>" + test.name + "</span>" + "</td>" + "<td valign=\"top\">"
      if test.result is false
        html += "<strong class=\"error\">" + test.message + "</strong>" + "<table class=\"failure\"><tbody>" + "<tr><td class=\"description\">Expected:</td><td>" + test.expected + "</td></tr>" + "<tr class=\"error\"><td class=\"description\">Actual:</td><td>" + test.actual + "</td></tr>" + "<tr><td class=\"description\">Source:</td><td>" + test.source + "</td></tr>" + "</tbody></table>"
      else
        html += "<strong class=\"success\">Ok</strong>"
      html += "</td></tr>"

    html += "</tbody></table>"
    writeXUnit test.attr("id"), result, ->

      # after xunit write had success or error, we launch the next test
      if result.summary.failed isnt 0
        testFail test, html
      else
        testSuccess test, html



  # stops the tests
  stop = ->
    result()


  # bookmark url for the selected tests
  bookmark = ->
    url = baseURL()
    url += "?select="
    v = false
    $(".test.selected").each ->
      url += ","  if v
      url += $(this).attr("id")
      v = true

    url  if url


  # update the view of selected tests
  updateSelected = ->
    nb = $(".test.selected").size()
    if nb
      $(".nbToRun").text nb
      $(".nbToRunPluralize").text (if (nb > 1) then "s" else "")
      $("#start").removeAttr("disabled").removeClass "disabled"
      $("#bms").show()
      $("#bms a").attr "href", bookmark()
      $("#quickLinks").hide()
    else
      $(".nbToRun").text "no"
      $(".nbToRunPluralize").text ""
      $("#start").attr("disabled", "true").addClass "disabled"
      $("#bms").hide()
      $("#quickLinks").show()
    $(".test, #header").removeClass("passed").removeClass "failed"
    $(".touch").html "&sim;"
    $(".testResult").hide()


  # click on a test
  $(".test a").click (e) ->
    e.preventDefault()
    return  if $(document.body).is(".running")
    $(this).closest(".test").toggleClass "selected"
    updateSelected()


  # Toggle +/- click
  $(".test .touch").click (e) ->
    e.preventDefault()
    test = $(this).closest(".test")
    if $(test).is(".failed,.passed")
      $(".testResult", test).toggle()
      $(this).html (if $(this).html() is "-" then "+" else "-")


  # Update selected when clicking heading
  $("#tests h2 span").click ->
    return  if $(document.body).is(".running")
    ul = $(this).parent().next("ul")
    if $(".test", ul).size() is $(".test.selected", ul).size()
      $(".test", ul).removeClass "selected"
    else
      $(".test", ul).addClass "selected"
    updateSelected()


  # start button
  $("#start").click ->
    return  if $(this).is(".disabled")
    run()


  # stop button
  $("#stop").click ->
    stop()


  # stop button in the overlay
  $("#stopQUnit").click ->
    stop()


  # select all link
  $("#sa").click ->
    $(".test").addClass "selected"
    updateSelected()


  # unselect all link
  $("#unselectAll").click (e) ->
    e.preventDefault()
    $(".test").removeClass "selected"
    updateSelected()


  # run all link
  $("#ra").click ->
    $(".test").addClass "selected"
    updateSelected()
    run()


  # select the bookmarked tests
  if /select=/.exec(document.location.search)
    toSelect = /select=([^&]+)/.exec(document.location.search)[1].split(",")
    if toSelect[0] is "all"
      $(".test").addClass "selected"
    else
      $(toSelect).each ->
        $(document.getElementById(this)).addClass "selected"


  # automatically run the tests
  run()  if /auto=yes/.exec(document.location.search)
  updateSelected()
