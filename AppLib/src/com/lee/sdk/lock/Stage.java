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

package com.lee.sdk.lock;

import com.lee.sdk.res.R;

/**
 * Keep track internally of where the user is in choosing a pattern.
 */
public enum Stage {
    Introduction(
            R.string.lockpattern_recording_intro_header, true),
    HelpScreen(
            R.string.lockpattern_settings_help_how_to_record, false),
    ChoiceTooShort(
            R.string.lockpattern_recording_incorrect_too_short, true),
    FirstChoiceValid(
            R.string.lockpattern_pattern_entered_header, false),
    NeedToConfirm(
            R.string.lockpattern_need_to_confirm, true),
    ConfirmWrong(
            R.string.lockpattern_need_to_confirm_wrong, true),
    ChoiceConfirmed(
            R.string.lockpattern_pattern_confirmed_header, false),
    NeedToUnlock(
            R.string.lockpattern_need_to_unlock, false),
    NeedToUnlockWrong(
            R.string.lockpattern_need_to_unlock_wrong, false),
    LockedOut(
            R.string.lockpattern_need_to_unlock_wrong, false);

    /**
     * @param headerMessage The message displayed at the top.
     * @param leftMode The mode of the left button.
     * @param rightMode The mode of the right button.
     * @param footerMessage The footer message.
     * @param patternEnabled Whether the pattern widget is enabled.
     */
    Stage(int headerMessage, boolean patternEnabled) {
        this.headerMessage = headerMessage;
        this.patternEnabled = patternEnabled;
    }

    final int headerMessage;
    final boolean patternEnabled;
}