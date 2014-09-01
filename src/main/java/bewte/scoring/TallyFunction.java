package bewte.scoring;

import java.util.List;

import bewte.BE;
import bewte.BE.BEPart;

public interface TallyFunction {
	
	public double tally(double refCount, BE be);
	
	public static class BinaryTallyFunction implements TallyFunction {
		public double tally(double refCount, BE be) {
			return refCount >= 1 ? 1.0 : 0.0;
		}
	}
	
	public static class LogTallyFunction implements TallyFunction {
		public double tally(double refCount, BE be) {
			return Math.log(1+refCount)/Math.log(2);
		}
	}
	
	public static class RootTallyFunction implements TallyFunction {
		public double tally(double refCount, BE be) {
			return Math.sqrt(refCount);
		}
	}
	
	public static class TotalTallyFunction implements TallyFunction {
		public double tally(double refCount, BE be) {
			return refCount;
		}
	}
	
	public static class BeLengthTallyFunction implements TallyFunction {
		public double tally(double refCount, BE be) {
			List<BEPart> parts = be.getParts();
			double totalLength = 1;
			for(BEPart part : parts) {
				totalLength += part.text.length();
			}
			return refCount >= 1 ? totalLength : 0.0;
		}
	}
}