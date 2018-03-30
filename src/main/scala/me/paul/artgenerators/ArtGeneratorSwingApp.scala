package me.paul.artgenerators

import java.awt.{Color, Font}
import java.io.File

import javax.swing.{UIManager, UnsupportedLookAndFeelException}
import javax.swing.text.{AbstractDocument, AttributeSet, DefaultCaret, DocumentFilter}
import javax.swing.text.DocumentFilter.FilterBypass

import scala.swing._
import scala.swing.event._

object ArtGeneratorSwingApp extends SimpleSwingApplication {

    val DefaultColor: Color = Color.BLUE
    val ValidColor: Color   = Color.GREEN
    val WarningColor: Color = Color.YELLOW
    val InvalidColor: Color = Color.RED

    try {
        // Set System L&F
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
    } catch {
        case ue: UnsupportedLookAndFeelException =>
            ue.printStackTrace()
        case cnre: ClassNotFoundException =>
            cnre.printStackTrace()
        case ie: InstantiationException =>
            ie.printStackTrace()
        case iae: IllegalAccessException =>
            iae.printStackTrace()
        case e: Exception =>
            e.printStackTrace()
    }

    var canStart = true

    /* ----- Component creation ----- */

    // Top Level labels

    val attributeLabel = new Label("Attribute")
    val valueLabel = new Label("Value")
    val feedbackLabel = new Label("Feedback")

    // width and height rows

    val imageWidthTextFieldLabel = new Label("Width: ")
    val imageHeightTextFieldLabel = new Label("Height: ")
    val imageWidthTextField, imageHeightTextField = new TextField {

        object IntegralFilter extends DocumentFilter {
            override def insertString(fb: FilterBypass, offs: Int, str: String, a: AttributeSet): Unit = {
                if ( str.forall( c => c.isDigit) )
                    super.insertString(fb, offs, str, a)
            }
            override def replace(fb: FilterBypass, offs: Int, l: Int, str: String, a: AttributeSet): Unit = {
                if ( str.forall( c => c.isDigit) )
                    super.replace(fb, offs, l, str, a)
            }
        }

        peer.getDocument.asInstanceOf[AbstractDocument].setDocumentFilter(IntegralFilter)

        text = ""
    }

    val imageWidthFeedbackLabel: Label = new Label {
        foreground = DefaultColor
        text = f"Using value of ${DefaultParameters.Width} [DEFAULT]"
    }
    val imageHeightFeedbackLabel: Label = new Label {
        foreground = DefaultColor
        text = f"Using value of ${DefaultParameters.Height} [DEFAULT]"
    }

    // open file check box

    val openFileCheckBoxLabel = new Label("Open File[s] After Generation?")
    val openFileCheckBox: CheckBox = new CheckBox {
        selected = DefaultParameters.OpenFile
    }
    val openFileFeedbackLabel: Label = new Label {
        foreground = DefaultColor
        text = "File[s] will be opened [DEFAULT]"
    }

    // filename

    val filenameTextFieldLabel = new Label("Filename: ")
    val filenameTextField: TextField = new TextField {
        text = ""
    }
    val filenameFeedbackLabel: Label = new Label {
        foreground = DefaultColor
        text = "Filename: " + "\"" + s"${DefaultParameters.Version}-${DefaultParameters.Width}x${DefaultParameters.Height}" + "\"" + " [DEFAULT]"
    }

    // file path

    val filepathFileChooserButtonLabel = new Label("Choose Filepath: ")
    val filepathFileChooser: FileChooser = new FileChooser {
        fileSelectionMode = FileChooser.SelectionMode.DirectoriesOnly
        selectedFile = new File(new File(ArtGeneratorSwingApp.this.getClass.getProtectionDomain.getCodeSource.getLocation.getFile).getParent
                + s"/images/${DefaultParameters.Version}/${DefaultParameters.Width}x${DefaultParameters.Height}/")
    }
    val filepathFileChooserButton = new Button("CHOOSE FOLDER")
    val filepathFeedbackLabel: Label = new Label {
        foreground = DefaultColor
        text = s"Output Folder: ${filepathFileChooser.selectedFile.getPath} [DEFAULT]"
    }

    // TODO: ImageCount
    // TODO: SeedCount

    // TODO: HueVariation
    // TODO: SaturationVariation
    // TODO: BrightnessVariation

    // TODO: HueBounds
    // TODO: SaturationBounds
    // TODO: BrightnessBounds

    // start button

