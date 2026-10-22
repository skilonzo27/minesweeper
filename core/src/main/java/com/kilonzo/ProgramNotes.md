# Program Notes
## The board as integers
- 0-8: number of neighboring bombs (uncovered tiles)
  - Draw a blank tile
- -1: represents a hidden bomb
  - Draw a blank tile
- 10-18: uncovered states of 0-8
  - Draw the actual title value (draw value - 10)
- 9: clicked bomb
  - Game over
- 19-28: a flagged tile
  - Draw a flagged tile
  - BUT....we know what is underneath the flag (value-20)

## User input
- left-click: +10 to tile
  - They uncover a tile
- right-click: +20/-20 to a tile
  - the flagged/unflagged a tile

## Completing the Project
~~1. Finish the flood fill Algorithm~~
~~2. When a bomb is clicked on, show all the bombs (game over)~~
~~3. Finish the win condition~~
~~4. Restart the game~~
~~5. Add sound / music~~
   ~~- Use an A.I. to walk you through the code~~
   ~~- Learn as you go, DO NOT HAVE THE  AI simply complete it~~
~~6. Personalize graphics, feel of the game~~
7. Use an A.I. and feed in your entire code base
   - Ask for improvements
   - If you make improvements, tag them and keep your OG code
