package com.third.springboot.employee.service;

import java.util.List;
import com.third.springboot.employee.entity.Employee;

public interface EmployeeService {
    Employee saveEmployee(Employee emp);
    List<Employee> getAllEmployees();
    Employee getEmployeeById(Long id);
    void deleteEmployee(Long id);

    // Multi-tenant and entity-scoped methods
    List<Employee> getEmployeesByOrg(String organizationId);
    List<Employee> getEmployeesByOrgAndEntity(String organizationId, String entityId);
    Employee getEmployeeByIdAndOrg(Long id, String organizationId);
}
