package lbms.tools.anidb;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Damokles
 *
 */
public enum MessageCode {
	//http://wiki.anidb.info/w/UDP_API_Definition
	INVALID_CODE			(000),
	/*
	PONG					(300, "PONG"),
	//Login
	LOGIN_ACCEPTED			(200, "LOGIN ACCEPTED"),
	LOGIN_ACCEPTED_NEW_VER  (201, "LOGIN ACCEPTED - NEW VERSION AVAILABLE"),
	LOGIN_FAILED 			(500, "LOGIN FAILED"),
	LOGGED_OUT				(203, "LOGGED OUT"),
	NOT_LOGGED_IN			(403, "NOT_LOGGED_IN"),
	CLIENT_VER_OUTDATED 	(503, "CLIENT VERSION OUTDATED"),
	CLIENT_BANNED			(504, "CLIENT BANNED"),
	//Errors
	LOGIN_FIRST				(501, "LOGIN FIRST"),
	ACCESS_DENIED			(502, "ACCESS DENIED"),
	ILLEGAL_INPUT			(505, "ILLEGAL INPUT OR ACCESS DENIED"),
	INVALID_SESSION			(506, "INVALID SESSION"),
	UNKNOWN_COMMAND 		(598, "UNKNOWN COMMAND"),
	INTERNAL_SERVER_ERROR	(600, "INTERNAL SERVER ERROR"),
	OUT_OF_SERVICE 			(601, "ANIDB OUT OF SERVICE - TRY AGAIN LATER"),
	SERVER_BUSY 			(602, "SERVER BUSY - TRY AGAIN LATER"),
	//Notify
	NOTIFICATION_ENABLED	(270, "NOTIFICATION ENABLED"),
	NOTIFICATION			(290, "NOTIFICATION"),
	NOTIFYLIST				(291, "NOTIFYLIST"),
	MESSAGEGET_M			(292, "MESSAGEGET"),
	MESSAGEGET_NSE			(392, "NO SUCH ENTRY"),
	NOTIFYGET_N				(293, "NOTIFYGET"),
	NOTIFYGET_NSE			(393, "NO SUCH ENTRY"),
	MESSAGE_ACK_SUCC		(281, "NOTIFYACK SUCCESSFUL"),
	MESSAGE_ACK_NSE			(381, "NO SUCH ENTRY"),
	NOTIFY_ACK_SUCC			(282, "NOTIFYACK SUCCESSFUL"),
	NOTIFY_ACK_NSE			(382, "NO SUCH ENTRY"),
	//Buddy
	NO_SUCH_USER			(394, "NO SUCH USER"),
	BUDDY_ADDED 			(255, "BUDDY ADDED"),
	BUDDY_ALREADY_ADDED		(355, "BUDDY ALREADY ADDED"),
	NO_SUCH_BUDDY			(356, "NO SUCH BUDDY"),
	BUDDY_DELETED			(256, "BUDDY DELETED"),
	BUDDY_ACCEPTED  		(257, "BUDDY ACCEPTED"),
	BUDDY_ALREADY_ACCEPTED	(357, "BUDDY ALREADY ACCEPTED"),
	BUDDY_DENIED			(258, "BUDDY DENIED"),
	BUDDY_ALREADY_DENIED	(358, "BUDDY ALREADY DENIED"),
	BUDDYLIST				(253, "BUDDYLIST"),
	BUDDYSTATE				(254, "BUDDYSTATE"),
	//Anime
	ANIME					(230, "ANIME"),
	NO_SUCH_ANIME			(330, "NO SUCH ANIME"),
	EPISODE					(240, "EPISODE"),
	NO_SUCH_EPISODE			(340, "NO SUCH EPISODE"),
	FILE					(220, "FILE"),
	NO_SUCH_FILE			(320, "NO SUCH FILE"),
	MULTIPLE_FILES_FOUND	(322, "MULTIPLE FILES FOUND"),
	GROUP					(250, "GROUP"),
	NO_SUCH_GROUP			(350, "NO SUCH GROUP"),
	PRODUCER				(245, "PRODUCER"),
	NO_SUCH_PRODUCER		(345, "NO SUCH PRODUCER"),
	//MyList
	MYLIST					(221, "MYLIST"),
	MULTIPLE_MYLIST_ENTRIES	(312, "MULTIPLE MYLIST ENTRIES"),
	MYLIST_NSE				(321, "NO SUCH ENTRY"),
	MYLIST_ENTRY_ADDED		(210, "MYLIST ENTRY ADDED"),
	FILE_ALREADY_IN_MYLIST	(310, "FILE ALREADY IN MYLIST"),
	MYLIST_ENTRY_EDITED		(311, "MYLIST ENTRY EDITED"),
	NO_SUCH_MYLIST_ENTRY	(411, "NO SUCH MYLIST ENTRY"),
	MYLIST_ENTRY_DELETED	(211, "MYLIST ENTRY DELETED"),
	MYLIST_STATS			(222, "MYLIST STATS"),
	//Voting
	VOTED					(260, "VOTED"),
	VOTE_FOUND				(261, "VOTE FOUND"),
	VOTE_UPDATED			(262, "VOTE UPDATED"),
	VOTE_REVOKED			(263, "VOTE REVOKED"),
	VOTE_NSE				(360, "NO SUCH VOTE"),
	INVALID_VOTE_TYPE		(361, "INVALID VOTE TYPE"),
	INVALID_VOTE_VALUE		(362, "INVALID VOTE VALUE"),
	PERMVOTE_NOT_ALLOWED	(363, "PERMVOTE NOT ALLOWED"),
	PERMVOTED_ALREADY		(364, "ALREADY PERMVOTED"),
	//Encryption
	ENCRYPTION_ENABLED		(209, "ENCRYPTION ENABLED"),
	API_PW_NOT_DEF			(309, "API PASSWORD NOT DEFINED"),
	NO_SUCH_ENCRYPTION_TYPE	(509, "NO SUCH ENCRYPTION TYPE"),
	//Encoding
	ENCODING_CHANGED		(219, "ENCODING CHANGED"),
	ENCODING_NOT_SUPPORTED	(519, "ENCODING NOT SUPPORTED"),
	SENDMSG_SUCCESSFUL		(294, "SENDMSG SUCCESSFUL"),
	USER					(295, "USER"),
	UPTIME					(208, "UPTIME"),
	VERSION					(998, "VERSION"),*/

