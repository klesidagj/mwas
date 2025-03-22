package mk.ukim.finki.wp.kol2024g1.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@Entity
@Table(name = "app_user")
public class Employee {

    public Employee() {

    }

    public Employee(String name, String password, Hotel hotel, boolean isManager) {
        this.name = name;
        this.password = password;
        this.hotel = hotel;
        this.isManager = isManager;
    }

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String password;

    @ManyToOne
    private Hotel hotel;

    private Boolean isManager;

}
