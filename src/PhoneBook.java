import java.util.*;

public class PhoneBook {
    private final Map<String, Set<String>> nameToPhones;
    private final Map<String, Set<String>> phoneToNames;
    private final TreeSet<String> allNames;
    private final TreeSet<String> allPhones;

    // Private constructor to enforce use of static factory methods
    private PhoneBook() {
        nameToPhones = new HashMap<>();
        phoneToNames = new HashMap<>();
        allNames = new TreeSet<>();
        allPhones = new TreeSet<>();
    }

    // Static factory method
    public static PhoneBook create() {
        return new PhoneBook();
    }

    // Static factory method with initial entries
    public static PhoneBook createWithEntries(Map<String, Set<String>> initialEntries) {
        PhoneBook phoneBook = new PhoneBook();
        for (Map.Entry<String, Set<String>> entry : initialEntries.entrySet()) {
            for (String phone : entry.getValue()) {
                phoneBook.addEntry(entry.getKey(), phone);
            }
        }
        return phoneBook;
    }

    public void addEntry(String name, String phoneNumber) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(phoneNumber, "Phone number cannot be null");

        // Add to name-to-phones mapping
        nameToPhones.computeIfAbsent(name, k -> new HashSet<>()).add(phoneNumber);

        // Add to phone-to-names mapping
        phoneToNames.computeIfAbsent(phoneNumber, k -> new HashSet<>()).add(name);

        // Update sorted collections
        allNames.add(name);
        allPhones.add(phoneNumber);
    }

    public Set<String> getPhoneNumbers(String name) {
        return Collections.unmodifiableSet(
                nameToPhones.getOrDefault(name, Collections.emptySet()));
    }

    public Set<String> getNames(String phoneNumber) {
        return Collections.unmodifiableSet(
                phoneToNames.getOrDefault(phoneNumber, Collections.emptySet()));
    }

    public Set<String> getAllNames() {
        return Collections.unmodifiableSet(allNames);
    }

    public Set<String> getAllPhoneNumbers() {
        return Collections.unmodifiableSet(allPhones);
    }

    public static void main(String[] args) {
        // Example usage with static factory methods
        PhoneBook phoneBook = PhoneBook.create();

        // Add some entries
        phoneBook.addEntry("John Smith", "555-1234");
        phoneBook.addEntry("John Smith", "555-5678"); // Multiple phones
        phoneBook.addEntry("Jane Smith", "555-1234"); // Multiple people

        System.out.println("John Smith's phone numbers: " + phoneBook.getPhoneNumbers("John Smith"));
        System.out.println("People with phone 555-1234: " + phoneBook.getNames("555-1234"));
        System.out.println("All names: " + phoneBook.getAllNames());
        System.out.println("All phone numbers: " + phoneBook.getAllPhoneNumbers());
    }
}