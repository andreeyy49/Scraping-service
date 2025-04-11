package webscraping.crawlerservice.model;

import jakarta.persistence.Index;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "pages")
@Table(indexes = {
        @Index(columnList = "path", name = "idx_path")
})
public class Page {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(columnDefinition = "VARCHAR(255)", nullable = false,
            name = "path")
    private String path;

    @Column(nullable = false)
    private int code;

    @Transient
//    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
}