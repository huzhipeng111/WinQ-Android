package com.stratagile.qlink.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "ETH_WALLET".
*/
public class EthWalletDao extends AbstractDao<EthWallet, Long> {

    public static final String TABLENAME = "ETH_WALLET";

    /**
     * Properties of entity EthWallet.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Address = new Property(1, String.class, "address", false, "ADDRESS");
        public final static Property Name = new Property(2, String.class, "name", false, "NAME");
        public final static Property Password = new Property(3, String.class, "password", false, "PASSWORD");
        public final static Property KeystorePath = new Property(4, String.class, "keystorePath", false, "KEYSTORE_PATH");
        public final static Property Mnemonic = new Property(5, String.class, "mnemonic", false, "MNEMONIC");
        public final static Property IsCurrent = new Property(6, boolean.class, "isCurrent", false, "IS_CURRENT");
        public final static Property IsBackup = new Property(7, boolean.class, "isBackup", false, "IS_BACKUP");
        public final static Property IsLook = new Property(8, boolean.class, "isLook", false, "IS_LOOK");
    }


    public EthWalletDao(DaoConfig config) {
        super(config);
    }
    
    public EthWalletDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"ETH_WALLET\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"ADDRESS\" TEXT," + // 1: address
                "\"NAME\" TEXT," + // 2: name
                "\"PASSWORD\" TEXT," + // 3: password
                "\"KEYSTORE_PATH\" TEXT," + // 4: keystorePath
                "\"MNEMONIC\" TEXT," + // 5: mnemonic
                "\"IS_CURRENT\" INTEGER NOT NULL ," + // 6: isCurrent
                "\"IS_BACKUP\" INTEGER NOT NULL ," + // 7: isBackup
                "\"IS_LOOK\" INTEGER NOT NULL );"); // 8: isLook
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"ETH_WALLET\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, EthWallet entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String address = entity.getAddress();
        if (address != null) {
            stmt.bindString(2, address);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(4, password);
        }
 
        String keystorePath = entity.getKeystorePath();
        if (keystorePath != null) {
            stmt.bindString(5, keystorePath);
        }
 
        String mnemonic = entity.getMnemonic();
        if (mnemonic != null) {
            stmt.bindString(6, mnemonic);
        }
        stmt.bindLong(7, entity.getIsCurrent() ? 1L: 0L);
        stmt.bindLong(8, entity.getIsBackup() ? 1L: 0L);
        stmt.bindLong(9, entity.getIsLook() ? 1L: 0L);
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, EthWallet entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String address = entity.getAddress();
        if (address != null) {
            stmt.bindString(2, address);
        }
 
        String name = entity.getName();
        if (name != null) {
            stmt.bindString(3, name);
        }
 
        String password = entity.getPassword();
        if (password != null) {
            stmt.bindString(4, password);
        }
 
        String keystorePath = entity.getKeystorePath();
        if (keystorePath != null) {
            stmt.bindString(5, keystorePath);
        }
 
        String mnemonic = entity.getMnemonic();
        if (mnemonic != null) {
            stmt.bindString(6, mnemonic);
        }
        stmt.bindLong(7, entity.getIsCurrent() ? 1L: 0L);
        stmt.bindLong(8, entity.getIsBackup() ? 1L: 0L);
        stmt.bindLong(9, entity.getIsLook() ? 1L: 0L);
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public EthWallet readEntity(Cursor cursor, int offset) {
        EthWallet entity = new EthWallet( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // address
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // name
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // password
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // keystorePath
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // mnemonic
            cursor.getShort(offset + 6) != 0, // isCurrent
            cursor.getShort(offset + 7) != 0, // isBackup
            cursor.getShort(offset + 8) != 0 // isLook
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, EthWallet entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setAddress(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setName(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setPassword(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setKeystorePath(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setMnemonic(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setIsCurrent(cursor.getShort(offset + 6) != 0);
        entity.setIsBackup(cursor.getShort(offset + 7) != 0);
        entity.setIsLook(cursor.getShort(offset + 8) != 0);
     }
    
    @Override
    protected final Long updateKeyAfterInsert(EthWallet entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(EthWallet entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(EthWallet entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
