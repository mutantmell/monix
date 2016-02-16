/*
 * Copyright (c) 2014-2016 by its authors. Some rights reserved.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.streams.internal.operators

import monix.execution.Ack
import monix.execution.Ack.Continue
import monix.streams.ObservableLike.Operator
import monix.streams.observers.Subscriber

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

private[streams] final
class ThrottleFirstOperator[A](interval: FiniteDuration)
  extends Operator[A,A] {

  def apply(out: Subscriber[A]): Subscriber[A] =
    new Subscriber[A] {
      implicit val scheduler = out.scheduler

      private[this] val intervalMs = interval.toMillis
      private[this] var nextChange = 0L

      def onNext(elem: A): Future[Ack] = {
        val rightNow = scheduler.currentTimeMillis()
        if (nextChange <= rightNow) {
          nextChange = rightNow + intervalMs
          out.onNext(elem)
        }
        else
          Continue
      }

      def onError(ex: Throwable): Unit =
        out.onError(ex)

      def onComplete(): Unit =
        out.onComplete()
    }
}