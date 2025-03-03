package entitties;

public enum CoursePlan {
    MOTO_BASIC(1, "Basic Motorcycle Course", 199.99, "Fundamental motorcycle training for beginners"),
    MOTO_ADVANCED(2, "Advanced Motorcycle Course", 299.99, "Advanced motorcycle handling and safety techniques"),

    CAR_BASIC(3, "Basic Car Driving Course", 349.99, "Essential car driving skills for new drivers"),
    CAR_DEFENSIVE(4, "Defensive Driving Course", 299.99, "Safety-focused defensive driving techniques"),
    CAR_INTENSIVE(5, "Intensive Car Course", 599.99, "Accelerated program for quick licensing"),

    TRUCK_LIGHT(6, "Light Truck Course", 499.99, "Training for driving light commercial vehicles"),
    TRUCK_HEAVY(7, "Heavy Truck License Course", 899.99, "Complete training for heavy goods vehicle license");

    private final int id;
    private final String name;
    private final double price;
    private final String description;

    CoursePlan(int id, String name, double price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public static CoursePlan getById(int id) {
        for (CoursePlan plan : values()) {
            if (plan.getId() == id) {
                return plan;
            }
        }
        throw new IllegalArgumentException("No course plan found with ID: " + id);
    }

    public String getCategory() {
        if (this.name.startsWith("Moto")) {
            return "Motorcycle";
        } else if (this.name.startsWith("Car")) {
            return "Car";
        } else {
            return "Truck";
        }
    }
}
