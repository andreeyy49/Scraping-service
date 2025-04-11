package webscraping.authservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Entity
@Table(name = "auth_users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    @Id
    private UUID id;

    @Column(name = "user_name")
    private String username;

    private String email;

    private String password;

}
