package shared.ports

import scala.concurrent.Future

trait MetricsService:
  def registerForHealthcheckMonitoring(selfAddress: String): Future[Unit]
