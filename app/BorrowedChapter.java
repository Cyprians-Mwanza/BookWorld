
public class BorrowedChapter {
    private String chapterContent;
    private String userId; // Optional: if you want to track who borrowed the chapter
    private String bookId; // Optional: if you want to track from which book the chapter is borrowed

    // Firestore requires a no-argument constructor
    public BorrowedChapter() {
    }

    public BorrowedChapter(String chapterContent) {
        this.chapterContent = chapterContent;
    }

    public BorrowedChapter(String chapterContent, String userId, String bookId) {
        this.chapterContent = chapterContent;
        this.userId = userId;
        this.bookId = bookId;
    }

    public String getChapterContent() {
        return chapterContent;
    }

    public void setChapterContent(String chapterContent) {
        this.chapterContent = chapterContent;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }
}
