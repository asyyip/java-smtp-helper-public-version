public class AddressException extends SmtpMessagingException {

	/**
	 * Auto generated serialVersionUID.
	 */
	private static final long serialVersionUID = -9211399036782588788L;
	
	private javax.mail.internet.AddressException wrappedEx;
	private AddressField causedField;

	public AddressException(javax.mail.internet.AddressException wrappedEx, AddressField causedField) {
		super();
		if (wrappedEx == null)
			throw new IllegalArgumentException("Wrapped exception must not be null.");
		if (causedField == null)
			throw new IllegalArgumentException("Caused field must not be null.");
		this.wrappedEx = wrappedEx;
		this.causedField = causedField;
	}

	public String getRef() {
		return wrappedEx.getRef();
	}

	public int getPos() {
		return wrappedEx.getPos();
	}

	public AddressField getCausedField() {
		return causedField;
	}

	@Override
	public String toString() {
		return wrappedEx.toString() + " from the address of " + causedField;
	}

	public enum AddressField {

		FROM {
			@Override
			public String toString() {
				return "sender";
			}
		},
		TO {
			@Override
			public String toString() {
				return "TO receiver";
			}
		},
		CC {
			@Override
			public String toString() {
				return "CC receiver";
			}
		},
		BCC {
			@Override
			public String toString() {
				return "BCC receiver";
			}
		}
	}
}
