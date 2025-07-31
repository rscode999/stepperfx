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
- [Class Structure Rules](#class-structure-rules)
- [Final Remarks](#final-remarks)




## Docstring Rules
All docstrings must follow the template given below:  

##### BEGIN DOCSTRING FORMAT
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
Exception description is not needed if throwing an Exception that an overridden function throws,
or if the Exception is unlikely to be thrown (i.e. ExecutionException and InterruptedException in a Task)  

##### END DOCSTRING FORMAT

NOTE: All parameter, return value, and exception entries must have a description, even if the associated values are trivial!


Class docstrings need not include parameters, return values, or exception information.
Docstrings for fields can contain only a one-line summary.





## FXML Rules
These rules apply, even if Scene Builder is used!

The parent container's FXML declaration must be in the following format:
- Open angled bracket.
- Characteristics of the container, listed in alphabetical order, followed by a newline.**
- URLs to important resources, followed by a newline.*
- Package and name of controller*
- FX ID, if applicable*

*These lines must be indented. 
**If the characteristics are excessively long, the attributes may be split by a newline.

Examples:  

    <VBox alignment="CENTER_LEFT" prefHeight="500" prefWidth="1000" spacing="30.0" style="-fx-background-color: #aaaaaa;"
        xmlns="sample-url" xmlns:fx="sample-url"    
        fx:controller="stepperfx.controllers.LoginController" fx:id="background">
**^ Notice the indentation on the second and third lines.**

    <VBox alignment="CENTER_LEFT" prefHeight="500" prefWidth="1000"  
        spacing="30.0" style="-fx-background-color: #aaaaaa;"  
        xmlns="sample-url" xmlns:fx="sample-url"  
        fx:controller="stepperfx.controllers.LoginController" fx:id="background">


Objects arranged inside containers must be arranged
from top to bottom, left to right.  
Example:  

    <Top>
    ...  
    <Top/> 
    
    <Left> 
    ...  
    <Left/>
    
    <Bottom>  
    ...  
    <Bottom/>


All objects or attributes contained within another object
must be separated by **1 level** of indentation.

The FXML defining individual GUI elements must be separated by one new line.  
Attributes, such as Font, should not be separated by newlines.

Example:

    <Container>
        <ExampleObject option1=... option2=...>
            <Attribute... />
        <ExampleObject/>
    
        <ExampleObject option1=... option2=...>
            <Attribute... />
        <ExampleObject/>
    <Container/>

At the start of the FXML object's options should be the `fx:id`, followed by the `onAction` option.  
Options for FXML objects must be listed in alphabetical order (or in some other logical order).
Examples:  

    <ExampleObject fx:id="exampleObject" onAction="#doSomething" option1=... option2=...  />  
    <ExampleObject fx:id="exampleObject2" option1=... option2=.../>
    <ExampleObject onAction="#doSomethingElse" option1=... option2=.../>

If the options exceed ~90 characters, the FXML value should have a line wrap.
The line wrap should be on the same level of indentation, or one level in, as the rest of the object.
Lines may wrap if the options are shorter than 90 characters.
Example:  
`<ExampleObject option1=... option2=... option3=... option4=... option5=...  
option6=... option7=... option8=... />`




## CSS Rules

Custom style classes should appear at the top of the CSS file, in alphabetical order by class name.  
Below is 3-4 blank lines.  
Preset style classes for GUI components (i.e. Button, CheckBox) should appear below the custom style classes and blank lines, in alphabetical order by class name.  

All style components inside a CSS entry should appear in some logical order, most often alphabetical order.




## Method Rules

Unless otherwise specified, all methods must not modify any of their inputs. Modifications must occur on **deep** defensive copies.  
If a method modifies its input, the modification must be explicitly stated in the method docstring
as the docstring's last line of text.

If a class constructor takes a non-primitive type as an input, the constructor must load the corresponding field with a
deep copy of the input. (Strings do not need to have defensive copies)






## Class Structure Rules

Classes must match the template below.  

There are lines of comments separating the different sections. The lines should appear in the class definition.
The number of comment lines may vary. Longer classes should have more comment lines separating each section.

Underneath each row of comment lines is a section header. The section header should also appear in the class definition.



##### BEGIN CLASS FORMAT

Docstring that conforms to the [Docstring Rules](#docstring-rules)   
NOTE: Since class definitions have no inputs or return values, 
a class docstring does not include any parameters, return values, or exceptions.


Constants, whose names are in all caps, with a descriptive name.
Must be declared in a logical order. If no logical order exists, declare in alphabetical order by variable name.  
Examples:  
`final String DEFAULT_INPUT = "Hello world";`  
`final int MAX_HEIGHT = 1000;`

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  

FXML variable fields, i.e. those annotated with `@FXML`  
Must be declared in a logical order. If no logical order exists, declare in alphabetical order 
by datatype, then by variable name.

Variable fields  
Must be declared in a logical order. If no logical order exists, declare in alphabetical order by variable name.


NOTE: All constant and variable fields MUST HAVE DOCSTRINGS!

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
//CONSTRUCTOR

Class constructor(s). If the class has multiple constructors, the most frequently used constructors are first.  
ALL CLASSES must have an explicitly defined constructor, even if the constructor does nothing.
Exception: subclasses do not need explicitly defined constructors if the superclass has one.

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
//HELPERS

Any private helper methods that other class methods may use.  
All must have docstrings and be organized in alphabetical order by method name.

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
//GETTERS AND SETTERS

Getter and setter methods, utility methods (i.e. `toString`)  
For any encapsulated field, the field's getter method must come before the setter method.  
All must have docstrings and be organized in alphabetical order by the name of the field manipulated.  
Example of alphabetical organization:  
`public void getA()...`  
`public void setA()...`  
`public void getB()...`  
`public void setB()...`  

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
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

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////  

Main method: `public static void main(String[] args) {...}`

##### END CLASS FORMAT




## Final Remarks
Curran Muhlburger is not allowed to contribute to any documentation in this project.

[Back to table of contents](#table-of-contents)