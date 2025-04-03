package org.mirza.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "posts")
@AllArgsConstructor
@NoArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;
    @NotNull(message = "title must not be null")
    private String title;
    @NotNull(message = "content must not be null")
    private String content;
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")    private Date updatedAt;

    public Post(Integer id, String title, String content, boolean isDeleted) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.isDeleted = isDeleted;
    }
}
