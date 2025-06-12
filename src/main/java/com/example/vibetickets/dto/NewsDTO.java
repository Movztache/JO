package com.example.vibetickets.dto;

import com.example.vibetickets.model.News;
import java.time.LocalDateTime;

/**
 * DTO pour les réponses de l'API News.
 * Évite les références circulaires lors de la sérialisation JSON.
 *
 * @author Vibe-Tickets Team
 */
public class NewsDTO {

    private Long id;
    private String title;
    private String description;
    private AuthorDTO author;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private boolean published;
    private String imageUrl;

    /**
     * DTO pour l'auteur d'un article (sans référence circulaire)
     */
    public static class AuthorDTO {
        private Long userId;
        private String firstName;
        private String lastName;
        private String email;

        public AuthorDTO() {}

        public AuthorDTO(Long userId, String firstName, String lastName, String email) {
            this.userId = userId;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        // Getters et Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    // Constructeur par défaut
    public NewsDTO() {}

    // Constructeur à partir d'une entité News
    public NewsDTO(News news) {
        this.id = news.getId();
        this.title = news.getTitle();
        this.description = news.getDescription();
        this.createdDate = news.getCreatedDate();
        this.updatedDate = news.getUpdatedDate();
        this.published = news.isPublished();
        this.imageUrl = news.getImageUrl();

        // Informations de l'auteur sans référence circulaire
        if (news.getAuthor() != null) {
            this.author = new AuthorDTO(
                news.getAuthor().getUserId(),
                news.getAuthor().getFirstName(),
                news.getAuthor().getLastName(),
                news.getAuthor().getEmail()
            );
        }
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

    public AuthorDTO getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDTO author) {
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
}
