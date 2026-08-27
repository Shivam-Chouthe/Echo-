package com.hackathon.echo;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000D\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010 \n\u0000\u001a&\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a&\u0010\b\u001a\u00020\u00012\b\b\u0002\u0010\t\u001a\u00020\n2\u0012\u0010\u000b\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00010\fH\u0007\u001a \u0010\u000e\u001a\u00020\u00012\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\u00032\u0006\u0010\u0010\u001a\u00020\u0003H\u0007\u001a,\u0010\u0011\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a(\u0010\u0015\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\r2\b\b\u0002\u0010\t\u001a\u00020\n2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a\b\u0010\u0018\u001a\u00020\u0001H\u0007\u001a\u0016\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0012\u001a\u00020\u0013\u001a\u0016\u0010\u001c\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u0002\u001a\u00020\u0003\u001a\u0016\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001e\u001a\u00020\u0003\u001a\u0016\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010 \u001a\u00020\u0003\u001a\u0016\u0010!\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\"\u001a\u00020\u0003\u001a\u001c\u0010#\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\u001b2\f\u0010$\u001a\b\u0012\u0004\u0012\u00020\u00130%\u00a8\u0006&"}, d2 = {"ActionButton", "", "text", "", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "onClick", "Lkotlin/Function0;", "DashboardScreen", "viewModel", "Lcom/hackathon/echo/ui/EchoViewModel;", "onEchoClick", "Lkotlin/Function1;", "", "DetailItem", "label", "value", "EchoCard", "echo", "Lcom/hackathon/echo/data/EchoItem;", "onDelete", "EchoDetailScreen", "echoId", "onBack", "NotificationPermissionEffect", "addToCalendar", "context", "Landroid/content/Context;", "copyToClipboard", "openInMaps", "location", "openUrl", "url", "searchOnWeb", "query", "syncToPc", "echoes", "", "app_debug"})
public final class MainActivityKt {
    
    @androidx.compose.runtime.Composable()
    public static final void NotificationPermissionEffect() {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void DashboardScreen(@org.jetbrains.annotations.NotNull()
    com.hackathon.echo.ui.EchoViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Integer, kotlin.Unit> onEchoClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void EchoCard(@org.jetbrains.annotations.NotNull()
    com.hackathon.echo.data.EchoItem echo, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDelete, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void EchoDetailScreen(int echoId, @org.jetbrains.annotations.NotNull()
    com.hackathon.echo.ui.EchoViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ActionButton(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.graphics.vector.ImageVector icon, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    public static final void addToCalendar(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.hackathon.echo.data.EchoItem echo) {
    }
    
    public static final void openInMaps(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String location) {
    }
    
    public static final void searchOnWeb(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String query) {
    }
    
    public static final void openUrl(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String url) {
    }
    
    public static final void copyToClipboard(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
    
    public static final void syncToPc(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    java.util.List<com.hackathon.echo.data.EchoItem> echoes) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DetailItem(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.graphics.vector.ImageVector icon, @org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
}