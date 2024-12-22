package com.github.megri.vscode.jdkmanager


import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel
import scala.scalajs.js.annotation.JSGlobal

import cps.*
import cps.monads.FutureAsyncMonad
import typings.vscode.mod as vscode

import vscode.ConfigurationTarget


extension [A](promiseLike: typings.std.PromiseLike[js.UndefOr[A]])
    /** Useful for ignoring the result of vscode information messages, letting a chain progress without waiting for the
      * user to interact with the message.
      */
    def toBackground: Future[Unit] = Future.unit


given [O]: Conversion[Function0[O], js.Function1[Any, Any]] = f => _ => f()

given ExecutionContext = ExecutionContext.global


@js.native
@JSGlobal
object process extends js.Object:
    val env: js.Dictionary[String] = js.native


@js.native
trait JDK extends js.Object:
    def homePath: String = js.native
    def majorVersion: Double = js.native
    def fullVersion: String = js.native


object JDK:
    def apply(homePath: String, majorVersion: Double, fullVersion: String): JDK =
        js.Dictionary("homePath" -> homePath, "majorVersion" -> majorVersion, "fullVersion" -> fullVersion).asInstanceOf


@js.native
trait ConfiguredJDKs extends js.Array[JDK]


@js.native
trait Configuration extends vscode.WorkspaceConfiguration:
    def showIntroduction: Boolean = js.native
    def installedJdks: js.Array[JDK] = js.native
    def projectJdk: js.UndefOr[Int] = js.native


object Configuration:
    def load(): Configuration = vscode.workspace.getConfiguration("jdkmanager").asInstanceOf

    extension (conf: Configuration)
        def setInstalledJdks(jdks: js.Array[JDK]): Future[js.Array[JDK]] =
            conf.update("installedJdks", jdks, ConfigurationTarget.Global).toFuture.map(_ => jdks)
        def setProjectJdk(jdk: JDK): Future[JDK] =
            conf.update("projectJdk", jdk.majorVersion, ConfigurationTarget.Workspace).toFuture.map(_ => jdk)
        def setShowIntroduction(value: Boolean): Future[Unit] =
            conf.update("showIntroduction", value, ConfigurationTarget.Global).toFuture


def showBooleanModal(
    title: String,
    details: String,
    ctaTitle: String,
    cancelTitle: String = "cancel"
): Future[Boolean] =
    val options = vscode
        .MessageOptions()
        .setModal(true)
        .setDetail(details)

    val cta = vscode.MessageItem(ctaTitle)
    val cancel = vscode.MessageItem(cancelTitle).setIsCloseAffordance(true)

    vscode.window.showInformationMessage(title, options, cta, cancel).toFuture.map(choice => choice == cta)


def showIntroductionMessage(): Future[Boolean] =
    showBooleanModal(
        title = "Thank you for using JDKManager!",
        details = """Click the button below to attempt to automatically populate configuration
                     for your installed JDKs, or run the 'Discover JKDs' command later.""",
        ctaTitle = "Discover JDKs",
        cancelTitle = "Skip"
    )


def showNoInstalledJdksNotice(): Future[Boolean] =
    showBooleanModal(
        title = "No JDKs configured.",
        details = "Either discover JDKs installed on your system or edit the user config manually.",
        ctaTitle = "Discover JDKs",
        cancelTitle = "I'll do it manually",
    )


@JSExportTopLevel("activate")
def activate(context: vscode.ExtensionContext): Unit =
    val reportJavaHomeEnv = () =>
        vscode.window.showInformationMessage(s"JAVA_HOME from env = '${process.env.get("JAVA_HOME")}'").toBackground

    val discoverJdks = () =>
        async[Future]:
            val foundJdks = await(actions.discover())
            val jdks = foundJdks.map(foundJdk => JDK(foundJdk.path, foundJdk.version.major, foundJdk.version.full))
            await(Configuration.load().setInstalledJdks(jdks))
            val displayString = s"${jdks.size} JDK(s) configured."
            vscode.window.showInformationMessage(displayString).toBackground

    val selectJdk = () =>
        class JdkChoice(val jdk: JDK) extends vscode.QuickPickItem:
            this.setDescription(jdk.fullVersion)
            def label = s"${jdk.majorVersion}"

        async[Future]:
            if Configuration.load().installedJdks.isEmpty then
                val shouldDiscoverJDKs = await(showNoInstalledJdksNotice())
                if shouldDiscoverJDKs then
                    await(discoverJdks())
                    val configuration = Configuration.load()
                    val options = vscode.QuickPickOptions().setTitle("Choose JDK")
                    val choices = configuration.installedJdks.map(JdkChoice(_))

                    val choice = await(vscode.window.showQuickPick_T(choices, options).toFuture)
                    choice.foreach: choice =>
                        val jdk = await(configuration.setProjectJdk(choice.jdk))
                        val displayString = s"${jdk.majorVersion} (${jdk.homePath})"
                        vscode.window.showInformationMessage(s"Selected JDK: $displayString").toBackground

    async[Future]:
        val configuration = Configuration.load()
        val shouldDiscoverJDKs =
            (configuration.showIntroduction && await(showIntroductionMessage())) ||
            (configuration.installedJdks.isEmpty && await(showNoInstalledJdksNotice()))
        if shouldDiscoverJDKs then await(discoverJdks())

    vscode.commands.registerCommand("jdkmanager.discoverJdks", discoverJdks)
    vscode.commands.registerCommand("jdkmanager.reportJavaHome", reportJavaHomeEnv)
    vscode.commands.registerCommand("jdkmanager.selectJdk", selectJdk)
