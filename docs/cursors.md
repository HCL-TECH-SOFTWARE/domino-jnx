---
layout: default
title: Cursors and Collection Navigation
nav_order: 002
---

I can't remember if Cursors have been on the roadmap for DQL, not sure. But it's probably not performant - and, in some scenarios, desirable - to send all data from a collection or matching a query.

At the same time, if you've got distinct calls, what is the best - or available - approaches to navigating through the dataset?
- How do you handle getting "next" or "previous" if there are inserts / deletions?
- What if the entry you're navigating from is no longer in the view?
- getEntryById in the core API doesn't cater for "show multiple values as separate entries".

On the last point, consider this scenario of a document categorised under "Domino" and "Sametime". The view looks like:
- Domino
    - How to install Domino (noteId 1111)
    - How to integrate Domino and Sametime (nodeId 2222)
    - System Requirements for Domino (noteId 3333)
- Sametime
    - How to install Sametime (noteId 4444)
    - How to integrate Domino and Sametime (noteId 2222)
    - System Requirements for Sametime (noteId 5555)

As far as I've been able to find, getting the entry by ID will get the entry under the Domino category, so getting the next entry will always get"System Requirements for Domino (noteId 3333). So you need to store the position (2.2). But if an entry gets inserted for "Leap > How to install Leap" before you try to get the next entry, the position isn't 2.2, but 3.2. How do you know or find out its new position?

An option is to allow caching of collections,maybe a CachedCollection class extending Collection, retrieved through an overloaded method that also passes the unique key or `getNextEntries()` passing the . This would require:
- get collection
- return sub-collection to request
- include unique key for request and cursors for before/after navigation
- asynchronously, serialise the whole collection and store with the unique key
- subsequent requests pass the unique key, maybe to the overloaded `getCollection()` method or to `getNext(Cursor, uniqueKey)`
- an additional timeout on the cursor