    val startButtonLabel = new Label("Start Generation: ")
    val startButton = new Button("START")

    // program output

    val output: TextArea = new TextArea {
        rows = 10
        columns = 100
        editable = false
        font = new Font(Font.MONOSPACED, Font.PLAIN, 14)
        peer.getCaret.asInstanceOf[DefaultCaret].setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE)
    }

    // progress bar

    val progressBar: ProgressBar = new ProgressBar {
        min = 0
        //max = 100
        value = 0
        labelPainted = true
        font = new Font(Font.MONOSPACED, Font.PLAIN, 14)

        label = f"${0.asInstanceOf[Double]}%6.2f %%"
    }

    /* ----- MainFrame component layout ----- */

    def top: MainFrame = new MainFrame {

        title = f"Art Generator S - ${DefaultParameters.Version}"

        contents = new BoxPanel(Orientation.Vertical) {

            def topLabelGrid: GridPanel = new GridPanel(1, 4) {
                contents += attributeLabel
                contents += valueLabel
                contents += feedbackLabel
            }
            contents += topLabelGrid

            contents += new Separator()
            contents += Swing.VStrut(50)

            def settingsGrid: GridPanel = new GridPanel(0, 2) {
                contents += imageWidthTextFieldLabel
                contents += imageWidthTextField

                contents += imageHeightTextFieldLabel
                contents += imageHeightTextField

                contents += openFileCheckBoxLabel
                contents += openFileCheckBox

                contents += filenameTextFieldLabel
                contents += filenameTextField

                contents += filepathFileChooserButtonLabel
                contents += filepathFileChooserButton
            }

            def settingsFeedbackGrid: GridPanel = new GridPanel(0, 1) {

                contents += imageWidthFeedbackLabel
                contents += imageHeightFeedbackLabel
                contents += openFileFeedbackLabel
                contents += filenameFeedbackLabel
                contents += filepathFeedbackLabel

            }

            def settingsSplitGrid: GridPanel = new GridPanel(1, 2) {

                contents += settingsGrid
                contents += settingsFeedbackGrid
            }
            contents += settingsSplitGrid

            contents += Swing.VStrut(50)
            contents += new Separator()

            def startRow: FlowPanel = new FlowPanel {
                contents += startButtonLabel
                contents += startButton
            }
            contents += startRow

            contents += new Separator()

            def outputScrollPane: ScrollPane = new ScrollPane(output)
            contents += outputScrollPane

            contents += new Separator()

            contents += progressBar

            // Other

            border = Swing.EmptyBorder(10)
        }

        size = new Dimension(1000, 600)
        minimumSize = new Dimension(400, 500)

        peer.setLocationRelativeTo(null)
    }

    /* ----- Event listeners ----- */

    val publishers = List(
        startButton,
        openFileCheckBox,
        imageWidthTextField,
        imageHeightTextField,
        filenameTextField,
        filepathFileChooserButton
    )

    listenTo(publishers: _*)

    reactions += {
        case ButtonClicked(`startButton`) =>
            if (canStart) {
                val params: Parameters = initializeParameters
                val gen = new Generator(params, output, progressBar)
                gen.execute()

            } else {
                // warning: fix errors
            }
        case ButtonClicked(`openFileCheckBox`) =>
            if (openFileCheckBox.selected) {
                openFileFeedbackLabel.foreground = DefaultColor
                openFileFeedbackLabel.text = "File[s] will be opened [DEFAULT]"
            } else {
                openFileFeedbackLabel.foreground = ValidColor
                openFileFeedbackLabel.text = "File[s] will NOT be opened"
            }
        case EditDone(`imageWidthTextField`) =>
            val text = imageWidthTextField.text

            if (text.length == 0) {
                canStart = true
                imageWidthFeedbackLabel.foreground = DefaultColor
                imageWidthFeedbackLabel.text = s"Using value of ${DefaultParameters.Width} [DEFAULT]"
            } else if (text.length > 9) {
                canStart = false
                imageWidthFeedbackLabel.foreground = InvalidColor
                imageWidthFeedbackLabel.text = s"Invalid Width! Valid range is 1 - ${DefaultParameters.MaxWidth}"
            } else {
                val value = text.toInt
                if (value > DefaultParameters.MaxWidth || value < 1) {
                    canStart = false
                    imageWidthFeedbackLabel.foreground = InvalidColor
                    imageWidthFeedbackLabel.text = s"Invalid Width! Valid range is 1 - ${DefaultParameters.MaxWidth}"
                } else {
                    canStart = true
                    imageWidthFeedbackLabel.foreground = ValidColor
                    imageWidthFeedbackLabel.text = s"Using value of $value"
                }
            }
        case EditDone(`imageHeightTextField`) =>
            val text = imageHeightTextField.text

            if (text.length == 0) {
                canStart = true
                imageHeightFeedbackLabel.foreground = DefaultColor
                imageHeightFeedbackLabel.text = s"Using value of ${DefaultParameters.Height} [DEFAULT]"
            } else if (text.length > 9) {
                canStart = false
                imageHeightFeedbackLabel.foreground = InvalidColor
                imageHeightFeedbackLabel.text = s"Invalid Height! Valid range is 1 - ${DefaultParameters.MaxHeight}"
            } else {
                val value = text.toInt
                if (value > DefaultParameters.MaxHeight || value < 1) {
                    canStart = false
                    imageHeightFeedbackLabel.foreground = InvalidColor
                    imageHeightFeedbackLabel.text = s"Invalid Height! Valid range is 1 - ${DefaultParameters.MaxHeight}"
                } else {
                    canStart = true
                    imageHeightFeedbackLabel.foreground = ValidColor
                    imageHeightFeedbackLabel.text = s"Using value of $value"
                }
            }
        case EditDone(`filenameTextField`) =>
            if (filenameTextField.text == "") {
                filenameTextField.foreground = DefaultColor
                filenameFeedbackLabel.text = "Filename: " + "\"" +
                        s"${DefaultParameters.Version}-${DefaultParameters.Width}x${DefaultParameters.Height}" +
                        "\"" + " [DEFAULT]"

            } else {
                filenameTextField.foreground = ValidColor
                filenameFeedbackLabel.text = "Filename: " + "\"" + filenameTextField.text + "\""
            }
        case ButtonClicked(`filepathFileChooserButton`) =>
            val result = filepathFileChooser.showOpenDialog(null)
            if (result == FileChooser.Result.Approve) {
                filepathFeedbackLabel.foreground = ValidColor
                filepathFeedbackLabel.text = s"Output Folder: ${filepathFileChooser.selectedFile.getPath}"
            }
    }

    /* ----- Helper Methods ----- */

    def initializeParameters: Parameters = {

        val p = new Parameters

        p.Debug = DefaultParameters.Debug
        p.Version = DefaultParameters.Version

        p.ImageCount = DefaultParameters.ImageCount
        p.OpenFile = openFileCheckBox.selected

        p.Width =
            if (imageWidthTextField.text == "")
                DefaultParameters.Width
            else
                imageWidthTextField.text.toInt

        p.Height =
            if (imageHeightTextField.text == "")
                DefaultParameters.Height
            else
                imageHeightTextField.text.toInt

        progressBar.max = p.Width * p.Height

        p.Filename =
            if (filenameTextField.text == "")
                s"${p.Version}-${p.Width}x${p.Height}"
            else
                filenameTextField.text

        p.Filepath = filepathFileChooser.selectedFile

        p.FileFormat = DefaultParameters.FileFormat

        p.SeedCount = DefaultParameters.SeedCount

        p.HueVariation        = DefaultParameters.HueVariation
        p.SaturationVariation = DefaultParameters.SaturationVariation
        p.BrightnessVariation = DefaultParameters.BrightnessVariation

        p.HueVariationDelta        = DefaultParameters.HueVariationDelta
        p.SaturationVariationDelta = DefaultParameters.SaturationVariationDelta
        p.BrightnessVariationDelta = DefaultParameters.BrightnessVariationDelta

        p.HueBounds        = DefaultParameters.HueBounds
        p.SaturationBounds = DefaultParameters.SaturationBounds
        p.BrightnessBounds = DefaultParameters.BrightnessBounds

        p.NorthSpreadChance = DefaultParameters.NorthSpreadChance
        p.EastSpreadChance  = DefaultParameters.EastSpreadChance
        p.SouthSpreadChance = DefaultParameters.SouthSpreadChance
        p.WestSpreadChance  = DefaultParameters.WestSpreadChance

        p.NorthSpreadChanceDelta  = DefaultParameters.NorthSpreadChanceDelta
        p.EastSpreadChanceDelta   = DefaultParameters.EastSpreadChanceDelta
        p.SouthSpreadChanceDelta  = DefaultParameters.SouthSpreadChanceDelta
        p.WestSpreadChanceDelta   = DefaultParameters.WestSpreadChanceDelta

        p
    }

}