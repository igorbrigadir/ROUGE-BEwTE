package bewte.annotations;

import tratz.runpipe.TextDocument;

public class DateAnnotation extends CanonicalizingAnnotation {
	
	public static final long serialVersionUID = 1;
	
	private String mMonth;
	private String mDate;
	private String mYear;
	
	public DateAnnotation(TextDocument doc, int start, int end, String month, String date, String year) {
		super(doc, start, end);
		mMonth = month;
		mDate = date;
		mYear = year;
	}
	
	@Override
	public String getCanonicalString() {
		return mMonth + "-" + mDate + "-" + mYear;
	}
	
	@Override
	public String getCanonicalType() {
		return DateAnnotation.class.getSimpleName();
	}
	
}