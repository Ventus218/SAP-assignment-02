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

  var token: Option[String] = None

  // Shared state
  var credits: Int = 0

  // Login/Register Window
  def top: Frame = new MainFrame {
    title = "Login/Register"

    val usernameField = new TextField { columns = 15 }
    val passwordField = new PasswordField { columns = 15 }
    val loginButton = new Button("Login")
    val registerButton = new Button("Register")
    val messageLabel = new Label("")

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += new Label("Username:")
        contents += usernameField
      }
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += new Label("Password:")
        contents += passwordField
      }
      contents += messageLabel
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
                case Left(error)  => messageLabel.text = error
                case Right(token) => openHomeWindow()
          yield ()
        } else {
          messageLabel.text = "Please enter valid credentials."
        }

      case ButtonClicked(`registerButton`) =>
        val username = usernameField.text
        val password = passwordField.password.mkString
        if (username.nonEmpty && password.nonEmpty) {
          messageLabel.text = "Registration successful!"
          openHomeWindow()
        } else {
          messageLabel.text = "Please enter valid credentials."
        }
    }

    def openHomeWindow(): Unit = {
      val homeWindow = new HomeFrame
      homeWindow.visible = true
      this.close()
    }
  }

  // Home Window
  class HomeFrame extends Frame {
    title = "Home"

    val creditsLabel = new Label(s"Credits: $credits")
    val refreshButton = new Button("Refresh")
    val rechargeField = new TextField { columns = 10 }
    val rechargeButton = new Button("Recharge credit")
    val messageLabel = new Label("")

    contents = new BoxPanel(Orientation.Vertical) {
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += creditsLabel
        contents += refreshButton
      }
      contents += new BoxPanel(Orientation.Horizontal) {
        contents += rechargeField
        contents += rechargeButton
      }
      contents += messageLabel
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }

    listenTo(refreshButton, rechargeButton)

    reactions += {
      case ButtonClicked(`refreshButton`) =>
        creditsLabel.text = s"Credits: $credits"

      case ButtonClicked(`rechargeButton`) =>
        Try(rechargeField.text.toInt).filter(_ > 0) match {
          case scala.util.Success(amount) =>
            credits += amount
            creditsLabel.text = s"Credits: $credits"
            messageLabel.text = s"Successfully recharged $amount credits."
            rechargeField.text = ""
          case _ =>
            messageLabel.text =
              "Invalid recharge amount. Please enter a positive integer."
        }
    }
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
          _ = this.token = Some(token)
        // TODO: set timer for refresh
        yield (token)
    yield (token)
}
