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

package monix.reactive.internal.operators

import monix.execution.Ack
import monix.execution.Ack.{Continue, Stop}
import monix.reactive.Observable
import monix.reactive.observables.ObservableLike.Operator
import monix.reactive.observers.Subscriber
import scala.collection.mutable

private[reactive] final class TakeLastOperator[A](n: Int)
  extends Operator[A, A] {

  def apply(out: Subscriber[A]): Subscriber[A] =
    new Subscriber.Sync[A] {
      implicit val scheduler = out.scheduler
      private[this] val queue = mutable.Queue.empty[A]
      private[this] var queued = 0

      def onNext(elem: A): Ack = {
        if (n <= 0)
          Stop
        else if (queued < n) {
          queue.enqueue(elem)
          queued += 1
          Continue
        }
        else {
          queue.enqueue(elem)
          queue.dequeue()
          Continue
        }
      }

      def onComplete(): Unit = {
        Observable.fromIterable(queue).unsafeSubscribeFn(out)
      }

      def onError(ex: Throwable): Unit = {
        out.onError(ex)
      }
    }
}