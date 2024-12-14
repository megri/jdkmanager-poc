package com.github.megri.vscode.jdkmanager.actions


import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.scalajs.js

import typings.jdkUtils.anon.Asdf as SkipFrom
import typings.jdkUtils.distMod as jdkUtils


case class Version(major: Double, full: String)
case class FoundJDK(path: String, version: Version, foundInEnv: Boolean)


def discover()(using ExecutionContext): Future[js.Array[FoundJDK]] =
    val searchOptions = jdkUtils
        .IOptions()
        .setCheckJavac(true)
        .setWithTags(true)
        .setWithVersion(true)
        .setSkipFrom(
            SkipFrom()
                .setAsdf(true)
                .setGradle(true)
                .setInPathEnv(true)
                .setJabba(true)
                .setJavaHomeEnv(true)
                .setJdkHomeEnv(true)
                .setJenv(true)
                .setSdkman(true)
        )

    jdkUtils
        .findRuntimes(searchOptions)
        .toFuture
        .map: runtimes =>
            runtimes
                .filter(_.hasJavac.getOrElse(false))
                .map: runtime =>
                    val home = runtime.homedir
                    val fullVersion = runtime.version.map(_.java_version).get
                    val majorVersion = runtime.version.map(_.major).get
                    val isJavaHomeEnv = runtime.isJavaHomeEnv.getOrElse(false)
                    val isJdkHomeEnv = runtime.isJdkHomeEnv.getOrElse(false)
                    FoundJDK(home, Version(majorVersion, fullVersion), isJavaHomeEnv | isJdkHomeEnv)
