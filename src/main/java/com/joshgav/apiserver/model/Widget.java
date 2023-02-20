package com.joshgav.apiserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Timestamp;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="widgets")
@Entity
public class Widget {
    @Id
    @Column(name = "id")
    @JsonProperty("id")
    private String id;

    @Column(name = "type")
    @JsonProperty("type")
    private String type;

    @Column(name = "modelName")
    @JsonProperty("modelName")
    private String modelName;

    @CreationTimestamp
    @Column(name = "created", updatable = false)
    @JsonProperty("created")
    private Timestamp created;

    @UpdateTimestamp
    @Column(name = "updated")
    @JsonProperty("updated")
    private Timestamp updated;
}
