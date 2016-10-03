package wheeloffortune;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author Phil O'Connell <pxo4@psu.edu>
 */
public class WheelOfFortune {

  // To read from the keyboard
  private static final Scanner _keyboard = new Scanner(System.in);

  // Used to get random values for puzzle and wheel
  private static final Random _random = new Random();

  // Winnings for user
  private static long _winnings = 0;

  // What wedge the player landed on
  private static String _wedgeValue = "";

  // True if we want to show all letters
  private static boolean _revealLetters = false;

  /*
   * These are the wedges that are part of the wheel.
   * There are 24.  Some values can appear more than once
   */
  private static final List<String> _wedges = Arrays.asList(
      /* 01 */"$5000",
      /* 02 */ "$600",
      /* 03 */ "$500",
      /* 04 */ "$300",
      /* 05 */ "$500",
      /* 06 */ "$800",
      /* 07 */ "$550",
      /* 08 */ "$400",
      /* 09 */ "$300",
      /* 10 */ "$900",
      /* 11 */ "$500",
      /* 12 */ "$300",
      /* 13 */ "$900",
      /* 14 */ "BANKRUPT",
      /* 15 */ "$600",
      /* 16 */ "$400",
      /* 17 */ "$300",
      /* 18 */ "LOSE A TURN",
      /* 19 */ "$800",
      /* 20 */ "$350",
      /* 21 */ "$450",
      /* 22 */ "$700",
      /* 23 */ "$300",
      /* 24 */ "$600"
  );

  /*
   * The number of wedges will not change throughout the game
   * We can cache the value so we're not calling .size() over and over
   */
  private static final int _wedgeCount = _wedges.size();

  // This is how much a vowel costs
  private static final int _vowelCost = 250;

  private static String chooseRandomWedgeValue() {
    // Choose a random index
    int randomWedgeIndex = _random.nextInt(_wedgeCount);

    // Return the corresponding wedge
    return _wedges.get(randomWedgeIndex);
  }

  // The menu choices
  private static final List<String> _menuChoices = Arrays.asList(
      "1. Spin the wheel",
      "2. Buy a vowel",
      "3. Solve the puzzle",
      "4. Quit the game"
  );
  private static final int _quitChoiceNumber = 4;

  // The different puzzles to choose from
  private static final List<String> _puzzles = Arrays.asList(
      "THE QUICK BROWN FOX JUMPS OVER THE LAZY DOG",
      "PENN STATE ABINGTON",
      "INFORMATION SCIENCES AND TECHNOLOGY",
      "CAT"
  );

  /*
   * The number of puzzles will not change throughout the game
   * We can cache the value so we're not calling .size() over and over
   */
  private static final int _puzzlesCount = _puzzles.size();

  /*
   * We will store the guessed letters in a hash map.
   * The "key" will be the character that was guessed
   * The "value" will be true/false
   *
   * Actually, the "value" aspect of this is not relevant.
   * Just the fact that a letter appears in the map as a key, is enough to imply
   * it was guessed.
   */
  private static Map<Character, Boolean> _guessedLetters = new HashMap<>();

  private static int checkLetterInPuzzle(char guessedLetter, String puzzle, boolean stopAtOne) {
    int count = 0;
    for (int i = 0; i < puzzle.length(); i++) {
      // Current letter
      char puzzleLetter = puzzle.charAt(i);
      if (guessedLetter == puzzleLetter) {
        count++;
        if (stopAtOne) {
          return count;
        }
      }
    }
    return count;
  }

  /*
   * Given a letter, determine if it's in the puzzle
   */
  private static int countLetterInPuzzle(char guessedLetter, String puzzle) {
    return checkLetterInPuzzle(guessedLetter, puzzle, false);
  }

  /*
   * Given a letter, determine if it's in the puzzle
   */
  private static boolean isLetterInPuzzle(char guessedLetter, String puzzle) {
    return (checkLetterInPuzzle(guessedLetter, puzzle, true) > 0);
  }

