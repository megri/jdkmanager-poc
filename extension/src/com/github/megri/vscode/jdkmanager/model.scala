package com.github.megri.vscode.jdkmanager
package model


import scala.scalajs.js

import typings.vscode.mod as vscode
import vscode.ConfigurationTarget


@js.native
trait JDK extends js.Object:
    def homePath: String = js.native
    def majorVersion: Double = js.native
    def fullVersion: String = js.native


object JDK:
    def apply(homePath: String, majorVersion: Double, fullVersion: String): JDK =
        js.Dictionary("homePath" -> homePath, "majorVersion" -> majorVersion, "fullVersion" -> fullVersion).asInstanceOf


@js.native
trait MutableConfig extends vscode.WorkspaceConfiguration:
    def installedJdks: js.Array[JDK] = js.native
    def projectJdk: js.UndefOr[Int] = js.native
    def showIntroduction: Boolean = js.native


object MutableConfig:
    def load(): MutableConfig = vscode.workspace.getConfiguration("jdkmanager").asInstanceOf

    def updateAndMutate[Value](
        config: MutableConfig,
        section: String,
        value: Value,
        configurationTarget: ConfigurationTarget
    ): Future[Value] =
        for
            _ <- config.update(section, value, configurationTarget).toFuture
            _ = config.set(section, value)
        yield value

    extension (config: MutableConfig)
        def setInstalledJdks(jdks: js.Array[JDK]): Future[scala.scalajs.js.Array[JDK]] =
            updateAndMutate(config, "installedJdks", jdks, ConfigurationTarget.Global)
        def setProjectJdk(jdk: js.UndefOr[JDK]): Future[js.UndefOr[JDK]] =
            updateAndMutate(config, "projectJdk", jdk.map(_.majorVersion), ConfigurationTarget.Workspace)
        def setShowIntroduction(value: Boolean): Future[Boolean] =
            updateAndMutate(config, "showIntroduction", value, ConfigurationTarget.Global)
