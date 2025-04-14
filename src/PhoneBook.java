import java.util.*;

public class PhoneBook {
    private Map<String, Set<String>> nameToPhones;
    private Map<String, Set<String>> phoneToNames;
    private TreeSet<String> allNames;
    private TreeSet<String> allPhones;

    public PhoneBook() {
        nameToPhones = new HashMap<>();
        phoneToNames = new HashMap<>();
        allNames = new TreeSet<>();
        allPhones = new TreeSet<>();
    }

    public void addEntry(String name, String phoneNumber) {
        // Add to name-to-phones mapping
        nameToPhones.computeIfAbsent(name, k -> new HashSet<>()).add(phoneNumber);

        // Add to phone-to-names mapping
        phoneToNames.computeIfAbsent(phoneNumber, k -> new HashSet<>()).add(name);

        // Update sorted collections
        allNames.add(name);
        allPhones.add(phoneNumber);
    }

    public Set<String> getPhoneNumbers(String name) {
        return nameToPhones.getOrDefault(name, new HashSet<>());
    }

    public Set<String> getNames(String phoneNumber) {
        return phoneToNames.getOrDefault(phoneNumber, new HashSet<>());
    }

    public Set<String> getAllNames() {
        return allNames;
    }

    public Set<String> getAllPhoneNumbers() {
        return allPhones;
    }

    public static void main(String[] args) {
        PhoneBook phoneBook = new PhoneBook();

        // Example usage
        phoneBook.addEntry("John Smith", "555-1234");
        phoneBook.addEntry("John Smith", "555-5678"); // John has multiple phones
        phoneBook.addEntry("Jane Smith", "555-1234"); // Multiple people share a landline

        System.out.println("John Smith's phone numbers: " + phoneBook.getPhoneNumbers("John Smith"));
        System.out.println("People with phone 555-1234: " + phoneBook.getNames("555-1234"));
        System.out.println("All names: " + phoneBook.getAllNames());
        System.out.println("All phone numbers: " + phoneBook.getAllPhoneNumbers());
    }
}