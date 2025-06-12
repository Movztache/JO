package com.example.vibetickets.repository;

import com.example.vibetickets.model.News;
import com.example.vibetickets.model.UserApp;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour la gestion des actualités/articles de blog.
 * Fournit les méthodes d'accès aux données essentielles pour l'entité News.
 *
 * @author Vibe-Tickets Team
 */
@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    /**
     * Trouve tous les articles publiés avec pagination
     * @param pageable Configuration de pagination
     * @return Page d'articles publiés
     */
    Page<News> findByPublishedTrue(Pageable pageable);

    /**
     * Trouve tous les articles d'un auteur spécifique
     * @param author L'auteur des articles
     * @return Liste des articles de l'auteur
     */
    List<News> findByAuthorOrderByCreatedDateDesc(UserApp author);

    /**
     * Recherche d'articles par titre (insensible à la casse)
     * @param title Le titre à rechercher
     * @return Liste des articles correspondants
     */
    @Query("SELECT n FROM News n WHERE LOWER(n.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY n.createdDate DESC")
    List<News> findByTitleContainingIgnoreCase(@Param("title") String title);

    /**
     * Trouve les articles créés dans une période donnée
     * @param startDate Date de début
     * @param endDate Date de fin
     * @return Liste des articles dans la période
     */
    List<News> findByCreatedDateBetweenOrderByCreatedDateDesc(LocalDateTime startDate, LocalDateTime endDate);
}
