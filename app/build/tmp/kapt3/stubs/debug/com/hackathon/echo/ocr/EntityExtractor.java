package com.hackathon.echo.ocr;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0003\u0018\u00002\u00020\u0001:\u0001\u0013B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0017\u0010\u0003\u001a\u0004\u0018\u00010\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u00a2\u0006\u0002\u0010\u0007J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u0006J\u0012\u0010\u000b\u001a\u0004\u0018\u00010\u00062\u0006\u0010\n\u001a\u00020\u0006H\u0002J\u0012\u0010\f\u001a\u0004\u0018\u00010\u00062\u0006\u0010\n\u001a\u00020\u0006H\u0002J\u0010\u0010\r\u001a\u00020\u00062\u0006\u0010\n\u001a\u00020\u0006H\u0002J\u0012\u0010\u000e\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u000f\u001a\u00020\u0006H\u0002J\u0017\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u00a2\u0006\u0002\u0010\u0012\u00a8\u0006\u0014"}, d2 = {"Lcom/hackathon/echo/ocr/EntityExtractor;", "", "()V", "calculateReminder", "", "dateStr", "", "(Ljava/lang/String;)Ljava/lang/Long;", "extract", "Lcom/hackathon/echo/ocr/EntityExtractor$ExtractedEntities;", "text", "extractDate", "extractLocation", "extractTitle", "fullMonthName", "token", "weekdayToCalendar", "", "(Ljava/lang/String;)Ljava/lang/Integer;", "ExtractedEntities", "app_debug"})
public final class EntityExtractor {
    
    public EntityExtractor() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hackathon.echo.ocr.EntityExtractor.ExtractedEntities extract(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return null;
    }
    
    private final java.lang.String extractTitle(java.lang.String text) {
        return null;
    }
    
    private final java.lang.String extractDate(java.lang.String text) {
        return null;
    }
    
    /**
     * Maps a month token (full or abbreviated, e.g. "sep"/"sept"/"september") to its full name.
     */
    private final java.lang.String fullMonthName(java.lang.String token) {
        return null;
    }
    
    private final java.lang.String extractLocation(java.lang.String text) {
        return null;
    }
    
    private final java.lang.Long calculateReminder(java.lang.String dateStr) {
        return null;
    }
    
    /**
     * Maps a weekday name to its Calendar.DAY_OF_WEEK constant, or null if not a weekday.
     */
    private final java.lang.Integer weekdayToCalendar(java.lang.String dateStr) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u000f\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B+\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0002\u0010\bJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0011\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010\u0012\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u0010\rJ<\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u00c6\u0001\u00a2\u0006\u0002\u0010\u0015J\u0013\u0010\u0016\u001a\u00020\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0019\u001a\u00020\u001aH\u00d6\u0001J\t\u0010\u001b\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\nR\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u0010\u000e\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\n\u00a8\u0006\u001c"}, d2 = {"Lcom/hackathon/echo/ocr/EntityExtractor$ExtractedEntities;", "", "title", "", "date", "location", "reminderAt", "", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)V", "getDate", "()Ljava/lang/String;", "getLocation", "getReminderAt", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getTitle", "component1", "component2", "component3", "component4", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)Lcom/hackathon/echo/ocr/EntityExtractor$ExtractedEntities;", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class ExtractedEntities {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String title = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String date = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.String location = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Long reminderAt = null;
        
        public ExtractedEntities(@org.jetbrains.annotations.NotNull()
        java.lang.String title, @org.jetbrains.annotations.Nullable()
        java.lang.String date, @org.jetbrains.annotations.Nullable()
        java.lang.String location, @org.jetbrains.annotations.Nullable()
        java.lang.Long reminderAt) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getTitle() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getDate() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String getLocation() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Long getReminderAt() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.String component3() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Long component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.hackathon.echo.ocr.EntityExtractor.ExtractedEntities copy(@org.jetbrains.annotations.NotNull()
        java.lang.String title, @org.jetbrains.annotations.Nullable()
        java.lang.String date, @org.jetbrains.annotations.Nullable()
        java.lang.String location, @org.jetbrains.annotations.Nullable()
        java.lang.Long reminderAt) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}