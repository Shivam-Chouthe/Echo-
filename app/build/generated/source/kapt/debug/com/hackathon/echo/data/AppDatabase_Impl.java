package com.hackathon.echo.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile EchoDao _echoDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(4) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `echo_items` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `rawText` TEXT NOT NULL, `title` TEXT NOT NULL, `category` TEXT NOT NULL, `intent` TEXT NOT NULL, `summary` TEXT, `action` TEXT, `date` TEXT, `location` TEXT, `source` TEXT, `createdAt` INTEGER NOT NULL, `reminderAt` INTEGER, `status` TEXT NOT NULL, `sourceType` TEXT NOT NULL, `sourceUrl` TEXT, `isAiRefined` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'e568f73e7168bbbc3a6195b54915c940')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `echo_items`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsEchoItems = new HashMap<String, TableInfo.Column>(16);
        _columnsEchoItems.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("rawText", new TableInfo.Column("rawText", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("intent", new TableInfo.Column("intent", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("summary", new TableInfo.Column("summary", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("action", new TableInfo.Column("action", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("date", new TableInfo.Column("date", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("location", new TableInfo.Column("location", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("source", new TableInfo.Column("source", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("reminderAt", new TableInfo.Column("reminderAt", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("sourceType", new TableInfo.Column("sourceType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("sourceUrl", new TableInfo.Column("sourceUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsEchoItems.put("isAiRefined", new TableInfo.Column("isAiRefined", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysEchoItems = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesEchoItems = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoEchoItems = new TableInfo("echo_items", _columnsEchoItems, _foreignKeysEchoItems, _indicesEchoItems);
        final TableInfo _existingEchoItems = TableInfo.read(db, "echo_items");
        if (!_infoEchoItems.equals(_existingEchoItems)) {
          return new RoomOpenHelper.ValidationResult(false, "echo_items(com.hackathon.echo.data.EchoItem).\n"
                  + " Expected:\n" + _infoEchoItems + "\n"
                  + " Found:\n" + _existingEchoItems);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "e568f73e7168bbbc3a6195b54915c940", "8a505951ca4ecff0ddd4a2884bc2f5ec");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "echo_items");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `echo_items`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(EchoDao.class, EchoDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public EchoDao echoDao() {
    if (_echoDao != null) {
      return _echoDao;
    } else {
      synchronized(this) {
        if(_echoDao == null) {
          _echoDao = new EchoDao_Impl(this);
        }
        return _echoDao;
      }
    }
  }
}
