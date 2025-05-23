package se.reviewservice.dto;

public class Group6Review {
    // EXAKT de 4 fält som Grupp 6 har specificerat
    private Integer snittbetyg;           // Snittbetyg 1-5 (stjärnor)
    private String skriftligReview;       // Skriftlig review
    private String namnPaReviewer;        // Namn på reviewer
    private String datumTid;              // Datum/tid när review skrevs

    // Konstruktorer
    public Group6Review() {}

    public Group6Review(Integer snittbetyg, String skriftligReview, String namnPaReviewer, String datumTid) {
        this.snittbetyg = snittbetyg;
        this.skriftligReview = skriftligReview;
        this.namnPaReviewer = namnPaReviewer;
        this.datumTid = datumTid;
    }

    // Getters och Setters
    public Integer getSnittbetyg() {
        return snittbetyg;
    }

    public void setSnittbetyg(Integer snittbetyg) {
        this.snittbetyg = snittbetyg;
    }

    public String getSkriftligReview() {
        return skriftligReview;
    }

    public void setSkriftligReview(String skriftligReview) {
        this.skriftligReview = skriftligReview;
    }

    public String getNamnPaReviewer() {
        return namnPaReviewer;
    }

    public void setNamnPaReviewer(String namnPaReviewer) {
        this.namnPaReviewer = namnPaReviewer;
    }

    public String getDatumTid() {
        return datumTid;
    }

    public void setDatumTid(String datumTid) {
        this.datumTid = datumTid;
    }
}