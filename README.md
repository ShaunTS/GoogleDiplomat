## Google Diplomat

The initial goal of this project was to create a Play Framework Application in scala to interact with Google Rest APIs (specifically the family of Google Places APIs), parse the resulting data into strongly typed data models, then persist those data models to a highly normalized PostgreSQL database.

At this stage, I have shifted to the more short-term goal of developing a general purpose toolkit of Play utilities revolving around json validation and adapting anorm functionality to work with a Postgres database. I've also created a fairly extensive hierarchy of custom error case classes, as well as a functional  mapping utility that will match Play exceptions and errors to corrsponding custom errors.

### Points of Interest

**Error classes and mapping**([_Source_](https://github.com/ShaunTS/GoogleDiplomat/tree/master/app/libs/errors)/[_Tests_](https://github.com/ShaunTS/GoogleDiplomat/blob/master/test/ErrorSpec.scala))
  
**JsParam[A]**  
Utility for wrapping key-value pairs, while preserving the type and json-serializer of the wrapped type `A`  
 ...[_Source_](https://github.com/ShaunTS/GoogleDiplomat/blob/master/app/models/JsParams.scala)..
 ...[_Tests_](https://github.com/ShaunTS/GoogleDiplomat/blob/master/test/JsParamSpec.scala)..

**Postgres Evolutions**  
 ...[_Source_](https://github.com/ShaunTS/GoogleDiplomat/blob/master/conf/evolutions/default/1.sql)..  
   
**PSQLHandler (Postgres database handler)**  
 ...Functionally defines how data will be sent to and from a Postgres database. That is to say every function in the defining class `PSQLFunctions` will accept some other function that uses a [`java.sql.Connection`](https://docs.oracle.com/javase/7/docs/api/java/sql/Connection.html), execute the function within the scope of an open connection, and catch any exceptions that may occur mapping them to [custom error classes](https://github.com/ShaunTS/GoogleDiplomat/blob/master/app/libs/errors/PostgresErrors.scala). ..  
 ...[_Source_](https://github.com/ShaunTS/GoogleDiplomat/blob/master/app/libs/PSQLHandler.scala)..
  
