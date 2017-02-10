MIT License applies to this software.

This program was inspired by PPCG user PhiNotPi's code-golf challenge. It asks 
to simulate a "gravity-based billiard ball machine". Here are the exact specs:

The program is first read entirely from STDIN, and then it is run row by row. Each character represents its own part of the code, and the program is entirely interpreted. Any character after and including the first occurrence of '#' on a line will be ignored. The textual machine consists of lowercase letters, uppercase letters, and the characters \\_/^<>ID,.P. The virtual machine consists of balls, ramps, logic operators, and outputs.

The interpreter will first begin by interpreting the first row.

A lowercase letter will result in the interpreter taking an input. If the input is not '0' after being trimmed of whitespace, then a ball is released at that location with an integer value of the input.

An uppercase letter is an output. Every time a ball passes through its space, it will increment its value (by 1). At the end of the execution, all outputs will be printed in order of row first, then column.

The characters \\ and / are ramps. If a ball falls onto it, it will deflect it one space right/left, and set its direction to +1/-1. If a ball comes sideways onto it, its direction will be set to 0 and it will fall down. If a ball levitates up from underneath it, it will be deflected one space left/right and its direction will be set to -1/+1.

The character _ is a logic operator. Whenever a ball passes over its space, it will increase its value by 1. At the end of one iteration of all balls, if there are no balls on its layer, then if its value is positive and even, it will create a new ball with a non-conflicting ID in the space directly underneath it, and set its value back to 0. If a ball falls straight down onto it, the ball will be destroyed. If a ball rolls over it in either direction, it will continue. If a ball levitates up through it, it will keep levitating upwards.

The character ^ sets a ball's direction to 0 and makes it begin levitating.

The characters < and > "inject" the ball's value into whatever is to its left/right. Essentially, if a ball with value X reaches a control operator with metadata X, the operator will do nothing. This allows for condition statements, albeit rather confusing. The ball whose value was used is then destroyed.

I and D increment/decrement a ball's value.

, prompts for the user's input and sets it as the ball's value; this is equivalent to destroying the ball and creating a new ball at that space with the same name.

. outputs the ball's value as (char) value.

P outputs the ball's value as an integer.

Levitation is started only by the character ^ and is stopped by a ramp.

Balls cannot collide. Execution stops once all balls have left the specified grid.

All strings that are shorter than the longest string will be padded with spaces on the right so that it is a rectangular grid.

All other characters will allow the ball to pass through them; if it is falling, it keeps falling. If it is rolling, it stops rolling and falls straight down (there is not horizontal momentum in this language). If it is levitating, it keeps levitating straight up.