import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void When_SaveNewDocument_Expect_DocumentSavedWithGeneratedId() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Test Document")
                .content("This is a test document")
                .author(DocumentManager.Author.builder().id("1").name("John Doe").build())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId());
        assertEquals("Test Document", savedDocument.getTitle());
        assertEquals("This is a test document", savedDocument.getContent());
        assertEquals("1", savedDocument.getAuthor().getId());
        assertEquals("John Doe", savedDocument.getAuthor().getName());
    }

    @Test
    void When_SaveDocumentWithExistingId_Expect_DocumentSavedWithoutChangingId() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id("existing-id")
                .title("Test Document")
                .content("This is a test document")
                .author(DocumentManager.Author.builder().id("1").name("John Doe").build())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertEquals("existing-id", savedDocument.getId());
    }

    @Test
    void When_FindExistingDocumentById_Expect_DocumentReturned() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Test Document")
                .content("This is a test document")
                .author(DocumentManager.Author.builder().id("1").name("John Doe").build())
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        Optional<DocumentManager.Document> foundDocument = documentManager.findById(savedDocument.getId());

        assertTrue(foundDocument.isPresent());
        assertEquals(savedDocument, foundDocument.get());
    }

    @Test
    void When_FindNonExistentDocumentById_Expect_EmptyOptionalReturned() {
        Optional<DocumentManager.Document> notFoundDocument = documentManager.findById("non-existent-id");

        assertFalse(notFoundDocument.isPresent());
    }

    @Test
    void When_SearchWithMultipleCriteria_Expect_MatchingDocumentsReturned() {
        DocumentManager.Document doc1 = documentManager.save(DocumentManager.Document.builder()
                .title("Java Programming")
                .content("Learn Java programming")
                .author(DocumentManager.Author.builder().id("1").name("John Doe").build())
                .created(Instant.parse("2023-01-01T00:00:00Z"))
                .build());

        documentManager.save(DocumentManager.Document.builder()
                .title("Python Basics")
                .content("Introduction to Python")
                .author(DocumentManager.Author.builder().id("2").name("Jane Smith").build())
                .created(Instant.parse("2023-02-01T00:00:00Z"))
                .build());

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Java"))
                .containsContents(List.of("programming"))
                .authorIds(List.of("1"))
                .createdFrom(Instant.parse("2023-01-01T00:00:00Z"))
                .createdTo(Instant.parse("2023-01-31T23:59:59Z"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertEquals(1, results.size());
        assertEquals(doc1, results.getFirst());
    }

    @Test
    void When_SearchWithNonMatchingCriteria_Expect_EmptyListReturned() {
        DocumentManager.Document doc = DocumentManager.Document.builder()
                .title("Java Programming")
                .content("Learn Java programming")
                .author(DocumentManager.Author.builder().id("1").name("John Doe").build())
                .created(Instant.parse("2023-01-01T00:00:00Z"))
                .build();

        documentManager.save(doc);

        DocumentManager.SearchRequest request = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Python"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(request);

        assertTrue(results.isEmpty());
    }

    @Test
    void When_OriginalObjectSaved_Expect_CloneIsReturned() {
        DocumentManager.Document original = DocumentManager.Document.builder()
                .title("Test Document")
                .content("This is a test document")
                .author(DocumentManager.Author.builder().id("1").name("John Doe").build())
                .build();

        DocumentManager.Document clone = documentManager.save(original);

        assertNotSame(original, clone);
    }
}