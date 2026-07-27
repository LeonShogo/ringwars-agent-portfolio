# RINGwars Java Agent

Autonomous Java agent developed as an individual project for the programming practicum at the FernUniversität in Hagen.

The project implements a modular, object-oriented agent for the turn-based strategy game RINGwars. The agent analyses the current game state, manages limited resources and selects suitable actions using reactive and heuristic decision strategies.

The project received a grade of **1.0** and reached the final round of the agent tournament.

## Key Features

- Modular object-oriented architecture
- Analysis and representation of game states
- Management of limited resources
- Reactive and heuristic decision strategies
- Separate strategy and move categories
- Validation and generation of rule-compliant moves
- Documented source code and generated Javadoc

## Technologies

- Java 21
- Java Standard Library
- Eclipse IDE
- Javadoc

## Project Structure

- `src/feu/agenten/shogo_seifert/`: Core agent, game model and utilities
- `src/moveCategories/`: Different categories of possible moves
- `src/strategyDeciders/`: Strategy selection and test implementations
- `doc/`: Generated Javadoc documentation

## Documentation

The generated Javadoc is available in [`doc/index.html`](doc/index.html).

The primary agent class is:

`feu.agenten.shogo_seifert.RINGwars_7172397_Shogo_Seifert`

## Importing into Eclipse

1. Clone or download the repository.
2. Open Eclipse.
3. Select **File → Import → Existing Projects into Workspace**.
4. Select the repository directory.
5. Use a Java 21 compatible JDK.

## Background

This project was developed independently as part of the module *Grundpraktikum Programmierung*. During development, several approaches were tested, evaluated and revised. The main focus was on creating a stable architecture and making the agent's decisions understandable and maintainable.

## Author

Shogo Seifert
