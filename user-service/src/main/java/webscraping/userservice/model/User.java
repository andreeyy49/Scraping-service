package webscraping.userservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    private String country;

    private String city;

    private String phone;

    private String photo;

}
