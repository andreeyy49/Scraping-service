package webscraping.lemmasservice.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lemmas")
@ToString(exclude = "indexes") // Исключаем indexes из toString()
@EqualsAndHashCode(exclude = "indexes") // Исключаем indexes из equals() и hashCode()
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_id")
    private Long siteId;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;

    @Column(nullable = false)
    private Integer frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @BatchSize(size = 500)
    @JsonManagedReference
    private List<Index> indexes = new ArrayList<>();
}
