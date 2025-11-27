import java.io.*;
import java.util.*;

/*
 StudentGradeTracker.java
 Simple console app to add students and grades, compute avg/high/low, save/load CSV.
*/

public class StudentGradeTracker {
    private static final String DATA_FILE = "grades.csv";
    private List<Student> students = new ArrayList<>();
    private Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        StudentGradeTracker app = new StudentGradeTracker();
        app.load();
        app.menu();
        app.save();
        System.out.println("Exiting. Data saved to " + DATA_FILE);
    }

    void menu() {
        while (true) {
            System.out.println("\n--- Student Grade Tracker ---");
            System.out.println("1. Add student");
            System.out.println("2. List students");
            System.out.println("3. Show summary report");
            System.out.println("4. Remove student");
            System.out.println("5. Save and Exit");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addStudent(); break;
                case "2": listStudents(); break;
                case "3": showReport(); break;
                case "4": removeStudent(); break;
                case "5": return;
                default: System.out.println("Invalid option.");
            }
        }
    }

    void addStudent() {
        System.out.print("Student name: ");
        String name = sc.nextLine().trim();
        List<Double> grades = new ArrayList<>();
        while (true) {
            System.out.print("Enter grade (or blank to finish): ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) break;
            try {
                double g = Double.parseDouble(line);
                if (g < 0 || g > 100) System.out.println("Enter 0-100.");
                else grades.add(g);
            } catch (NumberFormatException e) { System.out.println("Invalid number."); }
        }
        if (grades.isEmpty()) System.out.println("No grades added, student not saved.");
        else {
            students.add(new Student(name, grades));
            System.out.println("Student added.");
        }
    }

    void listStudents() {
        if (students.isEmpty()) { System.out.println("No students."); return; }
        int i=1;
        for (Student s : students) {
            System.out.printf("%d. %s - Grades: %s\n", i++, s.name, s.grades);
        }
    }

    void showReport() {
        if (students.isEmpty()) { System.out.println("No data."); return; }
        System.out.println("\n--- Summary Report ---");
        double totalAvg = 0;
        Student best = null, worst = null;
        for (Student s : students) {
            double avg = s.average();
            totalAvg += avg;
            if (best == null || avg > best.average()) best = s;
            if (worst == null || avg < worst.average()) worst = s;
        }
        System.out.printf("Total students: %d\n", students.size());
        System.out.printf("Class average: %.2f\n", totalAvg / students.size());
        System.out.printf("Highest avg: %s (%.2f)\n", best.name, best.average());
        System.out.printf("Lowest avg: %s (%.2f)\n", worst.name, worst.average());
        System.out.println("\nDetailed:");
        for (Student s : students) {
            System.out.printf("%s -> Avg: %.2f  Max: %.2f  Min: %.2f\n",
                s.name, s.average(), s.max(), s.min());
        }
    }

    void removeStudent() {
        listStudents();
        if (students.isEmpty()) return;
        System.out.print("Enter number to remove (or blank): ");
        String line = sc.nextLine().trim();
        if (line.isEmpty()) return;
        try {
            int idx = Integer.parseInt(line) - 1;
            if (idx >= 0 && idx < students.size()) {
                System.out.println("Removed: " + students.remove(idx).name);
            } else System.out.println("Index out of range.");
        } catch (NumberFormatException e) { System.out.println("Invalid."); }
    }

    // Persistence simple CSV: name,grade1;grade2;grade3
    void save() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(DATA_FILE))) {
            for (Student s : students) {
                pw.print(escape(s.name));
                pw.print(",");
                for (int i = 0; i < s.grades.size(); i++) {
                    if (i>0) pw.print(";");
                    pw.print(s.grades.get(i));
                }
                pw.println();
            }
        } catch (IOException e) {
            System.err.println("Save failed: " + e.getMessage());
        }
    }

    void load() {
        File f = new File(DATA_FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 2);
                if (parts.length < 2) continue;
                String name = unescape(parts[0]);
                String[] gs = parts[1].split(";");
                List<Double> grades = new ArrayList<>();
                for (String g : gs) {
                    try { grades.add(Double.parseDouble(g)); } catch (Exception ignored) {}
                }
                if (!grades.isEmpty()) students.add(new Student(name, grades));
            }
        } catch (IOException e) { System.err.println("Load failed: " + e.getMessage()); }
    }

    String escape(String s) { return s.replace(",", "\\,"); }
    String unescape(String s) { return s.replace("\\,", ","); }

    static class Student {
        String name;
        List<Double> grades;
        Student(String name, List<Double> grades) { this.name = name; this.grades = new ArrayList<>(grades); }
        double average() { return grades.stream().mapToDouble(Double::doubleValue).average().orElse(0); }
        double max() { return grades.stream().mapToDouble(Double::doubleValue).max().orElse(0); }
        double min() { return grades.stream().mapToDouble(Double::doubleValue).min().orElse(0); }
    }
}

