	class PCAListPoint extends ListPoint {
		double[][] coord;
		double[] corr;
		String[] name;
		public PCAListPoint(double[][] r, double[] c, String[] n) {
			coord = r; corr = c; name = n;
		}
		public double getX(int indice) {
			return coord[indice][0];
		}
		public double getY(int indice) {
			return coord[indice][1];
		}
		public String getName(int indice) {
			return name[indice];
		}
		public int size() {
			return coord.length;
		}
		public double getCorr(int indice) {
			return corr[indice];
		}
	}
