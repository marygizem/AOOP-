//Name: CG SEGUNDO
//Date:
//Description: 


package model;


public class Allowance {
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;

    public Allowance(double riceSubsidy, double phoneAllowance, double clothingAllowance) {
        this.riceSubsidy = riceSubsidy;
        this.phoneAllowance = phoneAllowance;
        this.clothingAllowance = clothingAllowance;
    }

    
    //setters
    public void setRiceSubsidy(double riceSubsidy) {
        this.riceSubsidy = riceSubsidy;
    }

    public void setPhoneAllowance(double phoneAllowance) {
        this.phoneAllowance = phoneAllowance;
    }

    public void setClothingAllowance(double clothingAllowance) {
        if (clothingAllowance < 0) {
            throw new IllegalArgumentException("Clothing allowance must be >= 0");
        }
        this.clothingAllowance = clothingAllowance;
    }

    
    // Getters
    public double getRiceSubsidy() {
        return riceSubsidy;
    }

    public double getPhoneAllowance() {
        return phoneAllowance;
    }

    public double getClothingAllowance() {
        return clothingAllowance;
    }
    
    
        public double getTotalAllowance() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }
    
}
