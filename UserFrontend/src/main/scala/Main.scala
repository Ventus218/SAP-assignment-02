import scala.swing._
import scala.swing.event._
import scala.util.Try

object SwingApp extends SimpleSwingApplication {

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
          messageLabel.text = "Login successful!"
          openHomeWindow()
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
}
