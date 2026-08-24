package com.hackathon.echo.capture;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\u001d\u001a\u0004\u0018\u00010\u001e2\u0006\u0010\u001f\u001a\u00020\u001eH\u0002J\u0012\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010#H\u0002J\u0012\u0010$\u001a\u00020!2\b\u0010%\u001a\u0004\u0018\u00010&H\u0014J(\u0010\'\u001a\u00020!2\u0006\u0010(\u001a\u00020\u001e2\u0006\u0010)\u001a\u00020\u001e2\b\u0010*\u001a\u0004\u0018\u00010\u001eH\u0082@\u00a2\u0006\u0002\u0010+J\u0018\u0010,\u001a\u00020!2\u0006\u0010-\u001a\u00020.2\u0006\u0010/\u001a\u000200H\u0002R\u001b\u0010\u0003\u001a\u00020\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001b\u0010\t\u001a\u00020\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\b\u001a\u0004\b\u000b\u0010\fR\u001b\u0010\u000e\u001a\u00020\u000f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\b\u001a\u0004\b\u0010\u0010\u0011R\u001b\u0010\u0013\u001a\u00020\u00148BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\b\u001a\u0004\b\u0015\u0010\u0016R\u001b\u0010\u0018\u001a\u00020\u00198BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001c\u0010\b\u001a\u0004\b\u001a\u0010\u001b\u00a8\u00061"}, d2 = {"Lcom/hackathon/echo/capture/CaptureActivity;", "Landroidx/activity/ComponentActivity;", "()V", "alarmManager", "Landroid/app/AlarmManager;", "getAlarmManager", "()Landroid/app/AlarmManager;", "alarmManager$delegate", "Lkotlin/Lazy;", "categorizer", "Lcom/hackathon/echo/ocr/Categorizer;", "getCategorizer", "()Lcom/hackathon/echo/ocr/Categorizer;", "categorizer$delegate", "database", "Lcom/hackathon/echo/data/AppDatabase;", "getDatabase", "()Lcom/hackathon/echo/data/AppDatabase;", "database$delegate", "entityExtractor", "Lcom/hackathon/echo/ocr/EntityExtractor;", "getEntityExtractor", "()Lcom/hackathon/echo/ocr/EntityExtractor;", "entityExtractor$delegate", "ocrProcessor", "Lcom/hackathon/echo/ocr/OcrProcessor;", "getOcrProcessor", "()Lcom/hackathon/echo/ocr/OcrProcessor;", "ocrProcessor$delegate", "extractUrl", "", "text", "handleIntent", "", "intent", "Landroid/content/Intent;", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "saveToDatabase", "content", "type", "sourceUrl", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "scheduleAlarm", "echoId", "", "reminderTime", "", "app_debug"})
public final class CaptureActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy ocrProcessor$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy categorizer$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy entityExtractor$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy alarmManager$delegate = null;
    
    public CaptureActivity() {
        super();
    }
    
    private final com.hackathon.echo.ocr.OcrProcessor getOcrProcessor() {
        return null;
    }
    
    private final com.hackathon.echo.ocr.Categorizer getCategorizer() {
        return null;
    }
    
    private final com.hackathon.echo.ocr.EntityExtractor getEntityExtractor() {
        return null;
    }
    
    private final com.hackathon.echo.data.AppDatabase getDatabase() {
        return null;
    }
    
    private final android.app.AlarmManager getAlarmManager() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void handleIntent(android.content.Intent intent) {
    }
    
    private final java.lang.String extractUrl(java.lang.String text) {
        return null;
    }
    
    private final java.lang.Object saveToDatabase(java.lang.String content, java.lang.String type, java.lang.String sourceUrl, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final void scheduleAlarm(int echoId, long reminderTime) {
    }
}