package com.example.vibetickets.controller;

import com.example.vibetickets.dto.NewsDTO;
import com.example.vibetickets.model.News;
import com.example.vibetickets.model.UserApp;
import com.example.vibetickets.service.NewsService;
import com.example.vibetickets.service.UserAppService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des actualités/articles de blog.
 * Fournit les endpoints pour les opérations CRUD sur les articles.
 * 
 * @author Vibe-Tickets Team
 */
@RestController
@RequestMapping("/api/news")
@CrossOrigin(origins = "*")
public class NewsController {

    private final NewsService newsService;
    private final UserAppService userAppService;

    @Autowired
    public NewsController(NewsService newsService, UserAppService userAppService) {
        this.newsService = newsService;
        this.userAppService = userAppService;
    }

    /**
     * Récupère tous les articles avec pagination
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de page (défaut: 10)
     * @param sortBy Champ de tri (défaut: createdDate)
     * @param sortDir Direction du tri (défaut: desc)
     * @return Page d'articles
     */
    @GetMapping
    public ResponseEntity<?> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<News> newsPage = newsService.getAllNews(pageable);
            Page<NewsDTO> newsDTOPage = newsPage.map(NewsDTO::new);
            return ResponseEntity.ok(newsDTOPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère tous les articles publiés avec pagination
     * @param page Numéro de page (défaut: 0)
     * @param size Taille de page (défaut: 10)
     * @return Page d'articles publiés
     */
    @GetMapping("/published")
    public ResponseEntity<?> getPublishedNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Sort sort = Sort.by("createdDate").descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<News> newsPage = newsService.getPublishedNews(pageable);
            Page<NewsDTO> newsDTOPage = newsPage.map(NewsDTO::new);
            return ResponseEntity.ok(newsDTOPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère un article par son ID
     * @param id L'ID de l'article
     * @return L'article trouvé
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getNewsById(@PathVariable Long id) {
        try {
            News news = newsService.getNewsById(id);
            NewsDTO newsDTO = new NewsDTO(news);
            return ResponseEntity.ok(newsDTO);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }



    /**
     * Crée un nouvel article (réservé aux Administrateurs)
     * @param news L'article à créer
     * @param authorId L'ID de l'auteur
     * @return L'article créé
     */
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> createNews(@Valid @RequestBody News news, @RequestParam Long authorId) {
        try {
            // Récupération de l'auteur
            UserApp author = userAppService.findById(authorId);
            if (author == null) {
                throw new EntityNotFoundException("Auteur non trouvé avec l'ID: " + authorId);
            }
            news.setAuthor(author);
            
            News createdNews = newsService.createNews(news);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNews);
        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Auteur non trouvé");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Données invalides");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne");
            error.put("message", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Met à jour un article existant (réservé aux Administrateurs)
     * @param id L'ID de l'article à mettre à jour
     * @param newsDetails Les nouvelles données de l'article
     * @return L'article mis à jour
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> updateNews(@PathVariable Long id, @Valid @RequestBody News newsDetails) {
        try {
            News updatedNews = newsService.updateNews(id, newsDetails);
            NewsDTO newsDTO = new NewsDTO(updatedNews);
            return ResponseEntity.ok(newsDTO);
        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Article non trouvé");
            error.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Données invalides");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne");
            error.put("message", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Supprime un article (réservé aux Administrateurs)
     * @param id L'ID de l'article à supprimer
     * @return Confirmation de suppression
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<?> deleteNews(@PathVariable Long id) {
        try {
            newsService.deleteNews(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Article supprimé avec succès");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Article non trouvé");
            error.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur interne");
            error.put("message", "Une erreur inattendue s'est produite");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }



    /**
     * Recherche d'articles par titre
     * @param title Le titre à rechercher
     * @return Liste des articles correspondants
     */
    @GetMapping("/search/title")
    public ResponseEntity<?> searchByTitle(@RequestParam String title) {
        try {
            List<News> newsList = newsService.findByTitle(title);
            List<NewsDTO> newsDTOList = newsList.stream().map(NewsDTO::new).toList();
            return ResponseEntity.ok(newsDTOList);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Paramètre invalide");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les articles d'un auteur spécifique
     * @param authorId L'ID de l'auteur
     * @return Liste des articles de l'auteur
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<?> getNewsByAuthor(@PathVariable Long authorId) {
        try {
            UserApp author = userAppService.findById(authorId);
            if (author == null) {
                throw new EntityNotFoundException("Auteur non trouvé avec l'ID: " + authorId);
            }

            List<News> newsList = newsService.findByAuthor(author);
            List<NewsDTO> newsDTOList = newsList.stream().map(NewsDTO::new).toList();
            return ResponseEntity.ok(newsDTOList);
        } catch (EntityNotFoundException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Auteur non trouvé");
            error.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupère les articles créés dans une période donnée
     * @param startDate Date de début (format: yyyy-MM-dd'T'HH:mm:ss)
     * @param endDate Date de fin (format: yyyy-MM-dd'T'HH:mm:ss)
     * @return Liste des articles dans la période
     */
    @GetMapping("/search/date")
    public ResponseEntity<?> getNewsByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            LocalDateTime start = LocalDateTime.parse(startDate, formatter);
            LocalDateTime end = LocalDateTime.parse(endDate, formatter);

            List<News> newsList = newsService.findByDate(start, end);
            List<NewsDTO> newsDTOList = newsList.stream().map(NewsDTO::new).toList();
            return ResponseEntity.ok(newsDTOList);
        } catch (DateTimeParseException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Format de date invalide");
            error.put("message", "Utilisez le format: yyyy-MM-dd'T'HH:mm:ss");
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Paramètre invalide");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
