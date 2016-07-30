package com.lee.sdk.test.section;

import android.text.TextUtils;

import com.lee.sdk.utils.CharacterParser;

public class Contact {
    int id;
    String name;
    String pinyin;
    String sortLetter = "#";
    String sectionStr;
    String phoneNumber;
    boolean isSection;
    static CharacterParser sParser = CharacterParser.getInstance();
    
    Contact() {
        
    }
    
    Contact(int id, String name) {
        this.id = id;
        this.name = name;
        this.pinyin = sParser.getSpelling(name);
        if (!TextUtils.isEmpty(pinyin)) {
            String sortString = this.pinyin.substring(0, 1).toUpperCase();
            if (sortString.matches("[A-Z]")) {
                this.sortLetter = sortString.toUpperCase();
            } else {
                this.sortLetter = "#";
            }
        }
    }
    
    @Override
    public String toString() {
        if (isSection) {
            return name;
        } else {
            //return name + " (" + sortLetter + ", " + pinyin + ")";
            //return name + " (" + phoneNumber + ")";
            return name;
        }
    }
}  