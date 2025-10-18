package com.example.demo;

public class Customer {
    private String ticketNumber;
    private String movieTitle;
    private String genre;
    private String tableNo;
    private String time;
    private String date;
    private String totalPayment;

    public Customer(String ticketNumber, String movieTitle, String genre, String tableNo, String time, String date, String totalPayment) {
        this.ticketNumber = ticketNumber;
        this.movieTitle = movieTitle;
        this.genre = genre;
        this.tableNo = tableNo;
        this.time = time;
        this.date = date;
        this.totalPayment = totalPayment;
    }

    // Getters
    public String getTicketNumber() { return ticketNumber; }
    public String getMovieTitle() { return movieTitle; }
    public String getGenre() { return genre; }
    public String getTableNo() { return tableNo; }
    public String getTime() { return time; }
    public String getDate() { return date; }
    public String getTotalPayment() { return totalPayment; }

    // Setters
    public void setTicketNumber(String ticketNumber) { this.ticketNumber = ticketNumber; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setTableNo(String tableNo) { this.tableNo = tableNo; }
    public void setTime(String time) { this.time = time; }
    public void setDate(String date) { this.date = date; }
    public void setTotalPayment(String totalPayment) { this.totalPayment = totalPayment; }
}
