libraryDependencies ++= {
  val specs2V = "3.8.8"
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "org.clapper" %% "grizzled-slf4j" % "1.3.0",
    "org.reactivemongo" %% "reactivemongo" % "0.18.6",
    "com.github.limansky" %% "mongoquery-reactive" % "0.7",
    "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.2.0",
    "org.specs2" % "specs2-core_2.11" % specs2V % Test,
    "org.specs2" % "specs2-mock_2.11" % specs2V % Test
  )
}

Revolver.settings