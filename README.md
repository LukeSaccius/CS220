# CS220 Java Implementations

This repository contains two Java implementations:

## 1. Phone Book System (`PhoneBook.java`)

A bidirectional phone book system that can store and retrieve name and telephone number information. The system supports:

- Multiple phone numbers per person
- Multiple people per phone number (e.g., household landlines)
- Searching by name or phone number
- Retrieving all names in alphabetical order
- Retrieving all phone numbers in numeric order

### Usage Example:

```java
PhoneBook phoneBook = new PhoneBook();
phoneBook.addEntry("John Smith", "555-1234");
phoneBook.addEntry("John Smith", "555-5678"); // Multiple phones
phoneBook.addEntry("Jane Smith", "555-1234"); // Multiple people

// Get phone numbers for a name
Set<String> phones = phoneBook.getPhoneNumbers("John Smith");

// Get names for a phone number
Set<String> names = phoneBook.getNames("555-1234");

// Get all names in alphabetical order
Set<String> allNames = phoneBook.getAllNames();

// Get all phone numbers in numeric order
Set<String> allPhones = phoneBook.getAllPhoneNumbers();
```

## 2. Gaming Stats System (`GamingStats.java`)

A personal record-keeping system for first-person shooter games that tracks:

- KDA (Kills, Deaths, Assists) statistics
- Multiple gaming sessions per game
- Timestamp for each session
- Ability to update stats as they change

### Usage Example:

```java
GamingStats stats = new GamingStats();
Date sessionTime = new Date();

// Add a new gaming session
stats.addSession("Call of Duty", sessionTime);

// Update stats for the session
stats.updateSession("Call of Duty", sessionTime, 15, 2, 8);

// Get all sessions for a game
List<GameSession> sessions = stats.getGameSessions("Call of Duty");
```

## Requirements

- Java 8 or higher
- No external dependencies required

## How to Run

1. Clone this repository
2. Navigate to the `src` directory
3. Compile the Java files:
   ```
   javac *.java
   ```
4. Run either implementation:
   ```
   java PhoneBook
   ```
   or
   ```
   java GamingStats
   ```
