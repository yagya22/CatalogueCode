import java.io.*;
import java.math.BigInteger;
import java.util.*;
import org.json.*;

public class SecretSharing {

    public static void main(String[] args) throws Exception {
        // Assuming the test cases are in files named "testcase1.json" and "testcase2.json"
        String[] testFiles = {"resources/testcase1.json", "resources/testcase2.json"};

        for (String testFile : testFiles) {
            JSONObject jsonObject = readJSONFile(testFile);

            int n = jsonObject.getJSONObject("keys").getInt("n");
            int k = jsonObject.getJSONObject("keys").getInt("k");

            Map<Integer, BigInteger> xValues = new HashMap<>();
            Map<Integer, BigInteger> yValues = new HashMap<>();

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String key = keys.next();

                if (key.equals("keys")) continue;

                int x = Integer.parseInt(key);
                JSONObject point = jsonObject.getJSONObject(key);
                String baseStr = point.getString("base");
                String valueStr = point.getString("value");

                int base = Integer.parseInt(baseStr);
                BigInteger y = new BigInteger(valueStr, base);

                xValues.put(x, BigInteger.valueOf(x));
                yValues.put(x, y);
            }

            // Use the first k points
            List<Integer> xList = new ArrayList<>(xValues.keySet());
            Collections.sort(xList);
            List<Integer> xPoints = xList.subList(0, k);

            // Compute c using Lagrange interpolation
            Rational c = lagrangeInterpolation(xPoints, xValues, yValues);

            // Since c should be integer, divide numerator by denominator
            BigInteger secretC = c.numerator.divide(c.denominator);

            System.out.println(secretC);
        }
    }

    private static JSONObject readJSONFile(String filename) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        StringBuilder jsonBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            jsonBuilder.append(line);
        }
        reader.close();
        return new JSONObject(jsonBuilder.toString());
    }

    private static Rational lagrangeInterpolation(List<Integer> xPoints, Map<Integer, BigInteger> xValues, Map<Integer, BigInteger> yValues) {
        Rational result = new Rational(BigInteger.ZERO, BigInteger.ONE);

        int k = xPoints.size();
        for (int i = 0; i < k; i++) {
            int xi = xPoints.get(i);
            BigInteger yi = yValues.get(xi);

            Rational li0 = computeLi0(xi, xPoints);

            Rational term = li0.multiply(yi);

            result = result.add(term);
        }

        return result;
    }

    private static Rational computeLi0(int xi, List<Integer> xPoints) {
        Rational numerator = new Rational(BigInteger.ONE, BigInteger.ONE);
        Rational denominator = new Rational(BigInteger.ONE, BigInteger.ONE);

        for (int xj : xPoints) {
            if (xj != xi) {
                // numerator *= (0 - xj)
                numerator = numerator.multiply(BigInteger.valueOf(-xj));

                // denominator *= (xi - xj)
                denominator = denominator.multiply(BigInteger.valueOf(xi - xj));
            }
        }

        // li0 = numerator / denominator
        return numerator.divide(denominator);
    }

    // Rational number class with BigInteger numerator and denominator
    static class Rational {
        BigInteger numerator;
        BigInteger denominator;

        public Rational(BigInteger num, BigInteger den) {
            if (den.equals(BigInteger.ZERO)) {
                throw new ArithmeticException("Denominator cannot be zero");
            }
            // Normalize the sign
            if (den.compareTo(BigInteger.ZERO) < 0) {
                num = num.negate();
                den = den.negate();
            }
            BigInteger gcd = num.gcd(den);
            numerator = num.divide(gcd);
            denominator = den.divide(gcd);
        }

        public Rational add(Rational other) {
            BigInteger num = this.numerator.multiply(other.denominator).add(other.numerator.multiply(this.denominator));
            BigInteger den = this.denominator.multiply(other.denominator);
            return new Rational(num, den);
        }

        public Rational multiply(Rational other) {
            BigInteger num = this.numerator.multiply(other.numerator);
            BigInteger den = this.denominator.multiply(other.denominator);
            return new Rational(num, den);
        }

        public Rational multiply(BigInteger value) {
            BigInteger num = this.numerator.multiply(value);
            return new Rational(num, this.denominator);
        }

        public Rational divide(Rational other) {
            return this.multiply(new Rational(other.denominator, other.numerator));
        }

        public Rational multiply(int value) {
            return this.multiply(BigInteger.valueOf(value));
        }

        @Override
        public String toString() {
            return numerator + "/" + denominator;
        }
    }
}
