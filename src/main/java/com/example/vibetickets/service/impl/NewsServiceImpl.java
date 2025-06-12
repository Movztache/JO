package com.example.vibetickets.service.impl;

import com.example.vibetickets.model.News;
import com.example.vibetickets.model.UserApp;
import com.example.vibetickets.repository.NewsRepository;
import com.example.vibetickets.service.NewsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implémentation du service de gestion des actualités/articles de blog.
 * Contient la logique métier essentielle pour les opérations sur les articles.
 *
 * @author Vibe-Tickets Team
 */
@Service
@Transactional
public class NewsServiceImpl implements NewsService {

    private final NewsRepository newsRepository;

    @Autowired
    public NewsServiceImpl(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<News> getAllNews(Pageable pageable) {
        return newsRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<News> getPublishedNews(Pageable pageable) {
        return newsRepository.findByPublishedTrue(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public News getNewsById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'article ne peut pas être null");
        }
        return newsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Article non trouvé avec l'ID: " + id));
    }

    @Override
    public News createNews(News news) {
        // Validation des données d'entrée
        validateNews(news);

        // S'assurer que l'ID est null pour une nouvelle entité
        if (news.getId() != null && news.getId() == 0) {
            news.setId(null);
        }

        // Définir la date de création si elle n'est pas définie
        if (news.getCreatedDate() == null) {
            news.setCreatedDate(LocalDateTime.now());
        }

        return newsRepository.save(news);
    }

    @Override
    public News updateNews(Long id, News newsDetails) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'article ne peut pas être null");
        }

        News existingNews = getNewsById(id);

        // Validation des nouvelles données
        validateNewsForUpdate(newsDetails);

        // Mise à jour des champs
        existingNews.setTitle(newsDetails.getTitle());
        existingNews.setDescription(newsDetails.getDescription());
        existingNews.setImageUrl(newsDetails.getImageUrl());
        existingNews.setPublished(newsDetails.isPublished());

        // La date de mise à jour sera automatiquement définie par @PreUpdate

        return newsRepository.save(existingNews);
    }

    @Override
    public void deleteNews(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de l'article ne peut pas être null");
        }

        if (!newsRepository.existsById(id)) {
            throw new EntityNotFoundException("Article non trouvé avec l'ID: " + id);
        }

        newsRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findByTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de recherche ne peut pas être vide");
        }
        return newsRepository.findByTitleContainingIgnoreCase(title.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findByAuthor(UserApp author) {
        if (author == null) {
            throw new IllegalArgumentException("L'auteur ne peut pas être null");
        }
        return newsRepository.findByAuthorOrderByCreatedDateDesc(author);
    }

    @Override
    @Transactional(readOnly = true)
    public List<News> findByDate(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Les dates de début et de fin ne peuvent pas être null");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La date de début doit être antérieure à la date de fin");
        }
        return newsRepository.findByCreatedDateBetweenOrderByCreatedDateDesc(startDate, endDate);
    }

    /**
     * Valide les données d'un article pour la création
     * @param news L'article à valider
     * @throws IllegalArgumentException si les données sont invalides
     */
    private void validateNews(News news) {
        if (news == null) {
            throw new IllegalArgumentException("L'article ne peut pas être null");
        }

        if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'article est obligatoire");
        }

        if (news.getDescription() == null || news.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La description de l'article est obligatoire");
        }

        if (news.getAuthor() == null) {
            throw new IllegalArgumentException("L'auteur de l'article est obligatoire");
        }

        // Validation de la longueur du titre
        if (news.getTitle().length() > 200) {
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 200 caractères");
        }

        // Validation de l'URL de l'image si fournie
        if (news.getImageUrl() != null && news.getImageUrl().length() > 500) {
            throw new IllegalArgumentException("L'URL de l'image ne doit pas dépasser 500 caractères");
        }
    }

    /**
     * Valide les données d'un article pour la mise à jour
     * @param news L'article à valider
     * @throws IllegalArgumentException si les données sont invalides
     */
    private void validateNewsForUpdate(News news) {
        if (news == null) {
            throw new IllegalArgumentException("L'article ne peut pas être null");
        }

        if (news.getTitle() == null || news.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre de l'article est obligatoire");
        }

        if (news.getDescription() == null || news.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La description de l'article est obligatoire");
        }

        // Validation de la longueur du titre
        if (news.getTitle().length() > 200) {
            throw new IllegalArgumentException("Le titre ne doit pas dépasser 200 caractères");
        }

        // Validation de l'URL de l'image si fournie
        if (news.getImageUrl() != null && news.getImageUrl().length() > 500) {
            throw new IllegalArgumentException("L'URL de l'image ne doit pas dépasser 500 caractères");
        }
    }
}