  /*
   * Given a puzzle, return a masked version, with hidden letters
   */
  private static String maskPuzzle(String puzzle, boolean revealLetters) {
    // Use a string builder, since Java strings are immutable
    StringBuilder maskedPuzzle = new StringBuilder();

    // For each letter in the puzzle
    for (int i = 0; i < puzzle.length(); i++) {
      // Current letter
      char c = puzzle.charAt(i);

      /*
       * Either we're revealing all letters, or we've already guessed the
       * letter
       */
      boolean isLetterGuessed = revealLetters || _guessedLetters.containsKey(c);

      /*
       * If the letter is not blank (we don't mask blanks), and the letter
       * has not been guessed, then we will mask it.
       */
      if (c != ' ' && !isLetterGuessed) {
        c = '_';
      }

      // Put one space after each character (even a space) in the puzzle
      maskedPuzzle.append(c + " ");
    }

    // Convert the string builder to a string and return it
    return maskedPuzzle.toString();
  }

  // Choose a random puzzle
  private static String chooseRandomPuzzle() {
    // Choose a random puzzle index
    int randomPuzzleIndex = _random.nextInt(_puzzlesCount);

    //Return the corresponding puzzle
    return _puzzles.get(randomPuzzleIndex);
  }

  // Determine if the given number choice actually appears on the menu
  private static boolean isValidMenuChoice(int choice) {
    if ((choice < 1) || (choice > _menuChoices.size())) {
      return false;
    }

    // Subtrace 1 because arrays/lists are zero-based
    int index = choice - 1;
    String menuText = _menuChoices.get(index);

    return !menuText.equals("");
  }

  // Input a letter from the keyboard
  private static char inputLetter(boolean allowAlreadyGuessedLetter) {
    char letter = ' ';
    boolean finished = false;

    while (!finished) {
      System.out.print("Enter a letter: ");

      String line = _keyboard.nextLine();
      if (line.length() != 1) {
        System.out.println("Enter just one letter");
      } else {
        // Convert letter to upper case
        letter = Character.toUpperCase(line.charAt(0));
        if (!Character.isLetter(letter)) {
          System.out.println("That is not a letter");
        } else {
          if (!allowAlreadyGuessedLetter && _guessedLetters.containsKey(letter)) {
            System.out.println("You already guessed that letter!");
          } else {
            // Will exit the loop
            finished = true;
          }
        }
      }
    }

    return letter;
  }

  private static boolean isLetterVowel(char letter) {
    // Find index of the letter within AEIOU
    int index = "AEIOU".indexOf(letter);

    // If found, then the index will be 0 or greater
    boolean isVowel = (index != -1);

    return isVowel;
  }

  // Determine if puzzle has been solved
  private static boolean isPuzzleSolved(String puzzle) {
    // For each letter in the puzzle
    for (int i = 0; i < puzzle.length(); i++) {
      char puzzleLetter = puzzle.charAt(i);
      // If this letter is not in the puzzle, then puzzle is not solved
      if (!_guessedLetters.containsKey(puzzleLetter)) {
        return false;
      }
    }
    return true;
  }

  // Allow user to solve the puzzle
  private static boolean solvePuzzle(String puzzle) {
    System.out.println("\nIF YOU GET ONE LETTER WRONG, YOU LOSE!!\n");

    while (!isPuzzleSolved(puzzle)) {
      System.out.println(maskPuzzle(puzzle, false));
      char letter = inputLetter(true);
      if (!isLetterInPuzzle(letter, puzzle)) {
        return false;
      }
      _guessedLetters.put(letter, true);
    }

    return true;
  }

