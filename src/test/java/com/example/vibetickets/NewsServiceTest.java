package com.example.vibetickets;

import com.example.vibetickets.model.News;
import com.example.vibetickets.model.UserApp;
import com.example.vibetickets.repository.NewsRepository;
import com.example.vibetickets.service.impl.NewsServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour le service NewsService.
 * Vérifie le bon fonctionnement des opérations CRUD sur les articles.
 * 
 * @author Vibe-Tickets Team
 */
class NewsServiceTest {

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsServiceImpl newsService;

    private News testNews;
    private UserApp testAuthor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Création d'un auteur de test
        testAuthor = new UserApp();
        testAuthor.setUserId(1L);
        testAuthor.setFirstName("Jean");
        testAuthor.setLastName("Dupont");
        testAuthor.setEmail("jean.dupont@example.com");
        
        // Création d'un article de test
        testNews = new News();
        testNews.setId(1L);
        testNews.setTitle("Article de test");
        testNews.setDescription("Description de l'article de test");
        testNews.setAuthor(testAuthor);
        testNews.setPublished(true);
        testNews.setCreatedDate(LocalDateTime.now());
    }

    @Test
    void testGetAllNews() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<News> newsList = Arrays.asList(testNews);
        Page<News> newsPage = new PageImpl<>(newsList, pageable, 1);
        
        when(newsRepository.findAll(pageable)).thenReturn(newsPage);
        
        // Act
        Page<News> result = newsService.getAllNews(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testNews.getTitle(), result.getContent().get(0).getTitle());
        verify(newsRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetPublishedNews() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<News> newsList = Arrays.asList(testNews);
        Page<News> newsPage = new PageImpl<>(newsList, pageable, 1);
        
        when(newsRepository.findByPublishedTrue(pageable)).thenReturn(newsPage);
        
        // Act
        Page<News> result = newsService.getPublishedNews(pageable);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).isPublished());
        verify(newsRepository, times(1)).findByPublishedTrue(pageable);
    }

    @Test
    void testGetNewsById_Success() {
        // Arrange
        when(newsRepository.findById(1L)).thenReturn(Optional.of(testNews));
        
        // Act
        News result = newsService.getNewsById(1L);
        
        // Assert
        assertNotNull(result);
        assertEquals(testNews.getTitle(), result.getTitle());
        assertEquals(testNews.getId(), result.getId());
        verify(newsRepository, times(1)).findById(1L);
    }

    @Test
    void testGetNewsById_NotFound() {
        // Arrange
        when(newsRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            newsService.getNewsById(999L);
        });
        verify(newsRepository, times(1)).findById(999L);
    }

    @Test
    void testGetNewsById_NullId() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.getNewsById(null);
        });
        verify(newsRepository, never()).findById(any());
    }

    @Test
    void testCreateNews_Success() {
        // Arrange
        News newNews = new News();
        newNews.setTitle("Nouvel article");
        newNews.setDescription("Description du nouvel article");
        newNews.setAuthor(testAuthor);

        when(newsRepository.save(any(News.class))).thenReturn(newNews);

        // Act
        News result = newsService.createNews(newNews);

        // Assert
        assertNotNull(result);
        assertEquals("Nouvel article", result.getTitle());
        verify(newsRepository, times(1)).save(newNews);
    }



    @Test
    void testCreateNews_NullNews() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.createNews(null);
        });
        verify(newsRepository, never()).save(any());
    }

    @Test
    void testCreateNews_EmptyTitle() {
        // Arrange
        News newNews = new News();
        newNews.setTitle("");
        newNews.setDescription("Description");
        newNews.setAuthor(testAuthor);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.createNews(newNews);
        });
        verify(newsRepository, never()).save(any());
    }

    @Test
    void testCreateNews_NullAuthor() {
        // Arrange
        News newNews = new News();
        newNews.setTitle("Titre");
        newNews.setDescription("Description");
        newNews.setAuthor(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.createNews(newNews);
        });
        verify(newsRepository, never()).save(any());
    }

    @Test
    void testUpdateNews_Success() {
        // Arrange
        News updatedNews = new News();
        updatedNews.setTitle("Titre mis à jour");
        updatedNews.setDescription("Description mise à jour");
        updatedNews.setPublished(false);

        when(newsRepository.findById(1L)).thenReturn(Optional.of(testNews));
        when(newsRepository.save(any(News.class))).thenReturn(testNews);

        // Act
        News result = newsService.updateNews(1L, updatedNews);

        // Assert
        assertNotNull(result);
        verify(newsRepository, times(1)).findById(1L);
        verify(newsRepository, times(1)).save(testNews);
    }

    @Test
    void testDeleteNews_Success() {
        // Arrange
        when(newsRepository.existsById(1L)).thenReturn(true);
        doNothing().when(newsRepository).deleteById(1L);
        
        // Act
        newsService.deleteNews(1L);
        
        // Assert
        verify(newsRepository, times(1)).existsById(1L);
        verify(newsRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteNews_NotFound() {
        // Arrange
        when(newsRepository.existsById(999L)).thenReturn(false);
        
        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            newsService.deleteNews(999L);
        });
        verify(newsRepository, times(1)).existsById(999L);
        verify(newsRepository, never()).deleteById(any());
    }

    @Test
    void testFindByTitle() {
        // Arrange
        List<News> searchResults = Arrays.asList(testNews);
        when(newsRepository.findByTitleContainingIgnoreCase("test")).thenReturn(searchResults);

        // Act
        List<News> result = newsService.findByTitle("test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNews.getTitle(), result.get(0).getTitle());
        verify(newsRepository, times(1)).findByTitleContainingIgnoreCase("test");
    }

    @Test
    void testFindByTitle_EmptyTerm() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.findByTitle("");
        });
        verify(newsRepository, never()).findByTitleContainingIgnoreCase(any());
    }

    @Test
    void testFindByAuthor() {
        // Arrange
        List<News> authorNews = Arrays.asList(testNews);
        when(newsRepository.findByAuthorOrderByCreatedDateDesc(testAuthor)).thenReturn(authorNews);

        // Act
        List<News> result = newsService.findByAuthor(testAuthor);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testNews.getTitle(), result.get(0).getTitle());
        verify(newsRepository, times(1)).findByAuthorOrderByCreatedDateDesc(testAuthor);
    }

    @Test
    void testFindByAuthor_NullAuthor() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.findByAuthor(null);
        });
        verify(newsRepository, never()).findByAuthorOrderByCreatedDateDesc(any());
    }

    @Test
    void testFindByDate() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<News> dateNews = Arrays.asList(testNews);
        when(newsRepository.findByCreatedDateBetweenOrderByCreatedDateDesc(startDate, endDate)).thenReturn(dateNews);

        // Act
        List<News> result = newsService.findByDate(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(newsRepository, times(1)).findByCreatedDateBetweenOrderByCreatedDateDesc(startDate, endDate);
    }

    @Test
    void testFindByDate_InvalidDates() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = LocalDateTime.now().minusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            newsService.findByDate(startDate, endDate);
        });
        verify(newsRepository, never()).findByCreatedDateBetweenOrderByCreatedDateDesc(any(), any());
    }
}
