package webscraping.lemmasservice.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "indexes")
@JsonIgnoreProperties({"lemma"})
@ToString(exclude = "lemma") // Исключаем lemma из toString()
@EqualsAndHashCode(exclude = "lemma") // Исключаем lemma из equals() и hashCode()
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "page_id")
    private UUID pageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lemma_id")
    @JsonBackReference
    private Lemma lemma;

    @Column(nullable = false, name = "`rank`")
    private Integer rank;
}
