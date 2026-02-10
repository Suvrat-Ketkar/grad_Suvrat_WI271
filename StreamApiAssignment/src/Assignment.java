package src;
import java.util.*;
import java.util.stream.*;

class Employee
{
    private String name;
    private int age;
    private String gender;
    private double salary;
    private String designation;
    private String department;

    public Employee(String name, int age, String gender, double salary, String designation, String department)
    {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.salary = salary;
        this.designation = designation;
        this.department = department;
    }

    public String getName()
    { 
        return name;
    }

    public int getAge()
    {
        return age;
    }

    public String getGender() 
    { 
        return gender;
    }

    public double getSalary()
    {
        return salary;
    }

    public String getDesignation()
    { 
        return designation; 
    }

    public String getDepartment()
    { 
        return department; 
    }

    public void setSalary(double salary) {
        this.salary = salary;
    }

    public String toString()
    {
        return "Employee:\n" +
            "name='" + name + "'\n" +
            "age=" + age + "\n" +
            "gender='" + gender + "'\n" +
            "salary=" + salary + "\n" +
            "designation='" + designation + "'\n" +
            "department='" + department + "'";
    }

}

public class Assignment
{

    public static void main(String[] args)
    {

        List<Employee> employees = new ArrayList<>();

        employees.add(new Employee("Amit", 45, "Male", 120000, "Manager", "IT"));
        employees.add(new Employee("Neha", 32, "Female", 80000, "Developer", "IT"));
        employees.add(new Employee("Rahul", 28, "Male", 60000, "Tester", "QA"));
        employees.add(new Employee("Priya", 40, "Female", 95000, "Manager", "HR"));
        employees.add(new Employee("Suresh", 50, "Male", 150000, "Director", "Management"));
        employees.add(new Employee("Kiran", 35, "Male", 70000, "Developer", "IT"));
        employees.add(new Employee("Anita", 30, "Female", 65000, "Recruiter", "HR"));
        employees.add(new Employee("Vikram", 55, "Male", 200000, "CEO", "Management"));
        employees.add(new Employee("Pooja", 27, "Female", 55000, "Analyst", "Finance"));
        employees.add(new Employee("Rohit", 42, "Male", 110000, "Manager", "Finance"));

        // 1. Find the highest salary paid employee
        System.out.println("-------------------------");
        System.out.println("1) Highest salary employee:");
        employees.stream()
            .sorted((e1, e2) -> Double.compare(e2.getSalary(), e1.getSalary()))
            .limit(1)
            .forEach(System.out::println);


        // 2. Find how many male & female employees
        System.out.println("-------------------------");
        System.out.println("2) Count of Male & Female employees:");
        Map<String, Long> genderCount = employees.stream().collect(Collectors.groupingBy(Employee::getGender, Collectors.counting()));
        System.out.println(genderCount);

        // 3. Total expense for the company department wise
        System.out.println("-------------------------");
        System.out.println("3) Total salary expense department wise:");
        Map<String, Double> deptExpense = employees.stream().collect(Collectors.groupingBy(Employee::getDepartment,Collectors.summingDouble(Employee::getSalary)));
        
        deptExpense.forEach((dept, total) -> System.out.println(dept + " : " + total));

        // 4. Top 5 senior employees (by age)
        System.out.println("-------------------------");
        System.out.println("4) Top 5 senior employees:");
        employees.stream().sorted(Comparator.comparingInt(Employee::getAge).reversed()).limit(5).forEach(System.out::println);

        // 5. Find only the names who all are managers
        System.out.println("-------------------------");
        System.out.println("5) Names of all Managers:");
        employees.stream().filter(e -> e.getDesignation().equalsIgnoreCase("Manager")).map(Employee::getName).forEach(System.out::println);

        // 6. Hike the salary by 20% for everyone except manager
        System.out.println("-------------------------");
        System.out.println("6) Salary after 20% hike (except managers):");
        employees.stream().filter(e -> !e.getDesignation().equalsIgnoreCase("Manager")).forEach(e -> e.setSalary(e.getSalary() * 1.20));

        employees.forEach(System.out::println);

        // 7. Find the total number of employees
        System.out.println("-------------------------");
        System.out.println("7) Total number of employees:");
        long count = employees.stream().count();
        System.out.println(count);
    }
}
