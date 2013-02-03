# play-coffee-qunit
A plugin to write QUnit tests with CoffeeScript and run tests in browser or in console.
It is a spare time project. Use it a your own risk. Support can be dropped at any time and APIs may change.
For now it is for play 2.0.4.

## Features
* for play 2.0.4
* only one route to set
* can be used with CoffeeScript or JavaScript
* app test files only in test folder and not mixed in public or app folder
* use Scala template engine to write tests, so you can re use your tags
* runs in console and in browser


## Run tests
* `play test` to run QUnit tests in the console
* `play ~run` and open http://localhost:9000/@qunit in your browser

## Create tests
* put your CoffeeScript QUnit test in the test folder
* put a scala template in the folder test/views of your project, i.e. test/views/test/sub1/sub2/test1.scala.html and load the test script
* <pre>
```@qunit.test {
      @qunit.script("subfolderOfTest/mySuite.test.coffee") @* also works with JavaScript *@
      <div id="some-element">stuff in body</div>
}
```</pre>
* see the example app https://github.com/schleichardt/play-coffee-qunit/tree/master/demo

## Installation
I will use maven central for deployment, for now are only snapshots on sonatype available and you need to setup new resolvers.

* add to project/plugins.sbt 

<pre>resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("info.schleichardt" % "coffee-qunit-sbt-plugin" % "0.3-SNAPSHOT")</pre>


* set in project/Build.scala (don't miss the last line!)


<pre>val appDependencies = Seq(
         "info.schleichardt" %% "play-coffee-qunit" % "0.3-SNAPSHOT"
)

val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
         resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
).settings(info.schleichardt.playcoffeequnit.sbt.CoffeeQunitSbtPlugin.buildPipelineSettings(): _*)
</pre>

* add to conf/routes: `GET     /@qunit                     controllers.QUnit.index(templateName: String ?= "", asset: String ?= "", testFile: String ?= "")`


## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 schleichardt

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Inspirations
* https://github.com/playframework/play
    * Apache 2 license
    * testrunner code
* https://github.com/playframework/Play20
    * Apache 2 license, Copyright 2012 Typesafe (http://www.typesafe.com).
    * how to compile CoffeeScript files
* https://github.com/irregular-at/play-qunit only for play 1
    * Apache 2 license
    * idea to use template engine to write tests
    * use /@qunit as url
    * run differen QUnit tests in browser, layout browser runner
* https://github.com/gcusnieux/play20-qunit
    * how to visualize test results in console
