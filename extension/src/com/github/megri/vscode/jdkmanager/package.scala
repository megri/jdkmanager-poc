package com.github.megri.vscode.jdkmanager


export scala.concurrent.Future
export scala.concurrent.ExecutionContext
export cps.monads.FutureAsyncMonad

import scala.scalajs.js


extension [A](promiseLike: typings.std.PromiseLike[js.UndefOr[A]])
    /** Useful for ignoring the result of vscode information messages, letting a chain progress without waiting for the
      * user to interact with the message.
      */
    def toBackground: Future[Unit] = Future.unit


given [O]: Conversion[Function0[O], js.Function1[Any, Any]] = f => _ => f()

given ExecutionContext = ExecutionContext.global
