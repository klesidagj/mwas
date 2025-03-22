package mk.ukim.finki.wp.kol2024g1.service.impl;

import mk.ukim.finki.wp.kol2024g1.model.Employee;
import mk.ukim.finki.wp.kol2024g1.model.Hotel;
import mk.ukim.finki.wp.kol2024g1.repository.EmployeeRepository;
import mk.ukim.finki.wp.kol2024g1.service.EmployeeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Employee create(String name, String password, Hotel hotel, Boolean isManager) {
        Employee employee = new Employee();
        employee.setName(name);
        employee.setPassword(passwordEncoder.encode(password));
        employee.setHotel(hotel);
        employee.setIsManager(isManager);
        return employeeRepository.save(employee);
    }
}
