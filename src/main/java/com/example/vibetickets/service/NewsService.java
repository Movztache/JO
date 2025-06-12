package com.example.vibetickets.service;

import com.example.vibetickets.model.News;
import com.example.vibetickets.model.UserApp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface de service pour la gestion des actualités/articles de blog.
 * Définit les opérations métier essentielles pour les articles.
 *
 * @author Vibe-Tickets Team
 */
public interface NewsService {

    /**
     * Récupère tous les articles avec pagination
     * @param pageable Configuration de pagination
     * @return Page d'articles
     */
    Page<News> getAllNews(Pageable pageable);

    /**
     * Récupère tous les articles publiés avec pagination
     * @param pageable Configuration de pagination
     * @return Page d'articles publiés
     */
    Page<News> getPublishedNews(Pageable pageable);

    /**
     * Récupère un article par son ID
     * @param id L'ID de l'article
     * @return L'article trouvé
     * @throws jakarta.persistence.EntityNotFoundException si l'article n'existe pas
     */
    News getNewsById(Long id);

    /**
     * Crée un nouvel article
     * @param news L'article à créer
     * @return L'article créé
     * @throws IllegalArgumentException si les données sont invalides
     */
    News createNews(News news);

    /**
     * Met à jour un article existant
     * @param id L'ID de l'article à mettre à jour
     * @param newsDetails Les nouvelles données de l'article
     * @return L'article mis à jour
     * @throws jakarta.persistence.EntityNotFoundException si l'article n'existe pas
     */
    News updateNews(Long id, News newsDetails);

    /**
     * Supprime un article
     * @param id L'ID de l'article à supprimer
     * @throws jakarta.persistence.EntityNotFoundException si l'article n'existe pas
     */
    void deleteNews(Long id);

    /**
     * Recherche d'articles par titre
     * @param title Le titre à rechercher
     * @return Liste des articles correspondants
     */
    List<News> findByTitle(String title);

    /**
     * Récupère les articles d'un auteur spécifique
     * @param author L'auteur des articles
     * @return Liste des articles de l'auteur
     */
    List<News> findByAuthor(UserApp author);

    /**
     * Récupère les articles créés dans une période donnée
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des articles dans la période
     */
    List<News> findByDate(LocalDateTime startDate, LocalDateTime endDate);
}
