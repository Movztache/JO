package com.example.vibetickets.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Entité représentant un article de blog/actualité dans le système Vibe-Tickets.
 * Cette classe gère les articles d'actualités créés par les administrateurs.
 * 
 * @author Vibe-Tickets Team
 */
@Entity
@Table(name = "news")
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    @Column(nullable = false, length = 200)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnoreProperties({"role", "userApps", "password", "userKey"})
    private UserApp author;

    @Column(name = "created_date", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedDate;

    @Column(nullable = false)
    private boolean published = true;

    @Size(max = 500, message = "L'URL de l'image ne doit pas dépasser 500 caractères")
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Constructeurs
    public News() {
        this.createdDate = LocalDateTime.now();
    }

    public News(String title, String description, UserApp author) {
        this();
        this.title = title;
        this.description = description;
        this.author = author;
    }

    public News(String title, String description, UserApp author, String imageUrl) {
        this(title, description, author);
        this.imageUrl = imageUrl;
    }

    // Méthodes de cycle de vie JPA
    @PrePersist
    protected void onCreate() {
        if (createdDate == null) {
            createdDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UserApp getAuthor() {
        return author;
    }

    public void setAuthor(UserApp author) {
        this.author = author;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // Méthodes utilitaires
    /**
     * Vérifie si l'article a été modifié après sa création
     * @return true si l'article a été modifié, false sinon
     */
    public boolean isModified() {
        return updatedDate != null;
    }

    /**
     * Retourne le nom complet de l'auteur
     * @return Le nom complet de l'auteur ou "Auteur inconnu" si l'auteur est null
     */
    public String getAuthorFullName() {
        if (author != null) {
            return author.getFirstName() + " " + author.getLastName();
        }
        return "Auteur inconnu";
    }

    @Override
    public String toString() {
        return "News{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author=" + (author != null ? author.getEmail() : "null") +
                ", published=" + published +
                ", createdDate=" + createdDate +
                '}';
    }
}
