import java.awt.BorderLayout;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import toolbox.Input;
import toolbox.Sketch;
import toolbox.gfx.Color;
import toolbox.math.Maths;
import toolbox.utils.Console;

public class Fireplace extends Sketch {
    
    // ui constants (up to 50 commands are saved)
    private final int COMMANDS_HISTORY_LENGTH = 50;

    // window size
    private static int SIZE = 50;

    // simulation parameters
    private float updatePercentage = 0.4f; // the bigger the faster and more laggy the animation will look. It's the chances of a pixel being updated (ranged [0, 1])
    private int maxTemperature = 50; // the bigger the higher and the more colors the flame can have
    private float horizontalMotion = 1.25f; // the bigger the more the flame can move sideways (>= 0)
    private float dimmingFactor = 5.0f; // the bigger the faster the flame decays (>= 1)
    private float redFactor = 1.0f; // how much temperature influences red color channel (ranged [0, 1])
    private float greenFactor = 0.35f; // how much temperature influences green color channel (ranged [0, 1])
    private float blueFactor = 0.0f; // how much temperature influences blue color channel (ranged [0, 1])
    private int radius = 5; // flame brush radius (>= 0)
    private int paint = maxTemperature; // flame brush paint (>= 0)

    // grid
    private int[][] grid;

    // iteration variables
    private int i, x, y;
    
    // flame brush
    private int pointerX, pointerY;
    private int xx, yy;
    
    // temporary
    private int value, actualX, xOffset, dimAmount;
    private Color color = new Color(0, 0, 0);

    // command line UI components
    private JPanel uiPanel;
    private TextArea parametersList;
    private JLabel feedbackLabel;
    private TextField commandLine;
    
    private int commandsHistoryIndex = 0;
    private int lastCommand = 1;
    private String[] commandsHistory = new String[COMMANDS_HISTORY_LENGTH];

    /** Updates the parametersList text area to show the updated simulation parameters values **/
    private void updateParametersList(String format) {
        parametersList.setText(
            String.format(
                format,
                
                updatePercentage,
                maxTemperature,
                horizontalMotion,
                dimmingFactor,
                redFactor,
                greenFactor,
                blueFactor,
                radius,
                paint,
                maxTemperature
            )
        );
    }

    private void feedbackError(String message) {
        feedbackLabel.setForeground(java.awt.Color.RED);
        feedbackLabel.setText(message);
    }

    private void setCommandLinePlaceholder() {
        commandLine.setForeground(java.awt.Color.GRAY);
        commandLine.setText("Type a command, then press ENTER (UP / DOWN ARROWS to see history)");
    }

