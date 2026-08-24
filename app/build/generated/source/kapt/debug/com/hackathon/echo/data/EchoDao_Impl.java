package com.hackathon.echo.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class EchoDao_Impl implements EchoDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EchoItem> __insertionAdapterOfEchoItem;

  private final EntityDeletionOrUpdateAdapter<EchoItem> __deletionAdapterOfEchoItem;

  public EchoDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEchoItem = new EntityInsertionAdapter<EchoItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `echo_items` (`id`,`rawText`,`title`,`category`,`intent`,`date`,`location`,`source`,`createdAt`,`reminderAt`,`status`,`sourceType`,`sourceUrl`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EchoItem entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getRawText() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getRawText());
        }
        if (entity.getTitle() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTitle());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCategory());
        }
        if (entity.getIntent() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getIntent());
        }
        if (entity.getDate() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getDate());
        }
        if (entity.getLocation() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getLocation());
        }
        if (entity.getSource() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getSource());
        }
        statement.bindLong(9, entity.getCreatedAt());
        if (entity.getReminderAt() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getReminderAt());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getStatus());
        }
        if (entity.getSourceType() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getSourceType());
        }
        if (entity.getSourceUrl() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getSourceUrl());
        }
      }
    };
    this.__deletionAdapterOfEchoItem = new EntityDeletionOrUpdateAdapter<EchoItem>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `echo_items` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EchoItem entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final EchoItem echoItem, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfEchoItem.insertAndReturnId(echoItem);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final EchoItem echoItem, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfEchoItem.handle(echoItem);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EchoItem>> getAllEchoes() {
    final String _sql = "SELECT * FROM echo_items ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"echo_items"}, new Callable<List<EchoItem>>() {
      @Override
      @NonNull
      public List<EchoItem> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRawText = CursorUtil.getColumnIndexOrThrow(_cursor, "rawText");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIntent = CursorUtil.getColumnIndexOrThrow(_cursor, "intent");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfReminderAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceType");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final List<EchoItem> _result = new ArrayList<EchoItem>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EchoItem _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpRawText;
            if (_cursor.isNull(_cursorIndexOfRawText)) {
              _tmpRawText = null;
            } else {
              _tmpRawText = _cursor.getString(_cursorIndexOfRawText);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpIntent;
            if (_cursor.isNull(_cursorIndexOfIntent)) {
              _tmpIntent = null;
            } else {
              _tmpIntent = _cursor.getString(_cursorIndexOfIntent);
            }
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpReminderAt;
            if (_cursor.isNull(_cursorIndexOfReminderAt)) {
              _tmpReminderAt = null;
            } else {
              _tmpReminderAt = _cursor.getLong(_cursorIndexOfReminderAt);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSourceType;
            if (_cursor.isNull(_cursorIndexOfSourceType)) {
              _tmpSourceType = null;
            } else {
              _tmpSourceType = _cursor.getString(_cursorIndexOfSourceType);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            _item = new EchoItem(_tmpId,_tmpRawText,_tmpTitle,_tmpCategory,_tmpIntent,_tmpDate,_tmpLocation,_tmpSource,_tmpCreatedAt,_tmpReminderAt,_tmpStatus,_tmpSourceType,_tmpSourceUrl);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getEchoById(final int id, final Continuation<? super EchoItem> $completion) {
    final String _sql = "SELECT * FROM echo_items WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<EchoItem>() {
      @Override
      @Nullable
      public EchoItem call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRawText = CursorUtil.getColumnIndexOrThrow(_cursor, "rawText");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIntent = CursorUtil.getColumnIndexOrThrow(_cursor, "intent");
          final int _cursorIndexOfDate = CursorUtil.getColumnIndexOrThrow(_cursor, "date");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfSource = CursorUtil.getColumnIndexOrThrow(_cursor, "source");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfReminderAt = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderAt");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfSourceType = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceType");
          final int _cursorIndexOfSourceUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceUrl");
          final EchoItem _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpRawText;
            if (_cursor.isNull(_cursorIndexOfRawText)) {
              _tmpRawText = null;
            } else {
              _tmpRawText = _cursor.getString(_cursorIndexOfRawText);
            }
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final String _tmpIntent;
            if (_cursor.isNull(_cursorIndexOfIntent)) {
              _tmpIntent = null;
            } else {
              _tmpIntent = _cursor.getString(_cursorIndexOfIntent);
            }
            final String _tmpDate;
            if (_cursor.isNull(_cursorIndexOfDate)) {
              _tmpDate = null;
            } else {
              _tmpDate = _cursor.getString(_cursorIndexOfDate);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final String _tmpSource;
            if (_cursor.isNull(_cursorIndexOfSource)) {
              _tmpSource = null;
            } else {
              _tmpSource = _cursor.getString(_cursorIndexOfSource);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpReminderAt;
            if (_cursor.isNull(_cursorIndexOfReminderAt)) {
              _tmpReminderAt = null;
            } else {
              _tmpReminderAt = _cursor.getLong(_cursorIndexOfReminderAt);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            final String _tmpSourceType;
            if (_cursor.isNull(_cursorIndexOfSourceType)) {
              _tmpSourceType = null;
            } else {
              _tmpSourceType = _cursor.getString(_cursorIndexOfSourceType);
            }
            final String _tmpSourceUrl;
            if (_cursor.isNull(_cursorIndexOfSourceUrl)) {
              _tmpSourceUrl = null;
            } else {
              _tmpSourceUrl = _cursor.getString(_cursorIndexOfSourceUrl);
            }
            _result = new EchoItem(_tmpId,_tmpRawText,_tmpTitle,_tmpCategory,_tmpIntent,_tmpDate,_tmpLocation,_tmpSource,_tmpCreatedAt,_tmpReminderAt,_tmpStatus,_tmpSourceType,_tmpSourceUrl);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
