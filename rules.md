# PROJECT RULES
This is a project of rules and regulations! Read this document thoroughly before
starting work on the project!
As a friendly reminder, failure to follow the rules means I will hunt you down and [DATA EXPUNGED].

I reserve the rights to change, update, nullify, or interpret any of the rules at any time, for any reason.

### Table of contents
- [Docstring Rules](#docstring-rules)
- [FXML Rules](#fxml-rules)
- [CSS Rules](#css-rules)
- [Method Rules](#method-rules)
- [Method Naming Conventions](#method-naming-conventions)
- [Class Structure Rules](#class-structure-rules)
- [Final Remarks](#final-remarks)




## Docstring Rules
All docstrings must follow the template given below:  

**BEGIN DOCSTRING FORMAT**  
One-line summary of the method or class, followed by two `<br>` tags.

Additional details of the method or class. This may span multiple lines.  
If each line exceeds ~80 characters (where a significant portion of the text crosses the thin white line on the right of the screen),
any additional text must be on a new line.  
Any new lines must use the `<br>` tag.

One or two lines of important notice for clients, i.e. preconditions not enforceable by exceptions.  
This section must contain "Helper to {parent method name}" if the method is a private helper to another method.
- If an important notice section is included, the additional details section must end with two `<br>` tags.
- If the method modifies an input, the information must be provided here.

@param parameter1 the first parameter, followed by a period. Then state any preconditions on the parameter  
@param parameter2...  
@return some value. Return value descriptions are required! Must come immediately after the parameters  
@throws Exception... Newly thrown exceptions are required!
Exception description is not needed if throwing an Exception that an overridden function throws
(i.e. ExecutionException and InterruptedException in a Task), or if the Exception is unlikely to be thrown 

**END DOCSTRING FORMAT**

Clarity is more important than strict grammatical adherence.

NOTE: All parameter, return value, and exception entries must have a description, even if the entries are trivial or redundant!

Class docstrings need not include parameters, return values, or exception information.
Docstrings for fields can contain only a one-line summary.





## FXML Rules
These rules apply, even if Scene Builder is used!

A parent container is any JavaFX object that dictates the layout of objects inside it. Example: VBox

A parent container's FXML declaration must be in the following format:
1. Open angled bracket.
2. Characteristics of the container, listed in alphabetical order, followed by a newline.**
3. URLs to important resources, followed by a newline.*
4. Package and name of controller*
5. FX ID, if applicable*

*These lines must be 1-level indented.   
**May be split by a newline if excessively long. If split, all lines except for the first must be indented.

Examples:  

    <VBox alignment="CENTER_LEFT" prefHeight="500" prefWidth="1000" spacing="30.0" style="-fx-background-color: #aaaaaa;"
        xmlns="sample-url" xmlns:fx="sample-url"    
        fx:controller="stepperfx.controllers.LoginController" fx:id="background">
**^ Notice the indentation on the second and third lines.**

    <VBox alignment="CENTER_LEFT" prefHeight="500" prefWidth="1000"  
        spacing="30.0" style="-fx-background-color: #aaaaaa;"  
        xmlns="sample-url" xmlns:fx="sample-url"  
        fx:controller="stepperfx.controllers.LoginController" fx:id="background">


All JavaFX objects inside a container
must be separated by **1 level** of indentation.

Objects arranged inside containers must be arranged
from top to bottom, left to right. 
Example:  

    <Container>
        <Top>
        ...  
        <Top/> 
        
        <Left> 
        ...  
        <Left/>
        
        <Bottom>  
        ...  
        <Bottom/>
    <Container/>


The FXML defining individual JavaFX elements must be separated by one new line.  
Attributes of JavaFX objects, such as `Font`, should not be separated by newlines.  
Example (notice the line spacing between the two ExampleObjects):

    <Container>
        <ExampleObject option1=... option2=...>
            <Attribute... />
        <ExampleObject/>
    
        <ExampleObject option1=... option2=...>
            <Attribute... />
        <ExampleObject/>
    <Container/>

At the start of the FXML object's options should be the `fx:id`, followed by the `onAction` option (if either exist).  
Options for FXML objects must be listed in alphabetical order (or in some other logical order).  
Examples:  

    <ExampleObject fx:id="exampleObject" onAction="#doSomething" option1=... option2=...  />  
    <ExampleObject fx:id="exampleObject2" option1=... option2=.../>
    <ExampleObject onAction="#doSomethingElse" option1=... option2=.../>

If the options exceed ~90 characters, the FXML value should have a line wrap.
The line wrap should be on the same level of indentation, or one level in, as the rest of the object.
Lines may wrap if the options are shorter than 90 characters.  
Example:  
```
<ExampleObject option1=... option2=... option3=... option4=... option5=...  
option6=... option7=... option8=... />
```




## CSS Rules

Custom style classes should appear at the top of the CSS file, in alphabetical order by class name.  
Below should be 3-4 blank lines.  
Preset style classes for GUI components (i.e. Button, CheckBox) should appear below the custom style classes and blank lines, in alphabetical order by class name.  




## Method Rules

Unless otherwise specified, all methods must not modify any of their inputs. Modifications must occur on **deep** defensive copies.  
If a method modifies its input, the modification must be explicitly stated in the method docstring
as the docstring's last line of text.

If a class constructor takes a non-primitive type as an input, the constructor must load the corresponding field with a
deep copy of the input. (Strings do not need to have defensive copies)

All methods must assert their preconditions. If a precondition is broken, the method should throw an AssertionError with a descriptive message.



## Method Naming Conventions

Method names that start with the following words should do their associated actions:

- `fetch...`-- Retrieves an external resource, such as a file.
  Returns the resource as its **native type**, i.e. as a File or Image
- `get...`-- Accesses a private variable.
- `read...`-- Returns the contents of an external resource as a **String**.
  If the resource is not returned as a String, use `fetch...` instead.
- `set...`-- Changes the value of **exactly one** private variable.
- `show...`-- Changes the user interface, i.e. displays another screen, shows a dialog
- `take...`-- Retrieves a value from the user interface.
- `write...`-- Takes a String as one of its inputs. Writes to an external resource with the given String.

If the method does anything else, the method's name should not start with any of the above words.



## Class Structure Rules

Classes must match the template below.  

There are lines of comments separating the different sections. The lines should appear in the class definition.
The number of comment lines may vary. Longer classes should have more comment lines separating each section.  
Comment lines are not necessary if the class is short. A good measure is if at least half of the class definition
can fit on your screen.

All methods must conform to the [Method Rules](#method-rules).

Underneath each row of comment lines is a section header. The section header should also appear in the class definition.  

Example of comment lines with header:  
// ////////////////////////////////////////////////  
//HEADER NAME

A class does not need to have all the listed sections.

**BEGIN CLASS FORMAT**

Docstring that conforms to the [Docstring Rules](#docstring-rules)   
NOTE: Since class definitions have no inputs or return values, 
a class docstring does not include any parameters, return values, or exceptions.


Constants, whose names are in all caps, with a descriptive name.
Must be declared in a logical order. If no logical order exists, declare in alphabetical order by variable name.  
Example:  

    final String DEFAULT_INPUT = "Hello world";
    final int MAX_HEIGHT = 1000;


// ////////////////////////////////////////////////////////////  

Variable fields  
Must be declared in a logical order. If no logical order exists, declare in alphabetical order by variable name.


NOTE: All constant and variable fields MUST HAVE DOCSTRINGS!

// ////////////////////////////////////////////////////////////

FXML variable fields, i.e. those annotated with `@FXML`  
Must be declared in a logical order. If no logical order exists, declare in alphabetical order 
by datatype, then by variable name.  
All FXML fields must be private.  

Example:

    /**
    * Docstring...
    */
    @FXML
    private Button initializerButton;

    /**
    * Docstring...
    */
    @FXML
    private Button userInputButton;

    /**
    * Docstring...
    */
    @FXML
    private Label buttonText;


// ////////////////////////////////////////////////////////////  
//CONSTRUCTOR

Class constructor(s), or equivalent method to instantiate the class. If the class has multiple constructors, 
the most frequently used constructors are first.  
ALL CLASSES must have an explicitly defined constructor, even if the constructor does nothing.
Exception: subclasses do not need explicitly defined constructors if the superclass has a defined constructor.

// ////////////////////////////////////////////////////////////  
//HELPERS

Any private helper methods that other class methods may use.  
Also included here are utility methods (i.e. `toString`).

All must have docstrings and be organized in alphabetical order by method name.

// ////////////////////////////////////////////////////////////  
//SINGLE ABSTRACT METHOD

If the class has a single abstract method, the method should be implemented here.

// ////////////////////////////////////////////////////////////  
//GETTERS AND SETTERS

Getter and setter methods 

For any encapsulated field, the field's getter method must come before the setter method.  
All must have docstrings and be organized in alphabetical order by the name of the field manipulated.  
Example of alphabetical organization (without docstrings, to save space):

    public void getA()...
    public void setA()...
    public void getB()...
    public void setB()... 

Short getters and setters may be separated by 1-2 blank lines.

// ////////////////////////////////////////////////////////////  
//METHODS

Additional non-main methods   
All additional methods should be in alphabetical order by the method's name.

Private helper methods for a single method may go immediately below the parent method.
They may break the alphabetical order rule.  
These methods must have a message in their docstrings that says "Helper to {parent method name}"

All methods must be separated by 2 or 3 blank lines.    
Whether to separate by 2 or 3 blank lines is the writer's choice,
unless the class has more than 10 methods (here, the constructor counts as a method). If so, 
methods must be separated by 3 blank lines.  
Getters and setters of the same field, methods for unit tests, or overloads of the same method
may be separated by 1 or 2 blank lines.

If a method is overloaded, the method that takes the least parameters should be first.
Overloaded methods are ideally separated by 1-2 blank lines.

// ////////////////////////////////////////////////////////////  
// ////////////////////////////////////////////////////////////  

Main method: `public static void main(String[] args) {...}`

**END CLASS FORMAT**


**Example Class**

    /**
      * Controller for the sample screen (which does not really exist).
      */
      public class SampleScreenController extends BaseController {
    
      // ////////////////////////////////////////////////////
      //CONSTANTS
    
      /**
        * Message to be sent to the user, if none is provided
        */
          final private String DEFAULT_MESSAGE = "hello world";
    
      /**
        * Maximum length of the user input
        */
          final private int MAX_LENGTH = 100;
    
    
        // ////////////////////////////////////////////////////
        //VARIABLES
    
        /**
         * Number of attempts given to the user
         */
        private int attempts = 3;
    
        /**
         * Number of clicks on the screen
         */
        private int nClicks = 0;
    
        /**
         * IDs of previous users
         */
        private int[] previousUserIDs = {0};
    
        // ////////////////////////////////////////////////////
        //FXML VARIABLES
    
        /**
         * Button for the user to exit the screen
         */
        @FXML 
        private Button exitButton;
    
        /**
         * Button for the user to enter an input
         */
        @FXML
        private Button inputButton;
    
        /**
         * Allows the user to enter an input
         */
        @FXML
        private TextArea textInput;
        
        
        // ////////////////////////////////////////////////////
        // CONSTRUCTOR
    
        /**
         * Creates and initializes a new controller.<br><br>
         * 
         * This method is implicitly called on app startup.
         */
        public SampleScreenController() {
            // Not defined
        }
    
        // ////////////////////////////////////////////////////
        // HELPERS
    
        /**
         * Calculates the function 2x+1.
         * @param input the given value for `x`
         * @return result of `2x+1`
         */
        private double calc(float input) {
            return 2*input + 1;
        }
        
        
        // ////////////////////////////////////////////////////
        // GETTERS AND SETTERS
    
        /**
         * Returns the number of attempts that the user took
         * @return number of attempts
         */
        public int getAttempts() {
            return attempts;
        }
    
        /**
         * Sets the number of attempts given to the user to {@code newAttemptValue}.
         * @param newAttemptValue new number of attempts. Cannot be negative.
         */
        public void setAttempts(int newAttemptValue) {
            if(newAttemptValue < 0) throw new AssertionError("New attempt value cannot be negative");
            attempts = newAttemptValue;
        }
    
    
        /**
         * Returns the number of times that the user clicked
         * @return number of clicks
         */
        public int getNClicks() {
            return nClicks;
        }
    
        /**
         * Sets the number of clicks that the user made to {@code newNClicks}.
         * @param newNClicks new number of clicks. Cannot be negative.
         */
        public void setNClicks(int newNClicks) {
            if(newNClicks < 0) throw new AssertionError("New number of clicks cannot be negative");
            nClicks = newNClicks;
        }
    
    
        /**
         * Returns a deep copy of the previous user IDs.
         * @return list of user IDs
         */
        public int[] getPreviousUserIDs() {
            int[] output = new int[previousUserIDs.length];
            System.arraycopy(previousUserIDs, 0, output, 0, previousUserIDs.length);
            return output;
        }
    
        /**
         * Sets the list of previous user IDs to {@code newValue}.
         * @param newValues new value to set
         */
        public void setPreviousUserIDs(int[] newValues) {
            int[] inputDeepCopy = new int[newValues.length];
            System.arraycopy(newValues, 0, inputDeepCopy, 0, previousUserIDs.length);
            previousUserIDs = inputDeepCopy;
        }
    
    
        // ////////////////////////////////////////////////////
        // METHODS
    
        /**
         * Exits the screen.
         */
        @FXML 
        public void exit() {
            super.exit();
        }
    
    
        
        /**
         * Returns the contents of the manual page stored in {@code BaseController.MANUAL_DIRECTORY}.
         * @return contents of manual page
         */
        @Override 
        public String readManualPage() {
            inputButton.setDisable(true);
            ManualPage page = super.fetchManualPage();
            String output = page.toString();
            inputButton.setDisable(false);
            return output;
        }
    
        
        
        /**
         * Retrieves the user input from the text area and prints the result.<br>
         * If the user input is longer than {@code SampleScreenController.MAX_LENGTH}, 
         * prints {@code SampleScreenController.DEFAULT_MESSAGE} instead.
         */
        @FXML 
        public void takeUserInput() {
            if(textInput.getText().length() > MAX_LENGTH) {
                System.out.println(DEFAULT_MESSAGE);
            }
            else {
                System.out.println(textInput.getText());
            }
        }
    
    }

## Final Remarks
Curran Muhlburger is not allowed to contribute to any documentation in this project.

No one working on the project is allowed to make references to K-Pop Demon hunters or the "6 7" meme.

You agree that 3 is a good enough approximation for π (ratio of a circle's circumference to its diameter) and e (Euler's number).

If you disagree with any of the rules, click [here](https://www.google.com/url?sa=t&source=web&rct=j&opi=89978449&url=https://www.youtube.com/watch%3Fv%3DxvFZjo5PgG0).

[Back to table of contents](#table-of-contents)