    private void parseCommand(String format) {
        boolean success = true;
                
        String command = commandLine.getText();

        commandsHistoryIndex = 0;
        commandLine.setText("");
        feedbackLabel.setText("");
        feedbackLabel.setForeground(java.awt.Color.RED);
        
        // command parsing
        if (command.strip().length() == 0) return;

        String[] tokens = command.split(" ");
        if (tokens.length != 1 && tokens.length != 2 && tokens.length != 4) {
            feedbackError("Invalid command");
            success = false;
        } else {
            try {
                feedbackLabel.setForeground(java.awt.Color.BLUE);
                switch (tokens[0]) {
                    case "lat":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        updatePercentage = Float.parseFloat(tokens[1]);
                        updatePercentage = Maths.clamp(updatePercentage, 0, 1);
                        feedbackLabel.setText("Set simulation latency to " + updatePercentage);
                        break;
                    case "temp":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        maxTemperature = Integer.parseInt(tokens[1]);
                        maxTemperature = Math.max(0, maxTemperature);
                        feedbackLabel.setText("Set base temperature to " + maxTemperature);
                        break;
                    case "hmove":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        horizontalMotion = Float.parseFloat(tokens[1]);
                        horizontalMotion = Math.max(0, horizontalMotion);
                        feedbackLabel.setText("Set horizontal flame motion to " + horizontalMotion);
                        break;
                    case "dim":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        dimmingFactor = Float.parseFloat(tokens[1]);
                        dimmingFactor = Math.max(1, dimmingFactor);
                        feedbackLabel.setText("Set flame dimming to " + dimmingFactor);
                        break;
                    case "red":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        redFactor = Float.parseFloat(tokens[1]);
                        redFactor = Maths.clamp(redFactor, 0, 1);
                        feedbackLabel.setText("Set red channel to " + redFactor);
                        break;
                    case "green":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        greenFactor = Float.parseFloat(tokens[1]);
                        greenFactor = Maths.clamp(greenFactor, 0, 1);
                        feedbackLabel.setText("Set green channel to " + greenFactor);
                        break;
                    case "blue":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        blueFactor = Float.parseFloat(tokens[1]);
                        blueFactor = Maths.clamp(blueFactor, 0, 1);
                        feedbackLabel.setText("Set blue channel to " + blueFactor);
                        break;
                    case "color":
                        if (tokens.length != 4) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        redFactor = Float.parseFloat(tokens[1]);
                        redFactor = Maths.clamp(redFactor, 0, 1);
                        greenFactor = Float.parseFloat(tokens[2]);
                        greenFactor = Maths.clamp(greenFactor, 0, 1);
                        blueFactor = Float.parseFloat(tokens[3]);
                        blueFactor = Maths.clamp(blueFactor, 0, 1);
                        feedbackLabel.setText("Set color to (" + redFactor + ", " + greenFactor + ", " + blueFactor + ")");
                        break;
                    case "radius":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        radius = Integer.parseInt(tokens[1]);
                        radius = (int) Maths.clamp(radius, 0, SIZE);
                        feedbackLabel.setText("Set flame brush radius to " + radius);
                        break;
                    case "paint":
                        if (tokens.length != 2) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        paint = Integer.parseInt(tokens[1]);
                        paint = (int) Maths.clamp(paint, 0, maxTemperature);
                        feedbackLabel.setText("Set flame brush paint to " + paint);
                        break;
                    case "reset":
                        if (tokens.length != 1) {
                            feedbackError("Invalid format");
                            success = false;
                            break;
                        }
                        updatePercentage = 0.4f;
                        maxTemperature = 50;
                        horizontalMotion = 1.25f;
                        dimmingFactor = 5.0f;
                        redFactor = 1.0f;
                        greenFactor = 0.35f;
                        blueFactor = 0.0f;
                        radius = 5;
                        paint = maxTemperature;
                        feedbackLabel.setText("Reset parameters to starting values");
                        break;
                    default:
                        feedbackError("Invalid command");
                        success = false;
                        break;
                }
            } catch (NumberFormatException exception) {
                feedbackError("Invalid format");
                success = false;
            }
            // command history is updated only in case of successfull command
            if (success) {
                // shift all the elements by one slot up in the history
                for (i = lastCommand - 1; i > 0 ; i--) {
                    commandsHistory[i] = commandsHistory[i - 1];
                }

                // set the first element to be the last command
                commandsHistory[0] = command;

                // increase the last command index
                lastCommand++;
                lastCommand = Math.min(lastCommand, COMMANDS_HISTORY_LENGTH);
            }
            updateParametersList(format);
        }
    }

