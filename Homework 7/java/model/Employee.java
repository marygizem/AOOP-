package model;

import java.util.Date;

public abstract class Employee {
  protected static final double DEFAULT_SSS_RATE = 0.05;
  protected static final double DEFAULT_PHILHEALTH_RATE = 0.03;
  protected static final double DEFAULT_PAGIBIG_RATE = 0.02;
  protected static final double DEFAULT_TAX_RATE = 0.08;

    protected int empID;
                protected String lastName;
                protected String firstName;
                protected String position;
                protected String department;
                protected double basicSalary;
                protected Date birtday;
                protected String address;
                protected String phoneNumber;
                protected  String emailAdd;
                protected GovernmentDetails governmentDetails;
                protected Allowance allowance;
               protected Deduction deduction;
                
                
    public Employee(int empID, String lastName, String firstName, Date birtday, String address, String phoneNumber, String emailAdd,
                                            GovernmentDetails governmentDetails,
                                            Allowance allowance,
                                            Deduction deduction) {
        this.empID = empID;
        this.lastName = lastName;
        this.firstName = firstName;
        this.birtday = birtday;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.emailAdd = emailAdd;
        this.governmentDetails = governmentDetails;
        this.allowance = allowance;
        this.deduction = deduction;
    }

    public int getEmpID() {
        return empID;
    }
    
    public String getEmployeeNumber() {
        return String.valueOf(empID);
    }
    
    public double getBasicSalary() {
        return basicSalary;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public String getPosition() {
        return position;
    }
    
    public String getDepartment() {
        return department;
    }

    public void setBasicSalary(double basicSalary) {
      if (basicSalary < 0) {
        throw new IllegalArgumentException("Salary must be >= 0");
      }
      this.basicSalary = basicSalary;
    }

    public void setPosition(String position) {
      if (position == null || position.trim().isEmpty()) {
        throw new IllegalArgumentException("Position required");
      }
      this.position = position;
    }

    public void setDepartment(String department) {
      if (department == null || department.trim().isEmpty()) {
        throw new IllegalArgumentException("Department required");
      }
      this.department = department;
    }
                
    
    public String getFullName( ){
        return firstName + " " + lastName; 
    }
    
      public GovernmentDetails getGovernmentDetails() {
        return governmentDetails;
    }
      public Allowance getAllowance() {
        return allowance;
    }    
      
          public Deduction getDeduction() {
        return deduction;
    }

        public void validatePayrollInputs() {
          if (basicSalary < 0) {
            throw new IllegalStateException("Basic salary must be >= 0");
          }
          if (allowance == null) {
            throw new IllegalStateException("Allowance data is required");
          }
        }

        public double computeGross() {
          validatePayrollInputs();
          return basicSalary + allowance.getTotalAllowance();
        }

        public double computeSSS() {
          if (deduction != null) {
            return deduction.getSss();
          }
          return computeGross() * DEFAULT_SSS_RATE;
        }

        public double computePhilHealth() {
          if (deduction != null) {
            return deduction.getPhilhealth();
          }
          return computeGross() * DEFAULT_PHILHEALTH_RATE;
        }

        public double computePagIBIG() {
          if (deduction != null) {
            return deduction.getPagibig();
          }
          return computeGross() * DEFAULT_PAGIBIG_RATE;
        }

        public double computeTax() {
          if (deduction != null) {
            return deduction.getTax();
          }
          return computeGross() * DEFAULT_TAX_RATE;
        }

        public double computeTotalDeductions() {
          return computeSSS() + computePhilHealth() + computePagIBIG() + computeTax();
        }

        public double computeNetPay() {
          return computeGross() - computeTotalDeductions();
        }
          
      
    // Abstract
    public  abstract  double calculateSalary();
       
   
}
