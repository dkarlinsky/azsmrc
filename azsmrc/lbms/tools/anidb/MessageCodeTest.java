package lbms.tools.anidb;

import junit.framework.TestCase;

/**
 * @author Damokles
 *
 */
public class MessageCodeTest extends TestCase {

	/**
	 * Test method for {@link lbms.tools.anidb.MessageCode#getTagByCode(int)}.
	 */
	public void testGetTagByCode() {
		if (!MessageCode.getTagByCode(300).equals(MessageCode.PONG)) {
			fail("Incorrect Code 300");
		}
		if (!MessageCode.getTagByCode(200).equals(MessageCode.LOGIN_ACCEPTED)) {
			fail("Incorrect Code 200");
		}
		if (!MessageCode.getTagByCode(394).equals(MessageCode.NO_SUCH_USER)) {
			fail("Incorrect Code 394");
		}
		if (!MessageCode.getTagByCode(519).equals(MessageCode.ENCODING_NOT_SUPPORTED)) {
			fail("Incorrect Code 519");
		}
		if (!MessageCode.getTagByCode(666).equals(MessageCode.API_VIOLATION)) {
			fail("Incorrect Code 666");
		}
		if (!MessageCode.getTagByCode(668).equals(MessageCode.INVALID_CODE)) {
			fail("Incorrect Code 668");
		}
		if (!MessageCode.getTagByCode(668).equals(MessageCode.INVALID_CODE)) {
			fail("Incorrect Code 100");
		}
		if (!MessageCode.getTagByCode(668).equals(MessageCode.INVALID_CODE)) {
			fail("Incorrect Code 800");
		}
		if (!MessageCode.getTagByCode(50).equals(MessageCode.INVALID_CODE)) {
			fail("Incorrect Code 50");
		}

	}

}
