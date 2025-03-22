package mk.ukim.finki.wp.kol2024g1.service;

import mk.ukim.finki.wp.kol2024g1.model.Employee;
import mk.ukim.finki.wp.kol2024g1.model.Hotel;

public interface EmployeeService {
    Employee create(String name, String password, Hotel hotel, Boolean isManager);
}
