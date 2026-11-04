package com.third.springboot.employee.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.third.springboot.employee.entity.Employee;
import com.third.springboot.employee.repository.EmployeeRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeServiceImpl(EmployeeRepository repo) {
        this.repo = repo;
    }

    @Override
    public Employee saveEmployee(Employee emp) {
        if (emp.getOrganizationId() == null || emp.getOrganizationId().isBlank()) {
            emp.setOrganizationId("ORG001");
        }
        if (emp.getEntityId() == null || emp.getEntityId().isBlank()) {
            emp.setEntityId("BR001");
        }
        return repo.save(emp);
    }

    @Override
    public List<Employee> getAllEmployees() {
        return repo.findAll();
    }

    @Override
    public Employee getEmployeeById(Long id) {
        return repo.findById(id).orElse(null);
    }

    @Override
    public void deleteEmployee(Long id) {
        repo.deleteById(id);
    }

    @Override
    public List<Employee> getEmployeesByOrg(String organizationId) {
        return repo.findByOrganizationId(organizationId);
    }

    @Override
    public List<Employee> getEmployeesByOrgAndEntity(String organizationId, String entityId) {
        return repo.findByOrganizationIdAndEntityId(organizationId, entityId);
    }

    @Override
    public Employee getEmployeeByIdAndOrg(Long id, String organizationId) {
        return repo.findByIdAndOrganizationId(id, organizationId).orElse(null);
    }
}
