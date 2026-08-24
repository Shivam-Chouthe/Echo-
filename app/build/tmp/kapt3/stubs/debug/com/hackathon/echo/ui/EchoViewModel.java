package com.hackathon.echo.ui;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\nJ\u0016\u0010\u0010\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\u0012R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0013"}, d2 = {"Lcom/hackathon/echo/ui/EchoViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "dao", "Lcom/hackathon/echo/data/EchoDao;", "echoes", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/hackathon/echo/data/EchoItem;", "getEchoes", "()Lkotlinx/coroutines/flow/StateFlow;", "deleteEcho", "", "echoItem", "updateEchoStatus", "newStatus", "", "app_debug"})
public final class EchoViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.hackathon.echo.data.EchoDao dao = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.hackathon.echo.data.EchoItem>> echoes = null;
    
    public EchoViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.hackathon.echo.data.EchoItem>> getEchoes() {
        return null;
    }
    
    public final void deleteEcho(@org.jetbrains.annotations.NotNull()
    com.hackathon.echo.data.EchoItem echoItem) {
    }
    
    public final void updateEchoStatus(@org.jetbrains.annotations.NotNull()
    com.hackathon.echo.data.EchoItem echoItem, @org.jetbrains.annotations.NotNull()
    java.lang.String newStatus) {
    }
}