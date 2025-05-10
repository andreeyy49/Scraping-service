package webscraping.entityvaultservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "blog")
public class Blog implements Serializable {

    @Id
    private UUID id;

    @Column(name = "blog_text")
    private String blogText;

    private String path;

    @Column(name = "parse_time")
    private Date parseTime;

    @Column(name = "site_id")
    private long siteId;

    private String hash;

    @ElementCollection
    @CollectionTable(name = "blog_key_words", joinColumns = @JoinColumn(name = "blog_id"))
    @Column(name = "key_word")
    private List<String> keyWords;

    @ElementCollection
    @CollectionTable(name = "blog_images", joinColumns = @JoinColumn(name = "blog_id"))
    @Column(name = "image_url")
    private List<String> images;
}
