import java.awt.Color.*
import scala.concurrent.*
import scala.util.Try
import scala.swing._
import scala.swing.Swing.*
import scala.swing.event._
import sttp.client4.*
import upickle.default.*
import dto.*
import Utils.*
import ExecutionContext.Implicits.global

object SwingApp extends SimpleSwingApplication {

  var authToken: Option[String] = None

  // Login/Register Window
  def top: Frame = new MainFrame {
    title = "Login/Register"

    val usernameField = new TextField { columns = 15 }
    val passwordField = new PasswordField { columns = 15 }
    val loginButton = new Button("Login")
    val registerButton = new Button("Register")

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += new Label("Username:")
        contents += usernameField
      }
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += new Label("Password:")
        contents += passwordField
      }
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += loginButton
        contents += registerButton
      }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }

    listenTo(loginButton, registerButton)

    reactions += {
      case ButtonClicked(`loginButton`) =>
        val username = usernameField.text
        val password = passwordField.password.mkString
        if (username.nonEmpty && password.nonEmpty) {
          for
            token <- login(username, password)
            _ = onEDT:
              token match
                case Left(error)  => Dialog.showMessage(this, error)
                case Right(token) => openHomeWindow(Username(username))
          yield ()
        } else {
          Dialog.showMessage(this, "Please enter valid credentials.")
        }

      case ButtonClicked(`registerButton`) =>
        val username = usernameField.text
        val password = passwordField.password.mkString
        if (username.nonEmpty && password.nonEmpty) {
          for
            token <- register(username, password)
            _ = onEDT:
              token match
                case Left(error)  => Dialog.showMessage(this, error)
                case Right(token) => openHomeWindow(Username(username))
          yield ()
        } else {
          Dialog.showMessage(this, "Please enter valid credentials.")
        }
    }

    def openHomeWindow(username: Username): Unit = {
      val homeWindow = new HomeFrame(username)
      homeWindow.visible = true
      this.close()
    }

    private def login(
        username: String,
        password: String
    ): Future[Either[String, String]] =
      for
        res <- quickRequest
          .post(
            uri"http://localhost:8080/authentication/$username/authenticate"
          )
          .jsonBody(AuthenticateDTO(password))
          .sendAsync()
        token =
          for
            res <- res
            token <- Either.cond(res.isSuccess, res.body, res.body)
            _ = authToken = Some(token)
          // TODO: set timer for refresh
          yield (token)
      yield (token)

    private def register(
        username: String,
        password: String
    ): Future[Either[String, String]] =
      for
        res <- quickRequest
          .post(
            uri"http://localhost:8080/authentication/register"
          )
          .jsonBody(RegisterDTO(Username(username), password))
          .sendAsync()
        token =
          for
            res <- res
            token <- Either.cond(res.isSuccess, res.body, res.body)
            _ = authToken = Some(token)
          // TODO: set timer for refresh
          yield (token)
      yield (token)
  }

  // Home Window
  class HomeFrame(private val username: Username) extends Frame {
    private var credits: Option[Int] = None
    private var availableEBikes: Seq[EBikeId] = Seq()
    private var ride: Option[Ride] = None

    title = "Home"

    val creditsLabel = new Label()
    val refreshButton = new Button("Refresh")
    val rechargeField = new TextField { columns = 10 }
    val rechargeButton = new Button("Recharge credit")
    val bikesCombo = new ComboBox[String](Seq())
    val rideButton = new Button()

    contents = new BoxPanel(Orientation.Vertical) {
      contents += refreshButton
      contents += creditsLabel
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += rechargeField
        contents += rechargeButton
      }
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += new Label("Available EBikes")
        contents += bikesCombo
        contents += rideButton
      }

      border = Swing.EmptyBorder(10, 10, 10, 10)
    }

    listenTo(refreshButton, rechargeButton, rideButton)

    reactions += {
      case ButtonClicked(`refreshButton`) => fetchData()

      case ButtonClicked(`rechargeButton`) =>
        Try(rechargeField.text.toInt).filter(_ > 0) match {
          case scala.util.Success(amount) =>
            rechargeButton.enabled = false
            rechargeCredits(amount).map: res =>
              onEDT:
                res match
                  case Left(value) => Dialog.showMessage(this, value)
                  case Right(c) =>
                    this.credits = Some(c.amount)
                    updateUI()
                rechargeButton.enabled = true
          case _ => Dialog.showMessage(this, "Invalid recharge amount.")
        }

      case ButtonClicked(`rideButton`) =>
        if ride == None then
          if bikesCombo.selection.index != -1 then
            rideButton.enabled = false
            startRide(EBikeId(bikesCombo.selection.item)).map: res =>
              onEDT:
                res match
                  case Left(value) => Dialog.showMessage(this, value)
                  case Right(ride) =>
                    this.ride = Some(ride)
                    updateUI()
                rideButton.enabled = true
        else
          rideButton.enabled = false
          stopRide().map: res =>
            onEDT:
              res match
                case Left(value) => Dialog.showMessage(this, value)
                case Right(value) =>
                  this.ride = None
                  updateUI()
              rideButton.enabled = true
    }

    updateUI()
    fetchData()

    private def updateUI(): Unit =
      val credits = this.credits.map(_.toString()).getOrElse("???")
      creditsLabel.text = s"Credits: $credits"
      bikesCombo.peer.setModel(
        ComboBox.newConstantModel(availableEBikes.map(_.value))
      )
      if ride.isDefined then
        rideButton.text = "Stop ride"
        bikesCombo.enabled = false
        rechargeButton.enabled = false
      else
        rideButton.text = "Start ride"
        bikesCombo.enabled = true
        rechargeButton.enabled = true

    private def fetchData(): Unit =
      fetchCredits().map(res =>
        onEDT:
          res match
            case Left(value) => Dialog.showMessage(this, value)
            case Right(c)    => this.credits = Some(c.amount)
          updateUI()
      )
      fetchAvailableEBikes().map(res =>
        onEDT:
          res match
            case Left(value)   => Dialog.showMessage(this, value)
            case Right(eBikes) => this.availableEBikes = eBikes
          updateUI()
      )

    private def fetchCredits(): Future[Either[String, CreditDTO]] =
      for
        res <- quickRequest
          .get(
            uri"http://localhost:8082/users/${username.value}/credit"
          ) // TODO: move to api gateway
          .authorizationBearer(authToken.get)
          .sendAsync()
        credit =
          for
            res <- res
            credit <- Either.cond(
              res.isSuccess,
              read[CreditDTO](res.body),
              res.body
            )
          yield (credit)
      yield (credit)

    private def rechargeCredits(
        amount: Int
    ): Future[Either[String, CreditDTO]] =
      for
        res <- quickRequest
          .post(
            uri"http://localhost:8082/users/${username.value}/credit"
          ) // TODO: move to api gateway
          .jsonBody(CreditDTO(amount))
          .authorizationBearer(authToken.get)
          .sendAsync()
        credit =
          for
            res <- res
            credit <- Either.cond(
              res.isSuccess,
              read[CreditDTO](res.body),
              res.body
            )
          yield (credit)
      yield (credit)

    private def fetchAvailableEBikes(): Future[Either[String, Seq[EBikeId]]] =
      for
        res <- quickRequest
          .get(
            uri"http://localhost:8083/rides/availableEBikes"
          ) // TODO: move to api gateway
          .authorizationBearer(authToken.get)
          .sendAsync()
        eBikes =
          for
            res <- res
            eBikes <- Either.cond(
              res.isSuccess,
              read[Seq[EBikeId]](res.body),
              res.body
            )
          yield (eBikes)
      yield (eBikes)

    private def startRide(eBikeId: EBikeId): Future[Either[String, Ride]] =
      for
        res <- quickRequest
          .post(
            uri"http://localhost:8083/rides"
          ) // TODO: move to api gateway
          .jsonBody(StartRideDTO(eBikeId, username))
          .authorizationBearer(authToken.get)
          .sendAsync()
        ride =
          for
            res <- res
            ride <- Either.cond(
              res.isSuccess,
              read[Ride](res.body),
              res.body
            )
          yield (ride)
      yield (ride)

    private def stopRide(): Future[Either[String, Unit]] =
      ride match
        case None => Future(Left("You're not riding"))
        case Some(ride) =>
          for
            res <- quickRequest
              .put(
                uri"http://localhost:8083/rides/${ride.id.value}"
              ) // TODO: move to api gateway
              .authorizationBearer(authToken.get)
              .sendAsync()
            result =
              for
                res <- res
                result <- Either.cond(res.isSuccess, (), res.body)
              yield (result)
          yield (result)
  }
}
