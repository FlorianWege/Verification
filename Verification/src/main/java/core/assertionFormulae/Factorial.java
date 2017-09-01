package core.assertionFormulae;

public class Factorial {
	public int calc(int x) {
		int y = 1;
		int z = 0;
		
		while (z != x) {
			z = z + 1;
			y = y * z;
		}
		
		return y;
	}
	
	public Factorial() {
		int[] tests = new int[]{0, 1, 2, 3, -1, -2, -3};
		
		for (int test : tests) {
			System.out.println(test + " -> " + calc(test));
		}
	}
	
	public static void main(String[] args) {
		new Factorial();
	}
}