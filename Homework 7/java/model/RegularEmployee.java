//Name: CG SEGUNDO
//Date:
//Description: 


package model;

import java.util.Date;


        public class RegularEmployee  extends Employee {

            public RegularEmployee(double basicSalary, int empID, String lastName, String firstName, Date birtday, String address, String phoneNumber, String emailAdd, GovernmentDetails governmentDetails, Allowance allowance, Deduction deduction) {
                super(empID, lastName, firstName, birtday, address, phoneNumber, emailAdd, governmentDetails, allowance, deduction);
                this.basicSalary = basicSalary;  // Set parent's protected field
            }




            @Override
            public double calculateSalary() {
                return basicSalary + allowance.getTotalAllowance();
            }
    
}
