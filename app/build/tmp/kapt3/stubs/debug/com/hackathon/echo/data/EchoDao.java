package com.hackathon.echo.data;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0014\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\t0\bH\'J\u0018\u0010\n\u001a\u0004\u0018\u00010\u00052\u0006\u0010\u000b\u001a\u00020\fH\u00a7@\u00a2\u0006\u0002\u0010\rJ\u0016\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0010"}, d2 = {"Lcom/hackathon/echo/data/EchoDao;", "", "delete", "", "echoItem", "Lcom/hackathon/echo/data/EchoItem;", "(Lcom/hackathon/echo/data/EchoItem;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllEchoes", "Lkotlinx/coroutines/flow/Flow;", "", "getEchoById", "id", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insert", "", "app_debug"})
@androidx.room.Dao()
public abstract interface EchoDao {
    
    @androidx.room.Query(value = "SELECT * FROM echo_items ORDER BY createdAt DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.hackathon.echo.data.EchoItem>> getAllEchoes();
    
    @androidx.room.Query(value = "SELECT * FROM echo_items WHERE id = :id LIMIT 1")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEchoById(int id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.hackathon.echo.data.EchoItem> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.hackathon.echo.data.EchoItem echoItem, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.hackathon.echo.data.EchoItem echoItem, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}