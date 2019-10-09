import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;

public class SmtpHelper {

	private EmailConfig emailConfig;
	private Session session;
	private Transport transport;

	public SmtpHelper(SmtpConfig smtpConfig, EmailConfig emailConfig, Properties systemProperties)
			throws SmtpMessagingException {
		super();
		if (systemProperties == null)
			throw new IllegalArgumentException(
					"System properties cannot be null. Please pass System.getProperties() as argument.");
		if (smtpConfig == null)
			throw new IllegalArgumentException("SMTP configuration cannot be null.");
		if (emailConfig == null)
			throw new IllegalArgumentException("Email configuration cannot be null.");
		this.emailConfig = emailConfig;

		// Stand by
		systemProperties.put("mail.transport.protocol", "smtp");
		systemProperties.put("mail.smtp.port", smtpConfig.port);
		systemProperties.put("mail.smtp.starttls.enable", "true");
		systemProperties.put("mail.smtp.auth", "true");
		session = Session.getDefaultInstance(systemProperties);
		try {
			transport = session.getTransport();
			transport.connect(smtpConfig.host, smtpConfig.smtpUsername, smtpConfig.smtpPassword);
		} catch (MessagingException e) {
			throw new SmtpMessagingException(e);
		}
	}

	public void resetEmailConfig(EmailConfig emailConfig) {
		if (emailConfig == null)
			throw new IllegalArgumentException("Email configuration cannot be null.");
		this.emailConfig = emailConfig;
	}

	public void send() throws SmtpMessagingException {
		if (emailConfig.hasNoReceiver())
			throw new NoReceiverException();
		try {
			// Ready
			MimeMessage msg = new MimeMessage(session);
			try {
				msg.setFrom(new InternetAddress(emailConfig.fromAddress, emailConfig.fromName));
			} catch (javax.mail.internet.AddressException e) {
				throw new AddressException(e, AddressException.AddressField.FROM);
			}
			try {
				if (emailConfig.toAddresses != null)
					msg.setRecipients(Message.RecipientType.TO,
							stringArrayToInternetAddressArray(emailConfig.toAddresses));
			} catch (javax.mail.internet.AddressException e) {
				throw new AddressException(e, AddressException.AddressField.TO);
			}
			try {
				if (emailConfig.ccAddresses != null)
					msg.setRecipients(Message.RecipientType.CC,
							stringArrayToInternetAddressArray(emailConfig.ccAddresses));
			} catch (javax.mail.internet.AddressException e) {
				throw new AddressException(e, AddressException.AddressField.CC);
			}
			try {
				if (emailConfig.bccAddresses != null)
					msg.setRecipients(Message.RecipientType.BCC,
							stringArrayToInternetAddressArray(emailConfig.bccAddresses));
			} catch (javax.mail.internet.AddressException e) {
				throw new AddressException(e, AddressException.AddressField.BCC);
			}
			if (emailConfig.useUtf8) {
				msg.setSubject(emailConfig.getSubject(), "utf-8");
				msg.setContent(emailConfig.getHtmlBody(), "text/html; charset=utf-8");
			} else {
				msg.setSubject(emailConfig.getSubject());
				msg.setContent(emailConfig.getHtmlBody(), "text/html");
			}

			// Shoot
			transport.sendMessage(msg, msg.getAllRecipients());
		} catch (AddressException e) {
			throw e; // Re-throw instead of handling
		} catch (MessagingException e) {
			throw new SmtpMessagingException(e);
		} catch (UnsupportedEncodingException e) {
			throw new SmtpMessagingException(e);
		}
	}

	private InternetAddress[] stringArrayToInternetAddressArray(String[] a)
			throws javax.mail.internet.AddressException {
		List<String> shrinked = new ArrayList<String>();
		for (String s : a)
			if (!StringUtils.isBlank(s))
				shrinked.add(s);
		InternetAddress[] addresses = new InternetAddress[shrinked.size()];
		for (int i = 0; i < addresses.length; i++)
			addresses[i] = new InternetAddress(shrinked.get(i));
		return addresses;
	}

	public static class SmtpConfig {

		private String smtpUsername;
		private String smtpPassword;
		private String host;
		private int port;

		public SmtpConfig(String smtpUsername, String smtpPassword, String host, int port) {
			super();
			if (StringUtils.isBlank(host))
				throw new IllegalArgumentException("Host cannot be blank.");
			if (port != 25 && port != 465 && port != 587 && port != 2525)
				throw new IllegalArgumentException("Only ports 25, 465, 587 and 2525 are supported.");
			this.smtpUsername = smtpUsername;
			this.smtpPassword = smtpPassword;
			this.host = host;
			this.port = port;
		}

		public String getSmtpUsername() {
			return smtpUsername;
		}

		public String getSmtpPassword() {
			return smtpPassword;
		}

		public String getHost() {
			return host;
		}

		public int getPort() {
			return port;
		}
	}

	public static class EmailConfig {

		private String fromAddress;
		private String fromName;
		private String subject;
		private String htmlBody;
		private boolean useUtf8;

		private String[] toAddresses;
		private String[] ccAddresses;
		private String[] bccAddresses;

		public EmailConfig(String fromAddress, String fromName, String subject, String htmlBody, boolean useUtf8) {
			super();
			this.fromAddress = fromAddress == null ? "" : fromAddress;
			this.fromName = fromName == null ? "" : fromName;
			this.subject = subject == null ? "" : subject;
			this.htmlBody = htmlBody == null ? "" : htmlBody;
			this.useUtf8 = useUtf8;
			toAddresses = null;
			ccAddresses = null;
			bccAddresses = null;
		}

		public String getFromAddress() {
			return fromAddress;
		}

		public void setFromAddress(String fromAddress) {
			this.fromAddress = fromAddress == null ? "" : fromAddress;
		}

		public String getFromName() {
			return fromName;
		}

		public void setFromName(String fromName) {
			this.fromName = fromName == null ? "" : fromName;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject == null ? "" : subject;
		}

		public String getHtmlBody() {
			return htmlBody;
		}

		public void setHtmlBody(String htmlBody) {
			this.htmlBody = htmlBody == null ? "" : htmlBody;
		}

		public void requireUtf8(boolean useUtf8) {
			this.useUtf8 = useUtf8;
		}

		public String[] getToAddresses() {
			return toAddresses;
		}

		public void setToAddresses(String[] toAddresses) {
			this.toAddresses = toAddresses;
		}

		public void setToAddress(String toAddress) {
			toAddresses = StringUtils.isBlank(toAddress) ? null : new String[] { toAddress };
		}

		public String[] getCcAddresses() {
			return ccAddresses;
		}

		public void setCcAddresses(String[] ccAddresses) {
			this.ccAddresses = ccAddresses;
		}

		public void setCcAddress(String ccAddress) {
			ccAddresses = StringUtils.isBlank(ccAddress) ? null : new String[] { ccAddress };
		}

		public String[] getBccAddresses() {
			return bccAddresses;
		}

		public void setBccAddresses(String[] bccAddresses) {
			this.bccAddresses = bccAddresses;
		}

		public void setBccAddress(String bccAddress) {
			bccAddresses = StringUtils.isBlank(bccAddress) ? null : new String[] { bccAddress };
		}

		public boolean hasNoReceiver() {
			return (toAddresses == null || toAddresses.length == 0) && (ccAddresses == null || ccAddresses.length == 0)
					&& (bccAddresses == null || bccAddresses.length == 0);
		}
	}
}
