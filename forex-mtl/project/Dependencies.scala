import sbt._

object Dependencies {

  object Versions {
    val cats                = "2.7.0"
    val catsEffect          = "3.3.0"
    val fs2                 = "3.2.3"
    val http4s              = "0.23.7"
    val http4sJdkClient     = "0.5.0"
    val circe               = "0.14.1"
    val pureConfig          = "0.17.1"
    val scalaUri            = "4.0.0-M3"

    val kindProjector       = "0.10.3"
    val slf4j               = "1.7.32"
    val airframe            = "21.12.1"
    val scalaCheck          = "1.15.4"
    val scalaTest           = "3.2.10"
    val catsScalaCheck      = "0.3.1"
    val catsEffectTest      = "3.3.0"
  }

  object Libraries {
    def circe(artifact: String): ModuleID = "io.circe"    %% artifact % Versions.circe
    def http4s(artifact: String, version: String = Versions.http4s): ModuleID = "org.http4s" %% artifact % version

    lazy val cats                = "org.typelevel"         %% "cats-core"                  % Versions.cats
    lazy val catsEffect          = "org.typelevel"         %% "cats-effect"                % Versions.catsEffect
    lazy val fs2                 = "co.fs2"                %% "fs2-core"                   % Versions.fs2

    lazy val http4sDsl           = http4s("http4s-dsl")
    lazy val http4sServer        = http4s("http4s-blaze-server")
    lazy val http4sClient        = http4s("http4s-blaze-client")
    lazy val http4sJdkClient     = http4s("http4s-jdk-http-client", Versions.http4sJdkClient)
    lazy val http4sCirce         = http4s("http4s-circe")
    lazy val circeCore           = circe("circe-core")
    lazy val circeGeneric        = circe("circe-generic")
    lazy val circeGenericExt     = circe("circe-generic-extras")
    lazy val circeParser         = circe("circe-parser")
    lazy val pureConfig          = "com.github.pureconfig" %% "pureconfig"                 % Versions.pureConfig
    lazy val scalaUri            = "io.lemonlabs"          %% "scala-uri"                  % Versions.scalaUri

    // Compiler plugins
    lazy val kindProjector       = "org.typelevel"         %% "kind-projector"             % Versions.kindProjector

    // Runtime
    lazy val slf4jApi            = "org.slf4j"             % "slf4j-api"                   % Versions.slf4j
    lazy val slf4j               = "org.slf4j"             % "slf4j-simple"                % Versions.slf4j
    lazy val airframe            =  "org.wvlet.airframe"   %% "airframe-log"               % Versions.airframe

    // Test
    lazy val scalaTest           = "org.scalatest"         %% "scalatest"                  % Versions.scalaTest
    lazy val scalaCheck          = "org.scalacheck"        %% "scalacheck"                 % Versions.scalaCheck
    lazy val catsScalaCheck      = "io.chrisdavenport"     %% "cats-scalacheck"            % Versions.catsScalaCheck
    lazy val catsEffectTest      = "org.typelevel"         %% "cats-effect-testkit"        % Versions.catsEffectTest
  }

}
