package model;

public class Deduction {
    private double sss;
    private double philhealth;
    private double pagibig;
    private double tax;

    public Deduction(double sss, double philhealth, double pagibig, double tax) {
        this.sss = sss;
        this.philhealth = philhealth;
        this.pagibig = pagibig;
        this.tax = tax;
    }

    
    //getters
    public double getSss() {
        return sss;
    }

    public double getPhilhealth() {
        return philhealth;
    }

    public double getPagibig() {
        return pagibig;
    }

    public double getTax() {
        return tax;
    }

    
    //setters
    public void setSss(double sss) {
        setSssDeduction(sss);
    }

    public void setSssDeduction(double sssDeduction) {
        if (sssDeduction < 0) {
            throw new IllegalArgumentException("SSS deduction must be >= 0");
        }
        this.sss = sssDeduction;
    }

    public void setPhilhealth(double philhealth) {
        this.philhealth = philhealth;
    }

    public void setPagibig(double pagibig) {
        this.pagibig = pagibig;
    }

    public void setTax(double tax) {
        this.tax = tax;
    }
    

    public double getTotalDeduction() {
        return sss + philhealth + pagibig + tax;
    }
}