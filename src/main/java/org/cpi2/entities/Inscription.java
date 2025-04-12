package org.cpi2.entities;

import java.sql.Date;
import java.time.LocalDate;

public class Inscription {
    private int id;
    private Date inscriptioDate;
    private String cin;
    private CoursePlan plan;
    private boolean paymentStatus;
    private String status;
    private Date nextPaymentDate;
    private String paymentCycle;

    public Inscription() {
    }

    public Inscription(int id, String cin, CoursePlan plan, boolean paymentStatus, String status, Date nextPaymentDate, String paymentCycle,Date inscriptioDate) {
        this.id = id;
        this.cin = cin;
        this.plan = plan;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.nextPaymentDate = nextPaymentDate;
        this.paymentCycle=paymentCycle;
        this.inscriptioDate=inscriptioDate;
    }

    public String getPaymentCycle() {
        return paymentCycle;
    }

    public void setPaymentCycle(String paymentCycle) {
        this.paymentCycle = paymentCycle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public CoursePlan getPlan() {
        return plan;
    }

    public void setPlan(CoursePlan plan) {
        this.plan = plan;
    }

    public boolean isPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(boolean paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getnextPaymentDate() {
        return nextPaymentDate;
    }

    public void setnextPaymentDate(Date nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }

    public Date getInscriptioDate() {
        return inscriptioDate;
    }

    public void setInscriptioDate(Date inscriptioDate) {
        this.inscriptioDate = inscriptioDate;
    }

    //Can make Enum if needed
    public boolean isActive() {
        return status.equalsIgnoreCase("En cours");
    }

    @Override
    public String toString() {
        return "Inscription{" +
                "id=" + id +
                ", cin='" + cin + '\'' +
                ", plan=" + plan +
                ", paymentStatus=" + paymentStatus +
                ", status='" + status + '\'' +
                ", nextPaymentDate=" + nextPaymentDate +
                '}';
    }
    public double getAmount(){
        return this.plan.getPrice();
    }
}