    @Override
    public void windowSetup() {
        final int MAX_ROWS = 25;
        final int MAX_CHARS = 60;

        final String format = "Simulation latency: %.2f"
                            + "\nBase temperature: %d"
                            + "\nHorizontal motion: %.2f"
                            + "\nDimming factor: %.2f"
                            + "\nRed channel: %.2f"
                            + "\nGreen channel: %.2f"
                            + "\nBlue channel: %.2f"
                            + "\nFlame brush radius: %d"
                            + "\nFlame brush paint: %d"
                            + "\n\nCommands dictionary:"
                            + "\nlat <float> : changes simulation latency (ranged [0, 1])"
                            + "\ntemp <int> : changes base temperature (>= 0)"
                            + "\nhmove <float> : changes horizontal flame motion (>= 0)"
                            + "\ndim <float> : changes flame dimming (>= 1)"
                            + "\nred <float> : changes red channel (ranged [0, 1])"
                            + "\ngreen <float> : changes green channel (ranged [0, 1])"
                            + "\nblue <float> : changes blue channel (ranged [0, 1])"
                            + "\ncolor <float> <float> <float> : sets the color channels all at once"
                            + "\nradius <int> : changes the flame brush radius (>= 0)"
                            + "\npaint <int> : changes the flame brush paint (ranged [0, %d])"
                            + "\nreset : sets the parameters to the starting values"
                            + "\n\n\nMade by G3Dev";
        
        uiPanel = new JPanel();
		uiPanel.setLayout(new BorderLayout());
        
        // here are written all the parameter values
        parametersList = new TextArea("", MAX_ROWS, MAX_CHARS, TextArea.SCROLLBARS_NONE);
        parametersList.setEditable(false);
        parametersList.setFocusable(false);
        updateParametersList(format);
		uiPanel.add(parametersList, BorderLayout.NORTH);

        // a feedback label for commands output
        feedbackLabel = new JLabel();
        uiPanel.add(feedbackLabel);

        // command input text field
        commandLine = new TextField(MAX_CHARS);
        // command line placeholder text
        setCommandLinePlaceholder();
        commandLine.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {
                setCommandLinePlaceholder();
            }
            @Override
            public void focusGained(FocusEvent arg0) {
                commandLine.setForeground(java.awt.Color.BLACK);
                commandLine.setText("");
            }
        });
        // command line history interaction
        commandLine.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    if (lastCommand <= 1) return;
                    commandLine.setText(commandsHistory[commandsHistoryIndex]);
                    commandsHistoryIndex++;
                    commandsHistoryIndex = Math.min(commandsHistoryIndex, lastCommand - 2);

                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    if (commandsHistoryIndex == 0) {
                        commandLine.setText("");
                        return;
                    }
                    commandsHistoryIndex--;
                    commandsHistoryIndex = Math.max(0, commandsHistoryIndex);
                    commandLine.setText(commandsHistory[commandsHistoryIndex]);
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    parseCommand(format);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {}

            @Override
            public void keyTyped(KeyEvent e) {}
        });
		uiPanel.add(commandLine, BorderLayout.SOUTH);
		
        jFrame.add(uiPanel, BorderLayout.EAST);
    }

    @Override
    public void setup() {
        // create the grid
        grid = new int[SIZE][SIZE];

        // sets all cells to temperature 0
        for (y = 0; y < SIZE; y++) {
            for (x = 0; x < SIZE; x++) {
                grid[y][x] = 0;
            }
        }
    }

    @Override
    public void update() {
        // mouse flame paint
        if (input.isButtonDown(Input.LEFT_BUTTON)) {
            value = paint;
        } else {
            value = -1;
        }

        // update grid cells based on brush position
        if (value != -1) {
            pointerX = input.getMouseCanvasX();
            pointerY = input.getMouseCanvasY();

            for (y = -radius; y < radius; y++) {
                for (x = -radius; x < radius; x++) {
                    if (x*x + y*y <= radius*radius) {
                        xx = (int) Maths.clamp(x + pointerX, 0, SIZE - 1);
                        yy = (int) Maths.clamp(y + pointerY, 0, SIZE - 1);
                        grid[yy][xx] = value;
                    }
                }
            }
        }

        // update flame cells
        for (y = 0; y < SIZE; y++) {
            for (x = 0; x < SIZE; x++) {
                // we only update a grid cell with a percentage,
                // to give the simulation animation some juicy latency
                if (Math.random() >= updatePercentage) continue;
                
                // set the base temperature
                if (y == 0) {
                    value = maxTemperature;
                // set the other cells temperature to the temperature of the cell below
                // (directly underneath, to the left or to the right, established randomly based
                // on the horizontal motion parameter, the bigger the more chances there are to
                // pick the temperature from the cells on the sides rather than from the cell right
                // below the one)
                //
                // the temperature is decreased by a random factor based on the dimming factor
                // (the bigger the dimming factor the faster the temperature will go to zero)
                // the temperature is at most zero
                //
                // the color channels (red, green and blue) are obtained
                // by mapping the temperature from the [0, maxTemperature] range
                // to the [0, 255] color channel range
                // the channel color is then multiplied by the respective factor
                // to give the flame a custom hue
                } else {
                    // get a random offset based on the horizontalMotion parameter
                    xOffset = (int) ((Math.random() * 2 * horizontalMotion) - horizontalMotion);
                    // offset the position of the sampling cell below
                    actualX = x + xOffset;
                    // clamp the xOffset to avoid going outside the grid
                    actualX = (int) Maths.clamp(actualX, 0, SIZE - 1);
                    
                    // this number makes the flame go dimmer depending
                    // on the DIMMING_FACTOR constant
                    dimAmount = (int) (Math.random() * dimmingFactor);
                    
                    // dim the flame value
                    value = grid[y - 1][actualX] - dimAmount;
                    // clamp the value to make it ranged [0, maxTemperature]
                    value = Math.clamp(value, 0, maxTemperature);
                }
                // set the flame cell value
                grid[y][x] = value;
            }
        }
    }

    @Override
    public void render() {
        for (y = 0; y < SIZE; y++) {
            for (x = 0; x < SIZE; x++) {
                // map the temperature from [0, maxTemperature] range to [0, 255] color channel range
                value = Maths.map(grid[y][x], 0, maxTemperature, 0, 255);
                // multiply by the channel factors to give the flame a custom hue
                color.setRed((int) (value * redFactor));
                color.setGreen((int) (value * greenFactor));
                color.setBlue((int) (value * blueFactor));
                // set the pixel color
                screen.setPixel(x, y, color);
            }
        }
    }

    public static void main(String[] args) {
        Console.info("Run me with the \"-h\" argument to use high resolution rendering (there might be some lag)");
        boolean highResolution = false;
        if (args.length == 1) highResolution = args[0].strip().equals("-h");
        SIZE = 500 / (highResolution ? 1 : 10);
        new Fireplace().createCanvas("Fireplace", SIZE, SIZE, highResolution ? 1 : 10, Color.BLACK);
    }
}