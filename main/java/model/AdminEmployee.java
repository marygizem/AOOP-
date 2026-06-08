package model;

import java.util.Date;

public class AdminEmployee extends RegularEmployee {
    public AdminEmployee(double basicSalary, int empID, String lastName, String firstName, Date birtday,
                         String address, String phoneNumber, String emailAdd,
                         GovernmentDetails governmentDetails, Allowance allowance, Deduction deduction) {
        super(basicSalary, empID, lastName, firstName, birtday, address, phoneNumber, emailAdd,
                governmentDetails, allowance, deduction);
    }

    @Override
    public double computeTax() {
        return super.computeTax();
    }
}