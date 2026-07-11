package workflows;

import java.time.*;
import java.util.*;
import java.util.regex.*;
import org.openqa.selenium.WebElement;
import jakarta.mail.*;
import jakarta.mail.search.*;
import pageObjects.OtpPage;
import utilities.*;
import extensions.*;
import io.qameta.allure.Allure;

public class OtpFlow {

	private static String _user, _appPass, _domain, _text, _pattern;

	// Configure OTP mailbox (user/app password/domain to filter messages)
	public static void setOtp(String user, String appPass, String domain) {
		if (!user.isEmpty()) _user = user;
		if (!appPass.isEmpty()) _appPass = appPass;
		_domain = domain;
	}

	public static void setRegex(String pattern) {
		_pattern = pattern;
	}

	public static String getText() throws Exception {
		final long deadline = System.currentTimeMillis() + 15_000; // total wait ~15s
		final long pollEveryMs = 5_000;                            // poll interval 5s
		// Poll until text found or timeout reached
		while (System.currentTimeMillis() < deadline && (_text == null || _text.isBlank())) {
			Thread.sleep(pollEveryMs);
			_text = getEmailText();
		}
		return _text;
	}

	public static void typePassword(String password, String uri) {
		Allure.step("OTP flow", () -> {
			List<WebElement> elements = ManagePages.page(OtpPage.class).otpPassword();
			// If first word is a number -> treat as wait time (e.g. "10 Minutes")
			if (password.split(" ")[0].matches("\\d+")) { WaitForElement.waitForTime(password); _text = ""; }
			else _text = password;

			try {
				_text = getText();
				if (_text.isBlank()) Verifications.assertFailed("Text not found in the mail within timeout.");
				// Type each digit into its corresponding input
				for (int i = 0; i < elements.size(); i++) UiActions.enterText(elements.get(i), String.valueOf(_text.charAt(i)));
				// Verify and wait for navigation to profile creation
				UiActions.click(ManagePages.page(OtpPage.class).verifyPassword());
				WaitForElement.waitUntilUrlContains(uri);
			} catch (Exception e) {
				Verifications.assertFailed("Failed while waiting: " + e.getMessage());
			}
		});
	}

	// Returns last text from today's email sent from *@{domain}; empty string if none
	static String getEmailText() throws Exception {
		Properties props = new Properties();
		props.put("mail.store.protocol", "imaps"); // IMAPS (TLS)

		// Open IMAP store with try-with-resources
		try (Store store = Session.getInstance(props).getStore("imaps")) {
			store.connect("imap.gmail.com", 993, _user, _appPass);

			// Prefer Gmail "Primary" category; fallback to INBOX
			Folder folder = firstMail(store, "[Gmail]/Categories/Primary");
			folder.open(Folder.READ_ONLY);

			try {
				// Messages since start of local day, filtered by sender domain
				Date since = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
				Message[] messages = folder.search(new AndTerm(
						new FromStringTerm("@" + _domain),
						new ReceivedDateTerm(ComparisonTerm.GE, since)));
				if (messages.length == 0) return "";

				// Use the newest matching message's plain text, then find the last regex match in it
				String body = htmlToText(messages[messages.length - 1]).trim();
				Matcher matcher = Pattern.compile(_pattern).matcher(body);

				String result = "";
				while (matcher.find()) result = matcher.groupCount() > 0 ? matcher.group(1) : matcher.group();
				return result;
			} finally {
				try { folder.close(false); } catch (Exception ignore) {}
			}
		}
	}

	// Return first existing folder from list; otherwise INBOX
	static Folder firstMail(Store store, String... names) throws MessagingException {
		for (String n : names) {
			Folder f = store.getFolder(n);
			if (f != null && f.exists()) return f;
		}
		return store.getFolder("INBOX");
	}

	// Convert a Part to plain text: prefer text/plain; else cleaned text/html; recurse multiparts
	static String htmlToText(Part part) throws Exception {
		if (part.isMimeType("text/plain")) return String.valueOf(part.getContent());
		if (part.isMimeType("text/html"))  return normalizeHtml(String.valueOf(part.getContent()));

		if (part.isMimeType("multipart/*")) {
			Multipart mp = (Multipart) part.getContent();
			String htmlFallback = null;

			for (int i = 0; i < mp.getCount(); i++) {
				BodyPart b = mp.getBodyPart(i);
				if (b.isMimeType("text/plain")) return String.valueOf(b.getContent()).trim();
				if (b.isMimeType("text/html"))  htmlFallback = String.valueOf(b.getContent());
				if (b.isMimeType("multipart/*") || b.isMimeType("message/rfc822")) {
					String nested = htmlToText(b);
					if (!nested.isBlank()) return nested.trim();
				}
			}
			return htmlFallback == null ? "" : normalizeHtml(htmlFallback);
		}

		if (part.isMimeType("message/rfc822")) return htmlToText((Message) part.getContent());
		return "";
	}

	// Minimal HTML → text cleanup
	private static String normalizeHtml(String html) {
		return html.replaceAll("(?s)<[^>]*>", " ").replaceAll("\\s+", " ").trim();
	}
}
