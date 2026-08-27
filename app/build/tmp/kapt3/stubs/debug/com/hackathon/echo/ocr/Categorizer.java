package com.hackathon.echo.ocr;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0007\u0018\u00002\u00020\u0001:\u0001\u000eB\u0005\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u0010\u0010\u0007\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\t\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\n\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\u000b\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\f\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002J\u0010\u0010\r\u001a\u00020\b2\u0006\u0010\u0005\u001a\u00020\u0006H\u0002\u00a8\u0006\u000f"}, d2 = {"Lcom/hackathon/echo/ocr/Categorizer;", "", "()V", "categorize", "Lcom/hackathon/echo/ocr/Categorizer$CategoryInfo;", "text", "", "isEvent", "", "isPlace", "isRecipe", "isTask", "isTool", "isTopic", "CategoryInfo", "app_debug"})
public final class Categorizer {
    
    public Categorizer() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.hackathon.echo.ocr.Categorizer.CategoryInfo categorize(@org.jetbrains.annotations.NotNull()
    java.lang.String text) {
        return null;
    }
    
    private final boolean isRecipe(java.lang.String text) {
        return false;
    }
    
    private final boolean isEvent(java.lang.String text) {
        return false;
    }
    
    private final boolean isTool(java.lang.String text) {
        return false;
    }
    
    private final boolean isPlace(java.lang.String text) {
        return false;
    }
    
    private final boolean isTask(java.lang.String text) {
        return false;
    }
    
    private final boolean isTopic(java.lang.String text) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0005J\t\u0010\t\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\u000b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/hackathon/echo/ocr/Categorizer$CategoryInfo;", "", "category", "", "intent", "(Ljava/lang/String;Ljava/lang/String;)V", "getCategory", "()Ljava/lang/String;", "getIntent", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    public static final class CategoryInfo {
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String category = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String intent = null;
        
        public CategoryInfo(@org.jetbrains.annotations.NotNull()
        java.lang.String category, @org.jetbrains.annotations.NotNull()
        java.lang.String intent) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getCategory() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getIntent() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component2() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.hackathon.echo.ocr.Categorizer.CategoryInfo copy(@org.jetbrains.annotations.NotNull()
        java.lang.String category, @org.jetbrains.annotations.NotNull()
        java.lang.String intent) {
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