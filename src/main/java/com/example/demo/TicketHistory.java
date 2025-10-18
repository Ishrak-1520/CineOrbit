package com.example.demo;

import javafx.beans.property.*;

public class TicketHistory {
    private final StringProperty date;
    private final StringProperty movie;
    private final StringProperty screening;
    private final StringProperty seats;
    private final DoubleProperty amount;
    private final StringProperty status;
    private final StringProperty action;

    public TicketHistory(String date, String movie, String screening, String seats, Double amount, String status, String action) {
        this.date = new SimpleStringProperty(date);
        this.movie = new SimpleStringProperty(movie);
        this.screening = new SimpleStringProperty(screening);
        this.seats = new SimpleStringProperty(seats);
        this.amount = new SimpleDoubleProperty(amount);
        this.status = new SimpleStringProperty(status);
        this.action = new SimpleStringProperty(action);
    }

    // Property getters
    public StringProperty dateProperty() { return date; }
    public StringProperty movieProperty() { return movie; }
    public StringProperty screeningProperty() { return screening; }
    public StringProperty seatsProperty() { return seats; }
    public DoubleProperty amountProperty() { return amount; }
    public StringProperty statusProperty() { return status; }
    public StringProperty actionProperty() { return action; }

    // Value getters
    public String getDate() { return date.get(); }
    public String getMovie() { return movie.get(); }
    public String getScreening() { return screening.get(); }
    public String getSeats() { return seats.get(); }
    public Double getAmount() { return amount.get(); }
    public String getStatus() { return status.get(); }
    public String getAction() { return action.get(); }

    // Value setters
    public void setDate(String value) { date.set(value); }
    public void setMovie(String value) { movie.set(value); }
    public void setScreening(String value) { screening.set(value); }
    public void setSeats(String value) { seats.set(value); }
    public void setAmount(Double value) { amount.set(value); }
    public void setStatus(String value) { status.set(value); }
    public void setAction(String value) { action.set(value); }

    @Override
    public String toString() {
        return String.format("Ticket[movie=%s, date=%s, amount=%.2f]", getMovie(), getDate(), getAmount());
    }
} 