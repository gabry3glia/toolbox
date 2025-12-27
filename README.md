# ToolBox
A Java library for creating fast and simple sketches.

## Table of Contents
+ [Sketch options](#sketch-options-)
+ [Input](#input-)
+ [Drawing](#drawing-)
+ [Creating your sketch](#creating-your-sketch-)
+ [To-Do](#to-do-)

## Sketch options [#](#table-of-contents)
The sketch can be set up in many ways: change the tickrate (update rate), framerate (frames per second).\
Toggle the automatic screen clearing with `autoClear(boolean toggle)`.

The sketch window can be further customized by changing its title, size and the canvas pixel scale (the amount of actual screen pixels per canvas pixel).

## Input [#](#table-of-contents)
This toolbox comes in with easy to use input methods:
+ `boolean isKeyPressed(int key)`: returns true only on the key press, then returns false also while holding it
+ `boolean isKeyDown(int key)`: returns true when the key is pressed, even while holding it
+ `boolean isKeyReleased(int key)`: returns true only on the key release
There are also the mouse methods:
+ `boolean isButtonPressed(int button)`: returns true only on the button press, then returns false also while holding it
+ `boolean isButtonDown(int button)`: returns true when the button is pressed, even while holding it
+ `boolean isButtonReleased(int button)`: returns true only on the button release
The keyboard methods require a `java.awt.event.KeyEvent.VK_keycode` number, while the button methods require `Input.button_BUTTON`.
There are also wheel scroll and mouse cursor position detection methods:
+ `int getMouseScroll()`: returns the mouse scroll wheen amount and direction
+ `int getMouseX()`: returns the cursor x position in canvas space, ranged [0, canvas width)
+ `int getMouseY()`: returns the cursor y position in canvas space, ranged [0, canvas height)
+ `int getCanvasMouseX()`: returns the cursor x position in canvas space taking into account pixel scale and screen translation
+ `int getCanvasMouseY()`: returns the cursor y position in canvas space taking into account pixel scale and screen translation
+ `int getMouseDeltaX()`: returns the cursor x movement
+ `int getMouseDeltaY()`: returns the cursor y movement

## Drawing [#](#table-of-contents)
You can draw in the canvas thanks to the `screen` component.\
It holds some methods for drawing single pixels, points, lines, rectangles, squares, triangles, vectors and much more!\
It is possible to render an image you loaded via `Image(String path)` with the `screen.image(Image image)` method; and you can overlay screens together with `screen.overlay(Screen screen, int...)`
You can also set the outline and fill colors, the brush shape and the stroke width.

You can translate the screen, thus moving the coordinate system origin to a custom position by calling `screen.translate(int x, int y)` and reset the translation with `screen.resetTranslation()`. There is also the `screen.translateToCenter()` method, which moves the origin to the exact center of the canvas.

You can also set a custom padding via the `screen.padding(...)` methods.

To change the fill and outline colors you should call `screen.fill(Color color)` and `screen.outlines(Color color)` respectively, while `screen.disableFill()` and `screen.disableOutlines()` can be used to disable shape filling and outline drawing.

Set the screen clear color with `screen.background(Color color)` and manually clear the screen at anytime you want with `screen.clear()` and `screen.clear(Color color)` to also specify a clear color different from the background color.

## Creating your sketch [#](#table-of-contents)
In order to create your own sketch you have to make a new Java class and extend it to the `Sketch.java` class.\
There will be three methods to implement and override: `setup()` (called once before updating and rendering for the first time), `update()` (called once every tick), `render()` (called once every frame and always after `update()`).

The final frame is displayed after the `render()` call.

If auto-clear is enabled, `screen.clear()` will be called between `update()` and `render()`, meaning if you draw something to the canvas in the `update()` method, this will be erased before being actually shown.

An additional method called `windowSetup()` exists, but it is not mandatory to override and implement it. It is called once before the actual window (JFrame) is shown, meaning you can use it to add AWT components to the sketch window for more advanced UI creations.

You can find a sketch template [**here**](https://github.com/G3Dev-0/toolbox/blob/main/Template.java).

## Code Examples: Fireplace
An example sketch ([Fireplace.java](https://github.com/G3Dev-0/toolbox/blob/main/Fireplace.java)) has been provided in the repository.\
It implements a more complex UI as well as some cellular automata features along with some randomness in order to create a pleasant flame effect.\
There is a grid where each cell has a temperature value that gets changed depending on the values below it. Each cell is rendered as a pixel with a color that depends on that cell temperature.\
It works by setting the base pixels all to the same temperature and then fading it as you go up along the vertical axis of the grid.\
Every time we go up by a cell we take the temperature value from a cell below that can be either right beneath the current cell or to the left/right of it, depending on the value assigned to the parameter called "Horizontal motion", then we decrease the taken temperature by a random amount (depending on the "Dimming factor") and finally assign the result to the current cell.

<img src="fireplace.png" title="Fireplace" width="700"/>

## To-Do [#](#table-of-contents)
+ Improve sounds implementation
+ Don't support
+ Document the whole code
+ Add compilation instructions to this README file
