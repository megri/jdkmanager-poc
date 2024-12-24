package com.github.megri.vscode.jdkmanager
package commands


import cps.{async, await}
import typings.vscode.mod as vscode

import scala.scalajs.js

import model.*


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


private def ask(title: String, details: String, ctaTitle: String) =
    showBooleanModal(title, details, ctaTitle, cancelTitle = "Skip")


private def showNotice(message: String): Unit =
    vscode.window.showInformationMessage(message)


class Commands(config: MutableConfig, actions: Actions):
    def reportJavaHomeEnv() = showNotice(s"JAVA_HOME from env = '${process.env.get("JAVA_HOME")}'")

    def discoverJdks(): Future[Unit] =
        async[Future]:
            val foundJdks = await(actions.discoverJDKs())
            val jdks = foundJdks.map(foundJdk => JDK(foundJdk.path, foundJdk.version.major, foundJdk.version.full))
            await(config.setInstalledJdks(jdks))
            showNotice(s"${jdks.size} JDK(s) configured.")

    def selectJdk(): Future[Unit] =
        class JdkChoice(val jdk: JDK) extends vscode.QuickPickItem:
            this.setDescription(jdk.fullVersion)
            def label = s"${jdk.majorVersion}"

        def discoverJDKsAsk() = ask("No JDKs configured", "Would you like to run JDK discovery?", "Discover JDKs")

        async[Future]:
            if config.installedJdks.isEmpty then
                val shouldDiscoverJDKs = await(discoverJDKsAsk())
                if shouldDiscoverJDKs then
                    await(discoverJdks())
                    val options = vscode.QuickPickOptions().setTitle("Choose JDK")
                    val choices = config.installedJdks.map(JdkChoice(_))
                    val choice = await(vscode.window.showQuickPick_T(choices, options).toFuture)
                    if choice.isDefined then
                        val jdk = await(config.setProjectJdk(choice.get.jdk))
                        showNotice(s"Selected JDK: ${jdk.get.majorVersion} (${jdk.get.homePath})")

        async[Future]:
            val shouldDiscoverJDKs =
                (config.showIntroduction && await(
                    ask("Thank you for using JDKManager!", "Would you like to run JDK discovery?", "Discover JDKs")
                )) ||
                    (config.installedJdks.isEmpty && await(discoverJDKsAsk()))
            if shouldDiscoverJDKs then await(discoverJdks())
