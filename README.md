###Aktic - lightweight elasticsearch client built on Akka with Akka-http experimental module

###Getting started

To use the api, you must first instantiate an `Aktic` instance which will serve as your elasticsearch client.

```
    val client = Aktic()
```

By default, aktic retrieves configuration data from application.conf that comes along with the project that contains the following:
```
aktic {
  host = "localhost"
  port = 9200
}
```

The Aktic class contains two other constructor where it receives an actorsystem , config instance or both.
```
val clientWithActorSytem = Aktic(ActorSystem("hello"))

val clientWithConfig = Aktic(***** example here *****)
```

When Aktic instance is successfully created, you can begin using the api. Here are ways to use the basic operations you can do:

###Indexing
 < more details to follow >
###Updating
 < more details to follow >   
###Deleting
 < more details to follow >
###Searching
 < more details to follow >
< -- to be added later -- >