	LOGIN_ACCEPTED				(200), //a
	LOGIN_ACCEPTED_NEW_VER		(201), //a
	LOGGED_OUT					(203), //a
	RESOURCE					(205), //d
	STATS						(206), //b
	TOP							(207), //b
	UPTIME						(208), //b
	ENCRYPTION_ENABLED			(209), //c

	MYLIST_ENTRY_ADDED			(210), //a
	MYLIST_ENTRY_DELETED		(211), //a

	ADDED_FILE					(214), //e
	ADDED_STREAM				(215), //e

	ENCODING_CHANGED			(219), //c

	FILE						(220), //a
	MYLIST						(221), //a
	MYLIST_STATS				(222), //b

	ANIME						(230), //b
	ANIME_BEST_MATCH			(231), //b
	RANDOMANIME					(232), //b

	EPISODE						(240), //b
	PRODUCER					(245), //b
	GROUP						(250), //b

	BUDDY_LIST					(253), //c
	BUDDY_STATE					(254), //c
	BUDDY_ADDED					(255), //c
	BUDDY_DELETED				(256), //c
	BUDDY_ACCEPTED				(257), //c
	BUDDY_DENIED				(258), //c

	VOTED						(260), //b
	VOTE_FOUND					(261), //b
	VOTE_UPDATED				(262), //b
	VOTE_REVOKED				(263), //b

	NOTIFICATION_ENABLED		(270), //a
	NOTIFICATION_NOTIFY			(271), //a
	NOTIFICATION_MESSAGE		(272), //a
	NOTIFICATION_BUDDY			(273), //c
	NOTIFICATION_SHUTDOWN		(274), //c
	PUSHACK_CONFIRMED			(280), //a
	NOTIFYACK_SUCCESSFUL_M		(281), //a
	NOTIFYACK_SUCCESSFUL_N		(282), //a
	NOTIFICATION				(290), //a
	NOTIFYLIST					(291), //a
	NOTIFYGET_MESSAGE			(292), //a
	NOTIFYGET_NOTIFY			(293), //a

