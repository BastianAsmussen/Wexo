package tech.asmussen.util;

public final class MathExtras {
	
	public static double getAspectRatio(int width, int height) {
		
		return (double) width / height;
	}
	
	public static String decimalToFraction(double number) {
		
		// Fetch integral value of the decimal.
		double intValue = Math.floor(number);
		
		// Fetch fractional part of the decimal.
		double fValue = number - intValue;
		
		// Consider precision value to convert fractional part to integral equivalent.
		final long pValue = 1_000_000_000;
		
		// Calculate GCD of integral equivalent of fractional part and precision value.
		long gcdValue = getGreatestCommonDivisor(Math.round(fValue * pValue), pValue);
		
		// Calculate numerator and denominator of the fraction.
		long numerator = Math.round(fValue * pValue) / gcdValue;
		long denominator = pValue / gcdValue;
		
		// If fractional part is 0, return integral value.
		return (intValue == 0 ? "" : intValue + " ") + numerator + "/" + denominator;
	}
	
	private static long getGreatestCommonDivisor(long a, long b) {
		
		if (a == 0) {
			
			return b;
			
		} else if (b == 0) {
			
			return a;
		}
		
		if (a < b) {
			
			return getGreatestCommonDivisor(a, b % a);
			
		} else {
			
			return getGreatestCommonDivisor(b, a % b);
		}
	}
}
