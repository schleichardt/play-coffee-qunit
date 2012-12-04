package info.schleichardt.playcoffeequnit

import sbt._
import sbt.Keys._
import sbt.PlayExceptions.CompilationException
import sbt.PlayProject._
import scala.Some

//http://harrah.github.com/xsbt/latest/api/index.html#sbt.Plugin
object CoffeeQunitSbtPlugin extends Plugin
{
  val coffeescriptEntryPointsForTests = SettingKey[PathFinder]("play-coffeescript-entry-points-test")

  //TODO remove in stage, remove stuff out of main (resourceManaged in Compile)
  //http://stackoverflow.com/questions/11845176/how-to-make-a-sbt-task-use-a-specific-configuration-scope

  /* this is mostly copied from
 https://github.com/playframework/Play20/blob/2.0.x/framework/src/sbt-plugin/src/main/scala/PlayCommands.scala#L271
 to let test sources in folder test, I needed {@code sourceDirectory in Test} instead of {@code sourceDirectory in Compile}.
 In addition coffee files does not necessarily to be in test/assets.

 TODO: Maybe this can be outsourced into a module.
  */
  def TestAssetsCompiler(name: String,
                         watch: File => PathFinder,
                         filesSetting: sbt.SettingKey[PathFinder],
                         naming: (String, Boolean) => String,
                         compile: (File, Seq[String]) => (String, Option[String], Seq[File]),
                         optionsSettings: sbt.SettingKey[Seq[String]]) =
    (sourceDirectory in Test, resourceManaged in Compile /* in Test wouldn't work, no mapping*/, cacheDirectory, optionsSettings, filesSetting) map {
      (src, resources, cache, options, files) =>

        import java.io._

        val cacheFile = cache / name
        val currentInfos = watch(src).get.map(f => f -> FileInfo.lastModified(f)).toMap
        val (previousRelation, previousInfo) = Sync.readInfo(cacheFile)(FileInfo.lastModified.format)

        if (previousInfo != currentInfos) {

          // Delete previous generated files
          previousRelation._2s.foreach(IO.delete)

          val generated = (files x relativeTo(Seq(src))).flatMap {
            case (sourceFile, name) => {
              val (debug, min, dependencies) = compile(sourceFile, options)
              val out = new File(resources, "public/" + naming(name, false))
              val outMin = new File(resources, "public/" + naming(name, true))
              IO.write(out, debug)
              dependencies.map(_ -> out) ++ min.map {
                minified =>
                  IO.write(outMin, minified)
                  dependencies.map(_ -> outMin)
              }.getOrElse(Nil)
            }
          }

          Sync.writeInfo(cacheFile,
            Relation.empty[File, File] ++ generated,
            currentInfos)(FileInfo.lastModified.format)

          // Return new files
          generated.map(_._2).distinct.toList

        } else {

          // Return previously generated files
          previousRelation._2s.toSeq

        }

    }


  val CoffeescriptCompilerForTests = {
    val compilerName = "coffeescript-test"
    val watch: (sbt.File) => PathFinder = _ ** "*.coffee"
    val naming: (String, Boolean) => String = (name, min) => name.replace(".coffee", if (min) ".min.js" else ".js")
    val compileAsset: (File, Seq[String]) => (String, Option[String], Seq[File]) = {
      (coffeeFile, options) =>
        import scala.util.control.Exception._
        val jsSource = play.core.coffeescript.CoffeescriptCompiler.compile(coffeeFile, options)
        val minified = catching(classOf[CompilationException])
          .opt(play.core.jscompile.JavascriptCompiler.minify(jsSource, Some(coffeeFile.getName())))
        (jsSource, minified, Seq(coffeeFile))
    }

    TestAssetsCompiler(compilerName, watch, coffeescriptEntryPointsForTests, naming, compileAsset, coffeescriptOptions)
  }

  val deleteCoffeeTestAssets = TaskKey[Unit]("play-coffee-qunit-delete-generated-js")
  val deleteCoffeeTestAssetsTask = (managedResources in Compile, streams) map { (res, s) =>
    println(res.foreach{ file =>
      print(file.name + " ")
      if (file.name.endsWith(".test.js") || file.name.endsWith(".test.min.js")) {
        IO.delete(file)
        println("delete ")
      } else {
        println("not delete " )
      }
    })
  }

  //TODO generalize for multiple test scopes
  val qUnitRunner = sourceManaged in Test map {
    dir =>
      val file = dir / "QunitRunner.scala"
      IO.write(file, """class QunitRunner extends QUnitTestsRunner
                       |""".stripMargin)
      Seq(file)
  }

  override lazy val settings: Seq[sbt.Project.Setting[_]] = Seq(

      deleteCoffeeTestAssets <<= deleteCoffeeTestAssetsTask,
      playStage <<= playStage.dependsOn(deleteCoffeeTestAssets),
      coffeescriptEntryPointsForTests <<= (sourceDirectory in Test)(testDir => testDir ** "*.coffee"),
      resourceGenerators in Compile <+= CoffeescriptCompilerForTests,
      sourceGenerators in Test <+= qUnitRunner
    )
}
