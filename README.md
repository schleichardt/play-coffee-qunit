# play-coffee-qunit
An experimental plugin to write QUnit tests with CoffeeScript and run tests in browser or in console.
It is a spare time project. Use it a your own risk. Support can be dropped at any time and APIs may change.
For now it is for play 2.0.4.

## Features
* for play 2.1
* can be used with CoffeeScript or JavaScript
* test files reside only in the "test" folder and are not bundled with tha application
* use Scala template engine to write tests, so you can re use your tags
* runs in console and in browser


## Run tests
* `play test` to run QUnit tests in the console
* `play ~run` and open http://localhost:9000/@qunit in your browser

## Create tests
* put your CoffeeScript QUnit test in the test folder
* put a scala template in the folder test/views of your project, i.e. test/views/test/sub1/sub2/test1.scala.html and load the test script
* <pre>
```@qunitTest {
      @qunit.script("subfolderOfTest/mySuite.test.coffee") @* also works with JavaScript *@
      <div id="some-element">stuff in body</div>
}
```</pre>
* see the example app https://github.com/schleichardt/play-coffee-qunit/tree/master/demo

## Installation
I will use maven central for deployment, for now are only snapshots on sonatype available and you need to setup new resolvers.

* add to project/plugins.sbt 

<pre>resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("info.schleichardt" % "coffee-qunit-sbt-plugin" % "0.6-SNAPSHOT")</pre>


* set in project/Build.scala (don't miss the last line!)


<pre>val appDependencies = Seq(
         "info.schleichardt" %% "play-coffee-qunit" % "0.6-SNAPSHOT"
)

val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
         resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
).settings(info.schleichardt.playcoffeequnit.sbt.CoffeeQunitSbtPlugin.buildPipelineSettings(): _*)
</pre>

* add to conf/routes: `->     /@qunit qunit.Routes`


## Typical traps

### Function from CoffeeScript is not available in tests
CoffeeScript files are compiled to JavaScript code in an anonymous function wrapper by default.
Connect the functions to the window object or deactivate the wrapper in your Build.scala with `coffeescriptOptions := Seq("bare")`.
For more information see http://www.playframework.org/documentation/2.0.4/AssetsCoffeeScript .

### Scala templates in test folder not compiled in browser
Changes on the Scala templates will only affect the next start if you use the tests in the browser. Test files and assets should be hot deployed.

### No JUnit XML reports generated
Add to your Build.scala `testOptions in Test += Tests.Argument("junitxml", "console")`.

## Licence

This software is licensed under the Apache 2 license, quoted below.

Copyright 2012 schleichardt

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

## Inspirations
* https://github.com/playframework/play
* https://github.com/playframework/Play20
* https://github.com/irregular-at/play-qunit
* https://github.com/gcusnieux/play20-qunit
* https://github.com/jameslowry/play2-qunit
