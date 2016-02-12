package com.harrys.hyppo.worker.actor.queue

import javax.inject.Inject

import com.google.inject.ImplementedBy
import com.google.inject.assistedinject.Assisted

import com.harrys.hyppo.worker.api.code.ExecutableIntegration
import com.harrys.hyppo.worker.scheduling.WorkQueuePrioritizer

/**
  * Created by jpetty on 2/12/16.
  */
@ImplementedBy(classOf[DefaultDelgationStrategy])
trait DelegationStrategy {
  def priorityOrderWithoutAffinity(): Iterator[String]
  def priorityOrderWithPreference(prefer: ExecutableIntegration): Iterator[String]
}

object DelegationStrategy {
  trait Factory {
    def apply(@Assisted statusTracker: QueueStatusTracker, @Assisted prioritizer: WorkQueuePrioritizer): DelegationStrategy
  }
}

final class DefaultDelgationStrategy @Inject()
(
  @Assisted statusTracker:   QueueStatusTracker,
  @Assisted workPrioritizer: WorkQueuePrioritizer
) extends DelegationStrategy {

  override def priorityOrderWithoutAffinity(): Iterator[String] = ???

  override def priorityOrderWithPreference(prefer: ExecutableIntegration): Iterator[String] = ???
}
