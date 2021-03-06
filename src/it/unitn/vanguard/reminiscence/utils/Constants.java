package it.unitn.vanguard.reminiscence.utils;

public class Constants {

	public static final String SERVER_URL = "http://theflame92.altervista.org/";
	public static final int SUGGESTION_RESULT_LIMIT = 3;

	public static final int PASSWORD_MIN = 1;
	public static final int PASSWORD_MAX = 1000;

	public static final int QUESTION_POPUP_SHOWING_TIME = 10000;
	public static final int QUESTION_INTERVAL = 120000;
	
	public static final String LOUGO_DI_NASCITA_PREFERENCES_KEY = "luogodinascita";	
	public static final String NAME_KEY = "name";
	public final static String SURNAME_KEY = "surname";
	public final static String MAIL_KEY = "mail";
	public final static String DAY_KEY = "day";
	public final static String MONTH_KEY = "month";
	public final static String YEAR_KEY = "year";
	public final static String PASSWORD_KEY = "password";
	public final static String TOKEN_KEY = "token";
	public final static String LANGUAGE_KEY = "language";
	
	// Gestisce il cambio tra storie pubbliche e storie private
	public final static String PRIVATE_STORIES = "Private";
	public final static String PUBLIC_STORIES = "Public";
	// Di default setto il tipo di storie visibili con PRIVATE_STORIES
	public final static String ACTIVE_STORIES = PRIVATE_STORIES;
	
	public static enum imageType { PROFILE, STORY };
}
