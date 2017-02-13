# MIT License applies to this software.

This program was inspired by PPCG user PhiNotPi's code-golf challenge. It asks 
to simulate a "gravity-based billiard ball machine". Here are the exact specs:

The program is first read entirely from STDIN, and then it is run row by row. Each character represents its own part of the code, and the program is entirely interpreted. Any character after and including the first occurrence of `'#'` on a line will be ignored. The textual machine consists of lowercase letters, uppercase letters, and the characters `\_/^←→↑↓↧⇓⇩.↥+-*<>≤≥⋀$`. The virtual machine consists of balls, ramps, logic operators, and outputs.

The interpreter will first begin by interpreting the first row.

If the character is `⇓`, an input will be taken, parsed to an integer, and stored into a new ball.

If the character is `⇩`, an input will be taken, and each character in the input (excluding the newline) will be stored into a new ball.

An uppercase letter is an output. Every time a ball passes through its space, it will increment its value (by 1). At the end of the execution, all outputs will be printed in order of row first, then column.

The characters `\` and `/` are ramps. If a ball falls onto it, it will deflect it one space right/left, and set its direction to `+1/-1`. If a ball comes sideways onto it, its direction will be set to `0` and it will fall down. If a ball levitates up from underneath it, it will be deflected one space left/right and its direction will be set to `-1/+1`.

The character `_` is a logic operator. Whenever a ball passes over its space, it will increase its value by `1`. At the end of one iteration of all balls, if there are no balls on its layer, then if its value is positive and even, it will create a new ball with a non-conflicting ID in the space directly underneath it, and set its value back to `0`. If a ball falls straight down onto it, the ball will be destroyed. If a ball rolls over it in either direction, it will continue. If a ball levitates up through it, it will keep levitating upwards.

The character `^` sets a ball's direction to `0` and makes it begin levitating.

The characters `<` and `>` "inject" the ball's value into whatever is to its left/right. Essentially, if a ball with value `X` reaches a control operator with metadata `X`, the operator will do nothing. This allows for condition statements, albeit rather confusing. The ball whose value was used is then destroyed.

`↑` and `↓` increment/decrement a ball's value.

`↧` prompts for the user's input and sets it as the ball's value; this is equivalent to destroying the ball and creating a new ball at that space with the same name.

`.` outputs the ball's value as `(char) value`.

`↥` outputs the ball's value as an integer.

Levitation is started only by the character `^` and is stopped by a ramp.

`+`, `-`, and `*` will consume a ball and store its value on the first ball, and then on the second ball, will consume it and apply that operator to both of them. A new ball with this value is released right under it. It will then reset its memory and reset its state.

`<`, `>`, `≤`, `≥`, and `=` will compare the values of two balls (first in first), and then will release a ball if the comparison is true.

Upon exiting, each space with `$` will report how many times a ball passed over it, starting at (0, 0) and working by rows.

Balls cannot collide. Execution stops once all balls have left the specified grid.

All strings that are shorter than the longest string will be padded with spaces on the right so that it is a rectangular grid.

All other characters will allow the ball to pass through them; if it is falling, it keeps falling. If it is rolling, it stops rolling and falls straight down (there is not horizontal momentum in this language). If it is levitating, it keeps levitating straight up.

# Interpreter Flags

`--ignore-states -i` supresses output from capital-letters.  
`--debug -d` prints all of the balls in the system and requires the user to press <Enter> each time before all balls have been iterated.  
`--wait -w <ms>` waits `ms` milliseconds before all balls are iterated once.