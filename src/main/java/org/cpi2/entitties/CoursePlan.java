package org.cpi2.entitties;

import java.util.HashMap;
import java.util.Map;

public enum CoursePlan {
    MOTO_BASIC(1, "Basic Motorcycle Course", 199.99, "Fundamental motorcycle training for beginners", 10, 10),
    MOTO_ADVANCED(2, "Advanced Motorcycle Course", 299.99, "Advanced motorcycle handling and safety techniques", 10, 10),

    CAR_BASIC(3, "Basic Car Driving Course", 349.99, "Essential car driving skills for new drivers", 10, 10),
    CAR_DEFENSIVE(4, "Defensive Driving Course", 299.99, "Safety-focused defensive driving techniques", 10, 10),
    CAR_INTENSIVE(5, "Intensive Car Course", 599.99, "Accelerated program for quick licensing", 10, 10),

    TRUCK_LIGHT(6, "Light Truck Course", 499.99, "Training for driving light commercial vehicles", 10, 10),
    TRUCK_HEAVY(7, "Heavy Truck License Course", 899.99, "Complete training for heavy goods vehicle license", 10, 10);

    private final int id;
    private final String name;
    private final double price;
    private final String description;
    private final int nbreSeanceConduite;
    private final int getNbreSeanceCode;

    private static final Map<Integer, CoursePlan> lookup = new HashMap<>();

    static {
        for (CoursePlan plan : values()) {
            lookup.put(plan.getId(), plan);
        }
    }

    CoursePlan(int id, String name, double price, String description, int nbreSeanceConduite, int getNbreSeanceCode) {
        this.id = id;
        this.name = name;
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

    public static CoursePlan getById(int id) {
        return lookup.getOrDefault(id, null);
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
