/*
 * Copyright (C) 2013 Lee Hong (http://blog.csdn.net/leehong2005)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lee.sdk.data;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

/**
 * This class defines the abstract database processing.
 * 
 * @author Li Hong
 * @date 2011/08/10
 */
public abstract class AbstractDB {
    /**
     * Called when crate the table.
     * 
     * @param db The database.
     */
    public abstract void onCreateTable(SQLiteDatabase db);

    /**
     * Called when update the table.
     * 
     * @param db The database instance.
     * @param upgradeVersion The old version.
     * @param newVersion The current version.
     */
    public abstract void onUpgradeTable(SQLiteDatabase db, int upgradeVersion, int newVersion);

    /**
     * Close the data base if it is opened.
     * 
     * @return true if succeeds, otherwise false.
     */
    public boolean closeDB(SQLiteDatabase db) {
        // Removed by LiHong at 2012/07/09 begin ===========
        //
        // In the whole life cycle of the application, we do not close the
        // database.
        // When the application closes, we only close the database.
        /**
         * if /null != db && db.isOpen()) { db.close(); }
         */

        return true;
    }

    /**
     * Close the wild cursor.
     * 
     * @param cur Cursor to be closed.
     */
    public void closeCursor(Cursor cur) {
        if (null != cur && !cur.isClosed()) {
            cur.close();
        }
    }

    /**
     * End transaction for DB.
     * 
     * @param db
     */
    public void endTransaction(SQLiteDatabase db) {
        try {
            if (null != db && db.inTransaction()) {
                db.endTransaction();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Execute a specified SQL.
     * 
     * @param db The database to be operated by SQL.
     * @param sql The string of SQL.
     * @param bindArgs only byte[], String, Long and Double are supported in bindArgs.
     */
    public boolean execSQL(SQLiteDatabase db, String sql, Object[] bindArgs) {
        try {
            if (null == db) {
                return false;
            }

            if (null == bindArgs) {
                db.execSQL(sql);
            } else {
                db.execSQL(sql, bindArgs);
            }

            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Upgrade tables. In this method, the sequence is: 
     * 
     * <p>
     * <li>[1] Rename the specified table as a temporary table.
     * <li>[2] Create a new table which name is the specified name.
     * <li>[3] Insert data into the new created table, data from the temporary table.
     * <li>[4] Drop the temporary table.
     * </p>
     * 
     * @param db The database.
     * @param tableName The table name.
     * @param columns The columns range, format is "ColA, ColB, ColC, ... ColN";
     */
    protected void upgradeTables(SQLiteDatabase db, String tableName, String columns) {
        try {
            db.beginTransaction();

            // 1, Rename table.
            String tempTableName = tableName + "_temp";
            String sql = "ALTER TABLE " + tableName + " RENAME TO " + tempTableName;
            execSQL(db, sql, null);

            // 2, Create table.
            onCreateTable(db);

            // 3, Load data
            sql = "INSERT INTO " + tableName + " (" + columns + ") " + " SELECT " + columns + " FROM " + tempTableName;

            execSQL(db, sql, null);

            // 4, Drop the temporary table.
            execSQL(db, "DROP TABLE IF EXISTS " + tempTableName, null);

            db.setTransactionSuccessful();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Get the columns of the specified table.
     * 
     * @param db
     * @param tableName
     * 
     * @return The column array.
     */
    protected String[] getColumnNames(SQLiteDatabase db, String tableName) {
        String[] columnNames = null;
        Cursor c = null;

        try {
            c = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
            if (null != c) {
                int columnIndex = c.getColumnIndex("name");
                if (-1 == columnIndex) {
                    return null;
                }

                int index = 0;
                columnNames = new String[c.getCount()];
                for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
                    columnNames[index] = c.getString(columnIndex);
                    index++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeCursor(c);
        }

        return columnNames;
    }
}
