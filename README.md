# RINGwars Agent

This repository contains the Java agent I developed for the module
*Grundpraktikum Programmierung* at the FernUniversität in Hagen.

RINGwars is a turn-based strategy game. The agent reads the current game
state, evaluates possible actions and chooses moves based on reactive and
heuristic strategies.

The project was graded 1.0 and the agent reached the final round of the
course tournament.

## Implementation

The project is written in Java 21 and follows a modular object-oriented
structure.

The main parts are:

- `feu.agenten.shogo_seifert`: game model, input handling and agent control
- `moveCategories`: generation of possible move types
- `strategyDeciders`: selection and evaluation of strategies

During development, I tested several approaches and revised the structure
multiple times. My main goal was to keep the decision logic understandable
and to separate the different responsibilities clearly.

## Documentation

The generated Javadoc is located in the [`doc`](doc/) directory and can be
opened locally via `doc/index.html`.

## Eclipse

The repository contains the Eclipse project files. It can be imported through:

`File → Import → Existing Projects into Workspace`

Java 21 is required.

## Note

The agent was developed for the RINGwars tournament environment used in the
programming practicum. This repository contains the agent implementation, but
not the complete tournament environment.

## Author

Shogo Seifert

