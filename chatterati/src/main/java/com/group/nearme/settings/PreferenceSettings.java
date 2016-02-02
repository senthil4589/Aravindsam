/**
 * 
 */
package com.group.nearme.settings;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * @author Senthil
 * 
 */

@SuppressLint("CommitPrefEdits")
public class PreferenceSettings {

	public static final String MY_CONFIG = "my_settings";
	public static final String USER_NAME="user_name";
	public static final String USER_ID="user_id";
	public static final String GENDER="Gender";
	public static final String MOBILE_NO="mobile_no";
	public static final String COUNTRY="Country";
	public static final String USER_PROFILE_PICTURE="profile_pic";
	public static final String USER_STATE="user_state";
	public final static String LOGIN_STATUS = "login_status";
	public final static String PUSH_NOTIFICATION_STATUS = "push_status";
	public final static String SOUND_NOTIFICATION_STATUS = "sound_status";
	public static final String NAME_CHANGE_COUNT="name_change_count";
	public static final String GROUP_INVITATION="group_invitation";
	public static final String JOINED_FIRST_GROUP="first_group";
	public static final String USER_SCORE="user_score";
	public static final String PUSH_STATUS="status";
	public static Set<String> set = new HashSet<String>();
	public static SharedPreferences getPreferences() {
		return GroupNearMeApplication.getAppContext().getSharedPreferences(MY_CONFIG,Context.MODE_PRIVATE);
	}
	public static void applyPreferenceChanges(Editor editor) {
		editor.commit();
	}
	
	public static void setUserName(String name) {
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putString(USER_NAME, name);
		applyPreferenceChanges(editor);
	}
	
	public static String getUserName() {
		return getPreferences().getString(USER_NAME, USER_NAME);
	}
	
	public static void setUserID(String id) {
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putString(USER_ID, id);
		applyPreferenceChanges(editor);
	}
	
	public static String getUserID() {
		return getPreferences().getString(USER_ID, USER_ID);
	}
	
	public static void setUserScore(int score) {
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putInt(USER_SCORE, score);
		applyPreferenceChanges(editor);
	}
	
	public static int setUserScore() {
		return getPreferences().getInt(USER_SCORE, 1000);
	}
	
	public static void setMobileNo(String no) {
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putString(MOBILE_NO, no);
		applyPreferenceChanges(editor);
	}
	
	public static String getMobileNo() {
		return getPreferences().getString(MOBILE_NO, MOBILE_NO);
	}
	
	public static void setCountry(String country) {
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putString(COUNTRY, country);
		applyPreferenceChanges(editor);
	}
	
	public static String getCountry() {
		return getPreferences().getString(COUNTRY, COUNTRY);
	}
	
	
	public static void setProfilePic(String id) {
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putString(USER_PROFILE_PICTURE, id);
		applyPreferenceChanges(editor);
	}
	
	public static String getProfilePic() {
		return getPreferences().getString(USER_PROFILE_PICTURE, USER_PROFILE_PICTURE);
	}
	
	public static void setGender(String country) {
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putString(GENDER, country);
		applyPreferenceChanges(editor);
	}
	
	public static String getGender() {
		return getPreferences().getString(GENDER, GENDER);
	}
	
	public static void setUserState(String state) {
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putString(USER_STATE, state);
		applyPreferenceChanges(editor);
	}
	
	public static String getUserState() {
		return getPreferences().getString(USER_STATE, "Active");
	}
	
	
	
	public static boolean getLoginStatus(){
		return getPreferences().getBoolean(LOGIN_STATUS, false);
	}
	
	public static void setLoginStatus(boolean status){
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putBoolean(LOGIN_STATUS, status);
		applyPreferenceChanges(editor);
	}


	public static boolean getPushStatus(){
		return getPreferences().getBoolean(PUSH_STATUS, false);
	}
	
	public static void setPushStatus(boolean status){
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putBoolean(PUSH_STATUS, status);
		applyPreferenceChanges(editor);
	}

	
/*	public static boolean getPushStatus(){
		return getPreferences().getBoolean(PUSH_NOTIFICATION_STATUS, true);
	}
	
	public static void setPushStatus(boolean status){
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putBoolean(PUSH_NOTIFICATION_STATUS, status);
		applyPreferenceChanges(editor);
	}*/

	public static boolean getSoundStatus(){
		return getPreferences().getBoolean(SOUND_NOTIFICATION_STATUS, true);
	}
	
	public static void setSoundStatus(boolean status){
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putBoolean(SOUND_NOTIFICATION_STATUS, status);
		applyPreferenceChanges(editor);
	}
	
	public static int getNameChangeCount(){
		return getPreferences().getInt(NAME_CHANGE_COUNT, 0);
	}
	
	public static void setNameChangeCount(int count){
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putInt(NAME_CHANGE_COUNT, count);
		applyPreferenceChanges(editor);
	}
	
	@SuppressLint("NewApi")
	public static void setGroupInvitationList(List<String> list)
	{
		Set<String> set = new HashSet<String>();
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		set.addAll(list);
		editor.putStringSet(GROUP_INVITATION, set);
		editor.commit();
	}
	
	@SuppressLint("NewApi")
	public static Set<String> getGroupInvitationList()
	{
		return getPreferences().getStringSet(GROUP_INVITATION, set);
	}
	
	public static boolean isJoinFirst(){
		return getPreferences().getBoolean(JOINED_FIRST_GROUP, true);
	}
	
	public static void setJoinStatus(boolean status){
		SharedPreferences configPrefs = getPreferences();
		Editor editor = configPrefs.edit();
		editor.putBoolean(JOINED_FIRST_GROUP, status);
		applyPreferenceChanges(editor);
	}

}
