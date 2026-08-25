package com.hackathon.echo.capture;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000h\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\u0018\u0000 62\u00020\u0001:\u00016B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0012\u0010\"\u001a\u0004\u0018\u00010#2\u0006\u0010$\u001a\u00020#H\u0002J\u0012\u0010%\u001a\u00020&2\b\u0010\'\u001a\u0004\u0018\u00010(H\u0002J\u0012\u0010)\u001a\u00020&2\b\u0010*\u001a\u0004\u0018\u00010+H\u0014J(\u0010,\u001a\u00020&2\u0006\u0010-\u001a\u00020#2\u0006\u0010.\u001a\u00020#2\b\u0010/\u001a\u0004\u0018\u00010#H\u0082@\u00a2\u0006\u0002\u00100J\u0018\u00101\u001a\u00020&2\u0006\u00102\u001a\u0002032\u0006\u00104\u001a\u000205H\u0002R\u001b\u0010\u0003\u001a\u00020\u00048BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006R\u001b\u0010\t\u001a\u00020\n8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\r\u0010\b\u001a\u0004\b\u000b\u0010\fR\u001b\u0010\u000e\u001a\u00020\u000f8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0012\u0010\b\u001a\u0004\b\u0010\u0010\u0011R\u001b\u0010\u0013\u001a\u00020\u00148BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0017\u0010\b\u001a\u0004\b\u0015\u0010\u0016R\u001b\u0010\u0018\u001a\u00020\u00198BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u001c\u0010\b\u001a\u0004\b\u001a\u0010\u001bR\u001b\u0010\u001d\u001a\u00020\u001e8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b!\u0010\b\u001a\u0004\b\u001f\u0010 \u00a8\u00067"}, d2 = {"Lcom/hackathon/echo/capture/CaptureActivity;", "Landroidx/activity/ComponentActivity;", "()V", "aiIntentExtractor", "Lcom/hackathon/echo/ocr/AiIntentExtractor;", "getAiIntentExtractor", "()Lcom/hackathon/echo/ocr/AiIntentExtractor;", "aiIntentExtractor$delegate", "Lkotlin/Lazy;", "alarmManager", "Landroid/app/AlarmManager;", "getAlarmManager", "()Landroid/app/AlarmManager;", "alarmManager$delegate", "categorizer", "Lcom/hackathon/echo/ocr/Categorizer;", "getCategorizer", "()Lcom/hackathon/echo/ocr/Categorizer;", "categorizer$delegate", "database", "Lcom/hackathon/echo/data/AppDatabase;", "getDatabase", "()Lcom/hackathon/echo/data/AppDatabase;", "database$delegate", "entityExtractor", "Lcom/hackathon/echo/ocr/EntityExtractor;", "getEntityExtractor", "()Lcom/hackathon/echo/ocr/EntityExtractor;", "entityExtractor$delegate", "ocrProcessor", "Lcom/hackathon/echo/ocr/OcrProcessor;", "getOcrProcessor", "()Lcom/hackathon/echo/ocr/OcrProcessor;", "ocrProcessor$delegate", "extractUrl", "", "text", "handleIntent", "", "intent", "Landroid/content/Intent;", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "saveToDatabase", "content", "type", "sourceUrl", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "scheduleAlarm", "echoId", "", "reminderTime", "", "Companion", "app_debug"})
public final class CaptureActivity extends androidx.activity.ComponentActivity {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy ocrProcessor$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy categorizer$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy entityExtractor$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy aiIntentExtractor$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy alarmManager$delegate = null;
    private static final long AI_TIMEOUT_MS = 6000L;
    @org.jetbrains.annotations.NotNull()
    private static final kotlinx.coroutines.CoroutineScope captureScope = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.hackathon.echo.capture.CaptureActivity.Companion Companion = null;
    
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
    
    private final com.hackathon.echo.ocr.AiIntentExtractor getAiIntentExtractor() {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/hackathon/echo/capture/CaptureActivity$Companion;", "", "()V", "AI_TIMEOUT_MS", "", "captureScope", "Lkotlinx/coroutines/CoroutineScope;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}