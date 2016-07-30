/*
 * Copyright (C) 2011 Baidu Inc. All rights reserved.
 */

package com.lee.sdk.test.section;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.util.Xml.Encoding;

/**
 * 
 * @author lihong06
 * @since 2014-1-26
 */
public class ContactLoader {
    Context mContext;
    private static ContactLoader sInstance;
    
    private ContactLoader() {
        
    }
    
    public synchronized static ContactLoader getInstance() {
        if (null == sInstance) {
            sInstance = new ContactLoader();
        }
         
        return sInstance;
    }
    
    public ArrayList<Contact> getContacts(Context context) {
        ArrayList<Contact> contacts = readDummyData(context);//getContactList(context);
        Collections.sort(contacts, new PinyinComparator());
        
        return groupContacts(contacts);
    }
    
    private ArrayList<Contact> groupContacts(ArrayList<Contact> sources) {
        ArrayList<Contact> results = new ArrayList<Contact>();
        String curCategory = "";
        
        for (Contact contact : sources) {
            if (0 != curCategory.compareToIgnoreCase(contact.sortLetter)) {
                curCategory = contact.sortLetter;
                Contact newContact = new Contact(0, contact.sortLetter);
                newContact.isSection = true;
                newContact.sectionStr = curCategory;
                results.add(newContact);
            }
            
            contact.sectionStr = curCategory;
            results.add(contact);
        }
        
        return results;
    }
    
    public class PinyinComparator implements Comparator<Contact> {
        public int compare(Contact o1, Contact o2) {
            return o1.pinyin.compareTo(o2.pinyin);
        }
    }
    
    private ArrayList<Contact> getContactList(Context context) {
        // the selected cols for contact users
        String[] selectCol = new String[] { ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts._ID };

        final int COL_NAME = 0;
        final int COL_HAS_PHONE = 1;
        final int COL_ID = 2;

        // the selected cols for phones of a user
        String[] selPhoneCols = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.TYPE };

        final int COL_PHONE_NUMBER = 0;
        final int COL_PHONE_NAME = 1;
        final int COL_PHONE_TYPE = 2;

        String select = "((" + Contacts.DISPLAY_NAME + " NOTNULL) AND (" + Contacts.HAS_PHONE_NUMBER + "=1) AND ("
                + Contacts.DISPLAY_NAME + " != '' ))";

        ArrayList<Contact> list = new ArrayList<Contact>();
        Cursor cursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, selectCol, select,
                null, ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
        if (cursor == null) {
            return list;
        }
        if (cursor.getCount() == 0) {
            return list;
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int contactId;
            contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            if (cursor.getInt(COL_HAS_PHONE) > 0) {
                // the contact has numbers
                // 获得联系人的电话号码列表
                String displayName;
                displayName = cursor.getString(COL_NAME);
                Cursor phoneCursor = context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, selPhoneCols,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                if (phoneCursor.moveToFirst()) {
                    do {
                        // 遍历所有的联系人下面所有的电话号码
                        String phoneNumber = phoneCursor.getString(COL_PHONE_NUMBER);
                        Contact contact = new Contact(0, displayName);
                        contact.phoneNumber = phoneNumber;
                        list.add(contact);
                    } while (phoneCursor.moveToNext());
                    
                    phoneCursor.close();
                }
            }
            cursor.moveToNext();
        }
        
        cursor.close();

        return list;
    }
    
    private ArrayList<Contact> readDummyData(Context context) {
        ArrayList<Contact> list = new ArrayList<Contact>();
        InputStream is = null;
        try {
            is = context.getAssets().open("names.txt");
            String datas = streamToString(is);
            String[] arrays = datas.split("\\|");
            if (null != arrays) {
                for (String name : arrays) {
                    if (TextUtils.isEmpty(name)) {
                        continue;
                    }
                    Contact contact = new Contact(0, name);
                    list.add(contact);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return list;
    }
    
    
    public static String streamToString(InputStream is) {
        return streamToString(is, Encoding.UTF_8.toString());
    }
    
    /**
     * 按照特定的编码格式转换Stream成string
     * 
     * @param is
     *            Stream源
     * @param enc
     *            编码格式
     * @return 目标String
     */
    public static String streamToString(InputStream is, String enc) {
        if (null == is) {
            return null;
        }
        
        StringBuilder buffer = new StringBuilder();
        String line = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    is, enc), 1024);
            while (null != (line = reader.readLine())) {
                buffer.append(line);
            }
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return buffer.toString();
    }
}
