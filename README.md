# Expressions parser

## How to run
Run project directly from build engine and provide arguments with expression and list of variables):
```
./gradlew run --args="'(a+(b+c))/d' a 10 b 20 c 30 d 40"
```

or build project and execute `jar` file with similar arguments
```
./gradlew build
java -jar ./build/libs/math-expressions.jar "(a+(b+c))/d" a 10 b 20 c 30 d 40
```



## How to use
[Expression class](./src/main/java/io/github/ngsandbox/math/expressions/Expression.java) is a main facade for parsing and evaluation provided formula.
Few examples how to use expression evaluation (lots of them could be found in the [unit tests](./src/test/java/io/github/ngsandbox/math/expressions)): 
* `new Expression("3.14*2.0").eval()` - has to return [wrapped BigDecimal](./src/main/java/io/github/ngsandbox/math/expressions/wrappers/WrappedBigDecimal.java)
* `new Expression("y == \"ABC\"").with("y", "1").eval()` - has to return [wrapped BigDecimal ZERO](./src/main/java/io/github/ngsandbox/math/expressions/wrappers/WrappedBigDecimal.java) 
* `new Expression("if(a==0,\"ERR\",12/a)").with("a", 0).eval()` - has to return [wrapped String "ERR"](./src/main/java/io/github/ngsandbox/math/expressions/wrappers/WrappedString.java) 

### Implementation
The `Expression class` itself uses [Shunting-yard algorithm](./src/main/java/io/github/ngsandbox/math/expressions/tokens/ShuntingYardParser.java) to parse provided string formula to 
the [Reverse Polish notation](https://en.wikipedia.org/wiki/Reverse_Polish_notation) and use it for the future calculations 

Additional classes:
* [ExpressionSettings](./src/main/java/io/github/ngsandbox/math/expressions/ExpressionSettings.java) - setup precision and round type for `Expression class`  
* [Operators](./src/main/java/io/github/ngsandbox/math/expressions/operators/Operators.java) - the abstract factory with list of available math and logic operators and their processing (e.g.: `+`, `-`, `*`, `&&`, `>`, `<`, etc)
* [Functions](./src/main/java/io/github/ngsandbox/math/expressions/functions/Functions.java) - the abstract factory with list of available functions and their processing (e.g.: `MIN`, `MAX`, `IF`, `NOT`, etc.)
* [wrappers](./src/main/java/io/github/ngsandbox/math/expressions/wrappers) - wrappers for different types of values (variables, functions, string, decimal, etc). Main methods: 
  * `unwrap` - provides access to the calculated value
  * `getExpression` - returns a string expression itself