	SENDMSG_SUCCESSFUL			(294), //a
	USER						(295), //d

	// AFFIRMATIVE/NEGATIVE 3XX

	PONG						(300), //a
	AUTHPONG					(301), //c
	NO_SUCH_RESOURCE			(305), //d
	API_PASSWORD_NOT_DEFINED	(309), //c

	 FILE_ALREADY_IN_MYLIST		(310), //a
	MYLIST_ENTRY_EDITED			(311), //a
	MULTIPLE_MYLIST_ENTRIES		(312), //e

	SIZE_HASH_EXISTS			(314), //c
	INVALID_DATA				(315), //c
	STREAMNOID_USED				(316), //c

	NO_SUCH_FILE				(320), //a
	NO_SUCH_ENTRY				(321), //a
	MULTIPLE_FILES_FOUND		(322), //b

	NO_SUCH_ANIME				(330), //b
	NO_SUCH_EPISODE				(340), //b
	NO_SUCH_PRODUCER			(345), //b
	NO_SUCH_GROUP				(350), //b

	BUDDY_ALREADY_ADDED			(355), //c
	NO_SUCH_BUDDY				(356), //c
	BUDDY_ALREADY_ACCEPTED		(357), //c
	BUDDY_ALREADY_DENIED		(358), //c

	NO_SUCH_VOTE				(360), //b
	INVALID_VOTE_TYPE			(361), //b
	INVALID_VOTE_VALUE			(362), //b
	PERMVOTE_NOT_ALLOWED		(363), //b
	ALREADY_PERMVOTED			(364), //b

	NOTIFICATION_DISABLED		(370), //a
	NO_SUCH_PACKET_PENDING		(380), //a
	NO_SUCH_ENTRY_M				(381), //a
	NO_SUCH_ENTRY_N				(382), //a

	NO_SUCH_MESSAGE				(392), //a
	NO_SUCH_NOTIFY				(393), //a
	NO_SUCH_USER				(394), //a


	// NEGATIVE 4XX


	NOT_LOGGED_IN				(403), //a

	NO_SUCH_MYLIST_FILE			(410), //a
	NO_SUCH_MYLIST_ENTRY		(411), //a


	// CLIENT SIDE FAILURE 5XX


	LOGIN_FAILED				(500), //a
	LOGIN_FIRST					(501), //a
	ACCESS_DENIED				(502), //a
	CLIENT_VERSION_OUTDATED		(503), //a
	CLIENT_BANNED				(504), //a
	ILLEGAL_INPUT_OR_ACCESS_DENIED	(505), //a
	INVALID_SESSION				(506), //a
	NO_SUCH_ENCRYPTION_TYPE		(509), //c
	ENCODING_NOT_SUPPORTED		(519), //c

	BANNED						(555), //a
	UNKNOWN_COMMAND				(598), //a


	// SERVER SIDE FAILURE 6XX


	INTERNAL_SERVER_ERROR		(600), //a
	ANIDB_OUT_OF_SERVICE		(601), //a
	API_VIOLATION				(666); //a


	private int code;
	private static MessageCode[] searchArray;

	MessageCode (int code) {
		this.code = code;
	}

	public int getCode () {
		return code;
	}

	public static MessageCode getTagByCode (int code) {
		if (searchArray == null) {
			searchArray = values();
			Arrays.sort(searchArray, new Comparator<MessageCode>() {
				/* (non-Javadoc)
				 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
				 */
				public int compare(MessageCode o1, MessageCode o2) {
					return o1.code-o2.code;
				}
			});
		}
		int l = 0;
		int r = searchArray.length;
		int x = (l+r)/2;
		boolean found = false;

		while (true) {
			if (searchArray[x].code == code) {
				found = true;
				break;
			} else if (searchArray[x].code > code) {
				r = x;
			} else if (searchArray[x].code < code){
				l = x;
			}

			if (l==(r-1)) break;

			x = (l+r)/2;
		}

		if (found) {
			return searchArray[x];
		}

		return INVALID_CODE;
	}

}
