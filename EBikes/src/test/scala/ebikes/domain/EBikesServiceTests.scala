package ebikes.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers.*
import org.scalatest.BeforeAndAfterEach
import shared.adapters.persistence.InMemoryRepositoryAdapter
import shared.technologies.persistence.InMemoryMapDatabaseImpl
import ebikes.domain.model.*
import ebikes.ports.persistence.EBikesRepository

class EBikesServiceTests extends AnyFlatSpec:
  trait CleanService:
    val db = InMemoryMapDatabaseImpl()
    val repo = new InMemoryRepositoryAdapter[EBikeId, EBike](db, "ebikes")
      with EBikesRepository
    val service = EBikesServiceImpl(repo)

  trait DoubleBikeService:
    val db = InMemoryMapDatabaseImpl()
    val repo = new InMemoryRepositoryAdapter[EBikeId, EBike](db, "ebikes")
      with EBikesRepository
    val service = EBikesServiceImpl(repo)
    val bikeIds = Set[EBikeId]("b1", "b2")
    bikeIds.foreach(service.register(_, P2D(0, 0), V2D(0, 0)))

  "eBikes" should "retrieve no bike initially" in new CleanService:
    service.eBikes().size shouldBe 0

  it should "retrieve all registered bikes" in new DoubleBikeService:
    service.eBikes().size shouldBe bikeIds.size
    service
      .eBikes()
      .map(b => bikeIds.contains(b.id))
      .reduce(_ && _) shouldBe true

  "find" should "retrieve no bike if not present" in new DoubleBikeService:
    service.find("blabla") shouldBe None

  "find" should "retrieve a bike if present" in new DoubleBikeService:
    val id = bikeIds.head
    val bike = service.find(id)
    bike.isDefined shouldBe true
    bike.get.id shouldBe id

  private given Conversion[String, EBikeId] with
    def apply(x: String): EBikeId = EBikeId(x)
