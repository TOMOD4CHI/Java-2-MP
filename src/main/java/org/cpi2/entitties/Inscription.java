package org.cpi2.entitties;

import java.util.Date;

public class Inscription {
    private int id;
    private String cin;
    private Plan plan;
    private boolean paymentStatus;
    private String status;
    private Date netPaymentDate;

    public Inscription() {
    }

    public Inscription(int id, String cin, Plan plan, boolean paymentStatus, String status, java.util.Date netPaymentDate) {
        this.id = id;
        this.cin = cin;
        this.plan = plan;
        this.paymentStatus = paymentStatus;
        this.status = status;
        this.netPaymentDate = netPaymentDate;
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

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
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

    public java.util.Date getNetPaymentDate() {
        return netPaymentDate;
    }

    public void setNetPaymentDate(java.util.Date netPaymentDate) {
        this.netPaymentDate = netPaymentDate;
    }

    @Override
    public String toString() {
        return "Inscription{" +
                "id=" + id +
                ", cin='" + cin + '\'' +
                ", plan=" + plan +
                ", paymentStatus=" + paymentStatus +
                ", status='" + status + '\'' +
                ", netPaymentDate=" + netPaymentDate +
                '}';
    }
}