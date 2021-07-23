---
layout: default
title: Brainstorm
nav_order: 999
---
# Original Brainstorm Document

This are the original notes from the 25Nov19 session

## Java Version

TBD. 1.8 for now - check if latest LTS will ship when with Domino

## Interfaces for Data

New interfaces in the `com.hcl.domino` namespace. Make them modern, no old baggage required


### com.hcl.domino.Session

Potentially this is actually not needed. Eventually store user related info like group memeberships. TBD
Use in constructors to send user info. E.g. what cal info

### com.hcl.domino.DbInfo

Einzeleintrag wie DbDirectory entry from session.scanDirectroy or Database

### com.hcl.domino.Database


### com.hcl.domino.SearchQuery
- needs all query methods like DQL, map with View/Folder filter
- DBSearch, DQL, etc.

### com.hcl.domino.ACL ACLEntry
- standard ACL
- application access query
- 

more or less like exsiting API

### com.hcl.domino.Collection

Folders, Views (Standard Collection, Static Collection) -> contains CollectionEntry

Make it hibernate compliant?

### com.hcl.domino.Document

### com.hcl.domino.Item

Carefull attention for date/time and the new Java Stuff and JNA TimeDate

### com.hcl.domino.RichText

--> keine Mime Klassen, javax.mail nehmen

### com.hcl.domino.Person

Important -> needs a good caching strategty and a good scoping (like: Server, directory, single DB)
Inclusiv OutOfOffice info.

TBD: should it have social info. 

### com.hcl.domino.Server

Server command and sever info (e.g. DB Access), console commands

### com.hcl.domino.Admin

### com.hcl.domino.IdVault

### com.hcl.domino.FreeBusy

eventuell: OOO info

-> TBD: Rooms and Reservation, Calendar operations

### com.hcl.domino.MessageQueue

## Interfaces for Design

### com.hcl.domino.ECL 

### com.hcl.domino.DesignElement 

### com.hcl.domino.DXL

### com.hcl.domino.Design

- Database as constructor argument
- search design element

### com.hcl.domino.Replication

### com.hcl.domino.Schema
