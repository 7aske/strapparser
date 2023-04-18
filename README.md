# strapparser

## Description

Utility for code generation based on simple DSL for describing entities.

## Usage

```
Usage: args [OPTIONS] INPUTFILE

Options:
  --lombok                         Use Lombok (useful only for Java)
  --auditable                      Add database auditing
  --security                       Generate basic JWT security implementation
  --doc                            Generate openapi documentation and
                                   configuration
  -A, --all                        Generate everything (entity, repository,
                                   service, controller)
  -E, --entity                     Generate entities
  -R, --repository                 Generate repositories
  -S, --service                    Generate services
  -C, --controller                 Generate controllers
  --specification                  Repositories will extend JPA Specification
  -o, --output VALUE               Output directory (project root)
  -d, --domain TEXT                Domain/package of the project (e.g.
                                   com.example)
  -n, --name TEXT                  Name of project (e.g. app)
  --javaVersion TEXT               Java version
  --kotlinVersion TEXT             Kotlin version
  --springVersion TEXT             Spring Boot version
  --packaging TEXT                 Packaging type (e.g. jar, war)
  --database [POSTGRES|MARIADB|MYSQL|MONGODB]
                                   Database type (e.g. mysql, postgres,
                                   mariadb, mongodb)
  --databaseUser TEXT              Database user
  --databasePass TEXT              Database password
  --databaseHost TEXT              Database host
  --databasePort INT               Database port
  --databaseName TEXT              Database name
  --language TEXT                  Language (e.g. java, kotlin)
  -h, --help                       Show this message and exit

Arguments:
  INPUTFILE  Input .strap file
```

### Strap file

This is the syntax for the input file:

```strap
entity EntityName [userDetails, table=`table_name`]
    field `id` <type> [id, references, list, serial, column=`column_name`, username, password]
```

Types:

```
"string" to "String"
"varchar" to "String"
"text" to "String"
"char" to "Char"
"int" to "Int"
"integer" to "Int"
"long" to "Long"
"float" to "Float"
"double" to "Double"
"decimal" to "Double"
"real" to "Double"
"bool" to "Boolean"
"boolean" to "Boolean"
"timestamp" to "Instant"
"date" to "LocalDate"
"datetime" to "LocalDateTime"
"time" to "LocalTime"
```

### Example

If we want to build a simple blog application our input file could look like this:

```strap
entity Post
    field `id` int serial id
    field `title` string
    field `content` string
    field `author` references Author
    field `tag` list Tag
    field `category` references Category
    
entity Author userDetails table=`user`
    field `id` int serial id
    field `email` string username column=`username`
    field `password` string password
    
entity Category
    field `id` int serial id
    field `name` string
    
entity Tag
    field `id` int serial id
    field `name` string
    field `post` list Post
```

And the command to generate the code would be:

```bash
strap-parser app.strap -A --lombok --auditable --security --specification --doc --language java -o ~/test -d com._7aske -n app
```