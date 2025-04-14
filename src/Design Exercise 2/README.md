# Design Exercise 2: Data Structures Implementation

This exercise implements two different data structures for managing information:

## 1. Phone Book System (`PhoneBook.java`)

An efficient phone book implementation that supports bidirectional lookups between names and phone numbers.

### Features:

- Multiple phone numbers per person
- Multiple people per phone number (e.g., household landlines)
- Alphabetically sorted name retrieval
- Numerically sorted phone number retrieval
- Immutable return collections for thread safety
- Static factory methods for flexible instantiation

### Usage Example:

```java
// Create a new phone book
PhoneBook phoneBook = PhoneBook.create();

// Add entries
phoneBook.addEntry("John Smith", "555-1234");
phoneBook.addEntry("John Smith", "555-5678"); // Multiple phones
phoneBook.addEntry("Jane Smith", "555-1234"); // Multiple people

// Lookup operations
Set<String> johnsPhones = phoneBook.getPhoneNumbers("John Smith");
Set<String> peopleWith1234 = phoneBook.getNames("555-1234");
```

## 2. Gaming Stats System (`GamingStats.java`)

A flexible system for tracking gaming performance statistics across multiple gaming sessions.

### Features:

- KDA (Kills/Deaths/Assists) tracking per session
- Multiple sessions per game
- Timestamp-based session management
- Builder pattern for clean object creation
- Immutable session data
- Thread-safe collections

### Usage Example:

```java
// Create stats tracker
GamingStats stats = GamingStats.create();

// Create a new session using builder
GameSession session = new GameSessionBuilder()
    .gameName("Call of Duty")
    .timestamp(Instant.now())
    .build();

// Add and update session
stats.addSession(session);
stats.updateSession("Call of Duty", session.timestamp, 15, 2, 8);
```

## Implementation Details

Both implementations follow best practices from "Effective Java" including:

- Static factory methods instead of constructors
- Builder pattern for complex object creation
- Immutable objects where appropriate
- Defensive copying
- Proper resource management
- Thread-safe collections

## Requirements

- Java 8 or higher
- No external dependencies
