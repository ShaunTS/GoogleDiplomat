## Google Diplomat

The initial goal of this project was to create a Play Framework Application in scala to interact with Google Rest APIs (specifically the family of Google Places APIs), parse the resulting data into strongly typed data models, then persist those data models to a highly normalized PostgreSQL database.

At this stage, I have shifted to the more short-term goal of developing a general purpose toolkit of Play utilities revolving around json validation and adapting anorm functionality to work with a Postgres database. I've also created a fairly extensive hierarchy of custom error case classes, as well as a functional  mapping utility that will match Play exceptions and errors to corrsponding custom errors.

### Points of Interest

**Error classes and mapping**
https://github.com/ShaunTS/GoogleDiplomat/tree/master/app/libs/errors

**`JsParam[A]` utility for wrapping key-value pairs, while preserving the type and json-serializer of the wrapped type `A`**
https://github.com/ShaunTS/GoogleDiplomat/blob/master/app/models/JsParams.scala
