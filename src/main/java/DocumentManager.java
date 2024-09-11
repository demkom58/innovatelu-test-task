import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 * <p>
 *
 * Task implementation note:
 *      1. I didn't modify data classes and since I allowed to implement only DocumentManager class,
 * I put all methods in this class;
 *      2. I used HashMap to store documents and used stream API to filter documents based on search
 * criteria since it is simple and readable and there is no requirement for performance;
 *      3. UUID.randomUUID().toString() is used to generate unique id for documents, since it is simple,
 * universally unique and no requirement for performance;
 *      4. deepClone method is used to clone document object to avoid changing original one
 * when returning it, since there is no statement that it is safe to return original one;
 *      5. data class properties has no nullability mark and default nullability policy is not clear,
 * so every property treated as nullable and checked for null in methods;
 *      6. there was no limitation on Java version, so I used Java 21, hopefully it is not a problem.
 */
public class DocumentManager {
    private final Map<String, Document> documents = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        Document stored = deepClone(document);
        if (stored.getId() == null) {
            stored.setId(generateId());
        }
        documents.put(stored.getId(), stored);
        return deepClone(stored);
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return documents.values().stream()
                .filter(doc -> matchesSearchCriteria(doc, request))
                .map(this::deepClone)
                .toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(documents.get(id))
                .map(this::deepClone);
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    private boolean matchesSearchCriteria(Document doc, SearchRequest request) {
        return matchesTitlePrefixes(doc, request.getTitlePrefixes())
                && matchesContainsContents(doc, request.getContainsContents())
                && matchesAuthorIds(doc, request.getAuthorIds())
                && isWithinDateRange(doc, request.getCreatedFrom(), request.getCreatedTo());
    }

    private boolean matchesTitlePrefixes(Document doc, List<String> titlePrefixes) {
        if (doc.getTitle() == null) {
            return titlePrefixes == null || titlePrefixes.isEmpty();
        }
        return titlePrefixes == null || titlePrefixes.isEmpty()
                || titlePrefixes.stream().anyMatch(prefix -> doc.getTitle().startsWith(prefix));
    }

    private boolean matchesContainsContents(Document doc, List<String> containsContents) {
        if (doc.getContent() == null) {
            return containsContents == null || containsContents.isEmpty();
        }
        return containsContents == null || containsContents.isEmpty()
                || containsContents.stream().allMatch(content -> doc.getContent().contains(content));
    }

    private boolean matchesAuthorIds(Document doc, List<String> authorIds) {
        if (doc.getAuthor() == null || doc.getAuthor().getId() == null) {
            return authorIds == null || authorIds.isEmpty();
        }
        return authorIds == null || authorIds.isEmpty()
                || authorIds.contains(doc.getAuthor().getId());
    }

    private boolean isWithinDateRange(Document doc, Instant from, Instant to) {
        Instant created = doc.getCreated();
        if (created == null) {
            return from == null && to == null;
        }
        return (from == null || !created.isBefore(from))
                && (to == null || !created.isAfter(to));
    }

    private Document deepClone(Document doc) {
        Author author = doc.getAuthor() == null
                ? null
                : new Author(doc.getAuthor().getId(), doc.getAuthor().getName());

        return new Document(doc.getId(), doc.getTitle(), doc.getContent(), author, doc.getCreated());
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}