  // Display the game menu, and handle the choices made
  private static void gameMenu() {
    // Choice from the menu
    int choice = 0;

    // Line entered from keyboard
    String line = "";

    // True when user wants to quit
    boolean quit = false;

    // Choose one of the puzzles at random
    String puzzle = chooseRandomPuzzle();

    // Repeat the menu until the user chooses to quit
    while (!quit) {
      System.out.println("                      ======================");
      System.out.println("                      =  Wheel Of Fortune  =");
      System.out.println("                      ======================");
      System.out.println("                                            ");

      System.out.println("Winnings: $" + _winnings);

      System.out.println(maskPuzzle(puzzle, _revealLetters));
      System.out.println();

      // Loop over the menu choices, and display each one
      for (String menuChoice : _menuChoices) {
        // Skip blank place-holder choices
        if (!menuChoice.equals("")) {
          System.out.println(menuChoice);
        }
      }
      System.out.print("Enter choice: ");
      line = _keyboard.nextLine();
      try {
        // If the input was not an integer, then that error will be caught
        choice = Integer.parseInt(line);
      } catch (NumberFormatException nfe) {
        // Error message, then go to the top of the loop
        System.out.println("Invalid input");
        continue;
      }

      // If not valid, then go back to the top of the loop
      if (!isValidMenuChoice(choice)) {
        System.out.println("Not a menu choice");
        continue;
      }

      System.out.println("You chose: " + _menuChoices.get(choice - 1));
      switch (choice) {
        case _quitChoiceNumber:
          // This will allow us to leave the menu loop
          quit = true;
          break;

        case 1: // Spin the wheel
          _wedgeValue = chooseRandomWedgeValue();
          System.out.println("You landed on: " + _wedgeValue);
          // If user landed on a dollar-value wedge
          if (_wedgeValue.equals("BANKRUPT")) {
            System.out.println("Your money is gone!");
            _winnings = 0;
          } else {
            if (_wedgeValue.startsWith("$")) {
              char letter = ' ';

              // Set to false to ensure we get in the loop
              boolean guessedValidLetter = false;

              // Keep asking letter until user enters one that has not been guessed already
              while (!guessedValidLetter) {
                // Get not-already-guessed letter from the keyboard
                letter = inputLetter(false);
                System.out.println("Your letter is: " + letter);

                if (isLetterVowel(letter)) {
                  // Output an error
                  System.out.println("That is a vowel!");
                  System.out.println("Guess again");
                } else {
                  // If the letter has NOT been guessed, this will allow us to leave the loop
                  guessedValidLetter = true;
                }
              }
              // This letter has now been guessed
              _guessedLetters.put(letter, true);
              if (isLetterInPuzzle(letter, puzzle)) {
                System.out.println("Correct!");

                // Get rid of the leading dollar sign
                String justDollarAmount = _wedgeValue.substring(1);

                // Convert to integer
                int wedgeMoney = Integer.parseInt(justDollarAmount);

                // Add to winnings
                int count = countLetterInPuzzle(letter, puzzle);
                System.out.println(letter + " appears " + count + " times");
                _winnings += wedgeMoney * count;
              }
            }
            break;
          }

        case 2: // Buy a vowel
          if (_winnings < _vowelCost) {
            System.out.println("You need at least $" + _vowelCost);
          } else {
            _winnings -= _vowelCost;
            boolean isVowel = false;
            char letter = ' ';
            while (!isVowel) {
              letter = inputLetter(false);
              isVowel = isLetterVowel(letter);
              if (!isVowel) {
                System.out.println("That is not a vowel.");
              }
            }
            // This letter has now been guessed
            _guessedLetters.put(letter, true);
          }
          break;

        case 3: // Solve the puzzle
          if (solvePuzzle(puzzle)) {
            System.out.println("You just won $" + _winnings + "!!");
          } else {
            System.out.println("YOU GET NOTHING!");
            System.out.println("YOU LOSE!");
            System.out.println("GOOD DAY, SIR!");
            _winnings = 0;
          }
          quit = true;
      }
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    gameMenu();
  }

}
