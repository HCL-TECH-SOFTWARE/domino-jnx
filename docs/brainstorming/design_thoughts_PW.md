### General
Do we want to allow mapping from our objects to lotus.domino objects? Or do we expect people to just get a Domino session and navigate down if they want that functionality?

It will be key to have constructors that pass the admin / design versions of an object. This may actually just abstract some functionality, e.g. still going to the underlying database to get the corresponding element.

Use Builder classes to allow lambdas for creation.

### Session
Can we avoid dependencies on Notes.jar? 
It's required for NotesFactory, which is used for createSession. 
Is this going to be a problem for circulating code, or can we hook into Sessions at the C layer?

I think the Session is needed to get NotesInternational class, to get D/M/Y or M/D/Y or Y/M/D and 12-hour / 24-hour formats from the server.

### Database
We have Database, DbDesign, do we also want DbAdmin for administration of an NSF - signing, replication etc?

We will also need a DbCatalog class ("keep" name, filepath / replicaId / both). Seems like a good time to minimise the number of methods for accessing a database, and just allow passing the "keep" name. This should be the constructor.

## Documents
Having Document extend Map - or JsonObject - allows easy conversion to JSON for REST services. We can just have `get()` and `put()` methods - it's a new API after all. These can auto-box from whatever object type is passed. Vert.x returns `this` for `JsonObject.put()`, but that gives a dependency outside of core and pushes towards one framework implementation - not desirable for adoption.

It would be preferable if the `put()` method restricted keys that can't be displayed in a view - I know you can't use a hyphen (I tried setting a field value as "Project_" + @Unique!).

It would also be preferable if the `put()` method threw the error if there was a problem. Currently, `replaceItemValue()` accepts the value and it's only on `.save()` that you get an error. That makes it harder to tell which field update was the problem.

Domino can have multiple instances of an Item. That is confusing, but it's a core element of mail. How do we handle things like `getFirstItem()`?