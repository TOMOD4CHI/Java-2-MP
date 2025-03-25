package org.cpi2.entities;

public enum CoursePlan {
    MOTO_BASIC(1, "Basic Motorcycle Course",199.99, "Fundamental motorcycle training for beginners", 10, 10),
    //MOTO_ADVANCED(2, "Advanced Motorcycle Course", 299.99, "Advanced motorcycle handling and safety techniques", 10, 10),

    CAR_BASIC(3, "Basic Car Driving Course", 349.99, "Essential car driving skills for new drivers", 10, 10),
    //CAR_DEFENSIVE(4, "Defensive Driving Course", 299.99, "Safety-focused defensive driving techniques", 10, 10),
    //CAR_INTENSIVE(5, "Intensive Car Course", 599.99, "Accelerated program for quick licensing", 10, 10),

    //TRUCK_LIGHT(6, "Light Truck Course", 499.99, "Training for driving light commercial vehicles", 10, 10),
    TRUCK_HEAVY(7, "Heavy Truck License Course", 899.99, "Complete training for heavy goods vehicle license", 10, 10);

    private final int id;
    private final String name;
    private final double price;
    //private final TypePermis typePermis;
    private final String description;
    private final int nbreSeanceConduite;
    private final int getNbreSeanceCode;


    CoursePlan(int id, String name, double price, String description, int nbreSeanceConduite, int getNbreSeanceCode) {
        this.id = id;
        this.name = name;
        //this.typePermis = typePermis;
        this.price = price;
        this.description = description;
        this.nbreSeanceConduite = nbreSeanceConduite;
        this.getNbreSeanceCode = getNbreSeanceCode;
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

    public int getNbreSeanceConduite() {        return nbreSeanceConduite;
    }

    public int getGetNbreSeanceCode() {
        return getNbreSeanceCode;
    }

    public int getToatalSeances() {
        return nbreSeanceConduite + getNbreSeanceCode;
    }

    public static TypePermis requiredTypePermis(CoursePlan plan) {
        return switch (plan) {
            case TRUCK_HEAVY -> TypePermis.B;
            case CAR_BASIC  -> TypePermis.A;
            default -> null;
        };
    }

    public static CoursePlan getById(int id) {
        for (CoursePlan plan : values()) {
            if (plan.getId() == id) {
                return plan;
            }
        }
        throw new IllegalArgumentException("No course plan found with ID: " + id);
    }

    public TypePermis getCategory() {
        if (this.name.startsWith("Moto")) {
            return TypePermis.A;
        } else if (this.name.startsWith("Car")) {
            return TypePermis.B;
        } else {
            return TypePermis.C;
        }
    }

}
