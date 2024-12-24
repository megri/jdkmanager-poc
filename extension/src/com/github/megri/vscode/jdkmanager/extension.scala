package com.github.megri.vscode.jdkmanager


import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSGlobal

import typings.vscode.mod as vscode


@js.native
@JSGlobal
object process extends js.Object:
    val env: js.Dictionary[String] = js.native




def registerCommand(commandName: String, command: () => Unit): Unit =
    vscode.commands.registerCommand(commandName, command)


@JSExportTopLevel("activate")
def activate(context: vscode.ExtensionContext): Unit =
    val config = model.MutableConfig.load()
    val actions = Actions.live
    val cmds = commands.Commands(config, actions)

    registerCommand("jdkmanager.discoverJdks", cmds.discoverJdks)
    registerCommand("jdkmanager.reportJavaHome", cmds.reportJavaHomeEnv)
    registerCommand("jdkmanager.selectJdk", cmds.selectJdk)
