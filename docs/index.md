---
layout: default
title: Overview
nav_order: 001
---
# Domino Java API Next

Modern Java API for Domino based on JNA to Domino's C API

## Design Goals

Goals and decisions around the design of the API

- Runs on Java8 or later, take advantage of Java8++ capabilities
- Methods return the object where appropriate to enable use of lambdas, plus builder and build methods where appropriate (settings for DominoClient, updating multiple ACL entries in one call etc)
- Run on Server, basic & standard client
- Feel like real Java API to database and application server
- No dependencies on Eclipse plugins or Notes.jar
- Use of `com.hcl.domino` namespace
- Higher level functions for workflow applications
- no `recycle` operations
- Interface -> Classes -> In-memory extensions to facilitate mocking and tests without needing a Domino server. Mock classes can still perform transactions, but they write as plain text JSON to local text file - it's intended for testing and getting familiar, it's not intended for sensitive data or performance verification!
- Appropriate parameter names. Anyone using arg0, arg1 buys the team a drink ;-)
- JavaDoc comments throughout, as meaningful as possible, and including good example code. **NOTE** How can we best use progressive display to make comments work for non-Domino developers but also support existing Domino developers coming from traditional APIs? Do we just need separate documentation for "so you're used to Views, what are the differences?", "so you're used to Documents, what are the differences?", "where's the DateTime class?"
- Build for extensibility, so avoided _protected_ modifier where possible
- Enums not ints
- Abstract 32-bit / 64-bit methods from Domino JNA


## Non-Goals

- backward compatibility to existing Java API
- Android compatibility
- migration path for existing code (existing Java code is typically not written for re-use in new contexts)
- presumably we're also excluding UI classes, this is backend code only

## For further discussion

- Cursors, see separate document
- Do we want something like "watch for", "observable" to register to be notified if something changes, using RxJava or something else?
- Encryption of specific fields, with encryption key stored against (external) application?
- Domino encrypted fields? The implementation external to Domino will have to perform the decryption and the external application will have access, because it needs to send the data to the user interface. So is this really relevant outside an HCL client framework (Notes or XPages)? There will always be a "man-in-the-middle" risk, so the security is weakened. Or do we say the application needs the encryption keys as well as the user, and require an intersection of access.
- Transactional access - is this managed by a TransactionalDominoClient, or even made the default and you need to use a NonTransactionalDominoClient (DominoClient is by default transactional). Alternatively a Session property?
- Do we want / need to add anything for logging / monitoring? I'm not sure if Karsten's getNSFDatabase logs a session opened in log.nsf.