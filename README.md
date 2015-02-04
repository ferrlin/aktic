###Aktic - lightweight elasticsearch client built on Akka with Akka-http experimental module

[![Build Status](https://travis-ci.org/ferrlin/aktic.svg?branch=master)](https://travis-ci.org/ferrlin/aktic)

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
val client = Aktic(ActorSystem("hello"))
```

After successfully instantiating Aktic, you need to specify the index and type that will be used to identify the document you would want to operate on.

This can be done by simply doing the following:
```
val index = "OurIndex"
val typ = "OurType"
```

Another way of doing this is by defining a DocPath wrapping the data above.
```
implicit val docPath = DocPath("OurIndex", "OurType")
```


With everything setup, we can now start using the api. 

The following will show you two ways of doing the succeeding operations.

###Indexing

```
val id = Some("xba23")
val document = """{}"""
```

#a
```
client.index(index, typ, document, id)

// or if we want elasticsearch to provide the `id`
client.index( index, typ, document, None)
```

#b
```
client.index(id, document)(docPath)

// or simply
client.index(id, document)
```

 >
###Updating

#a
```
client.update(index, typ, document, id)
```

#b
```
client.update(id,document)(docPath)

//or simply
client.update(id, document)
```

###Deleting

#a
```
client.delete(index, typ, id)
```

#b
```
client.delete(id)(docPath)

// or simply
client.delete(id)
```


### Retrieving / Searching
#a 
```
client.get(index, typ, id)

client.search(index, Seq("size=20"))
```

#b
```
client.get(id)(docPath)

// or simply
client.get(id)
```

###

###Roadmap
 ...