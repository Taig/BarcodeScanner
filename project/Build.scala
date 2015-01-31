import android.Keys._
import android.Plugin._
import sbt.Keys._
import sbt.Resolver.ivyStylePatterns
import sbt._

object	Build
extends	android.AutoBuild
{
	lazy val main = Project( "barcode-scanner", file( "." ) )
		.settings( androidBuildAar: _* )
		.settings(
			libraryDependencies ++= Seq(
				"com.google.zxing" % "core" % "3.1.0'"
			),
			name := "Toolbelt",
			organization := "com.taig.android.barcode.scanner",
			publishArtifact in ( Compile, packageDoc ) := false,
			publishArtifact in ( Compile, packageSrc ) := true,
			scalaVersion := "2.11.5",
			scalacOptions ++= Seq( "-deprecation", "-feature" ),
			// @see https://github.com/pfn/android-sdk-plugin/issues/88
			sourceGenerators in Compile <<= ( sourceGenerators in Compile ) ( generators => Seq( generators.last ) ),
			version := "1.0.0",
			minSdkVersion in Android := "9",
			platformTarget in Android := "android-21",
			targetSdkVersion in Android := "21"
		)
}