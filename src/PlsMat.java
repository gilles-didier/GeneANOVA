//package Jama;

import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.text.FieldPosition;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.StreamTokenizer;

import java.io.*;

//import Jama.util.*;



public class PlsMat //implements Cloneable, java.io.Serializable
{


        private double[][] A;


        private int m, n;

        /* ------------------------
           Constructors
         * ------------------------ */


        public PlsMat (int m, int n) {
                this.m = m;
                this.n = n;
                A = new double[m][n];
        }



        public PlsMat (int m, int n, double s) {
                this.m = m;
                this.n = n;
                A = new double[m][n];
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < n; j++) {
                                A[i][j] = s;
                        }
                }
        }


        public PlsMat (double[][] A) {
                m = A.length;
                n = A[0].length;
                for (int i = 0; i < m; i++) {
                        if (A[i].length != n) {
                                throw new IllegalArgumentException("All rows must have the same length.");
                        }
                }
                this.A = A;
        }



        /* ------------------------
           Public Methods
         * ------------------------ */


        /** Make a deep copy of a PlsMat
        */

        public boolean NaNinside() {
                boolean x = false;
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < n; j++) {
                                if (Double.isNaN(A[i][j]))
                                        x = true;
                        }
                }

                return x;
        }

        //*********************************************************



        public PlsMat copy () {
                PlsMat X = new PlsMat(m, n);
                double[][] C = X.getArray();
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < n; j++) {
                                C[i][j] = A[i][j];
                        }
                }
                return X;
        }

        /** Access the internal two-dimensional array.
        @return     Pointer to the two-dimensional array of PlsMat elements.
        */
        public double[][] getArray () {
                return A;
        }

        //*******************************************************************

        public int getRowDimension () {
                return m;
        }

        //**************************************************************
        public int getColumnDimension () {
                return n;
        }
        //*****************************************************
        public double get (int i, int j) {
                return A[i][j];
        }
        //*****************************************************

        // sous matrice superieure
        public PlsMat getupperMatrix(int l, int c) {
                PlsMat X = new PlsMat(l, c);
                double[][] B = X.getArray();
                try {
                        for (int i = 0; i < l; i++) {
                                for (int j = 0; j < c; j++) {
                                        B[i][j] = A[i][j];
                                }
                        }
                } catch (ArrayIndexOutOfBoundsException e) {
                        throw new ArrayIndexOutOfBoundsException("SubMatrix indices erreurs");
                }
                catch (NegativeArraySizeException ee) {
                        throw new NegativeArraySizeException ("SubMatrix Negatives indices ");
                }
                return X;
        }

        //**********************************************************************************
        public PlsMat getMatrix (int i0, int i1, int j0, int j1) {
                PlsMat X = new PlsMat(i1 - i0 + 1, j1 - j0 + 1);
                double[][] B = X.getArray();
                try {
                        for (int i = i0; i <= i1; i++) {
                                for (int j = j0; j <= j1; j++) {
                                        B[i - i0][j - j0] = A[i][j];
                                }
                        }
                } catch (ArrayIndexOutOfBoundsException e) {
                        throw new ArrayIndexOutOfBoundsException("Submatrix indices");
                }
                return X;
        }
        //**************************************************************************
        /** PlsMat transpose.
        @return    A'
        */

        public PlsMat transpose () {
                PlsMat X = new PlsMat(n, m);
                double[][] C = X.getArray();
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < n; j++) {
                                C[j][i] = A[i][j];
                        }
                }
                return X;
        }
        //****************CENTER*****************************************************
        //avec donnees  manquantes possibles
        public PlsMat center () throws java.io.IOException {
                int nbnb = 0;
                PlsMat X = new PlsMat(m, n);
                double s = 0;
                double[][] M = X.getArray();
                for (int j = 0; j < n ; j++) {
                        s = 0;
                        nbnb = 0;
                        for (int i = 0 ; i < m ; i++) {
                                if (!(Double.isNaN(A[i][j]))) {
                                        s = s + A[i][j];
                                        nbnb++;
                                }
                        } //fin Sommes
                        if (nbnb == 0 | nbnb == 1)
                                throw new java.io.IOException("Column with 1 or 0 NaN");
                        else {
                                s = s / nbnb; //Mean Value
                        }
                        for (int i = 0; i < m ; i++) {
                                if (!(Double.isNaN(A[i][j])))
                                        M[i][j] = A[i][j] - s;
                                else
                                        M[i][j] = Double.NaN;
                        }
                }
                return X;
        }


        //***********************REDUIRE********************************************
        //avec donnees  manquantes possibles
        public PlsMat reduce() throws java.io.IOException {
                PlsMat X = new PlsMat(m, n);
                double s = 0;
                double var = 0;
                int nbnb = 0;
                double[][] M = X.getArray();
                for (int j = 0; j < n ; j++) {
                        s = 0;
                        var = 0;
                        nbnb = 0;
                        for (int i = 0 ; i < m ; i++)
                                if (!(Double.isNaN(A[i][j]))) {
                                        s = s + A[i][j];
                                        nbnb++;
                                }
                        s = s / nbnb;
                        if (nbnb == 0 | nbnb == 1)
                                throw new java.io.IOException("Column with 1 or 0 NaN");
                        //System.out.println(s);
                        for (int i = 0 ; i < m ; i++)
                                if (!(Double.isNaN(A[i][j])))
                                        var = var + (A[i][j] - s) *
                                                (A[i][j] - s);
                        var = var / (nbnb - 1);
                        //System.out.println(var);
                        var = Math.sqrt(var);
                        //System.out.println(var);
                        for (int i = 0 ; i < m ; i++) {
                                if (!(Double.isNaN(A[i][j])))
                                        M[i][j] = A[i][j] / var;
                                else
                                        M[i][j] = Double.NaN;
                        }
                }
                return X;
        }

        //************************************************************************
        //NormeE
        public double normE() {
                if (n > 1) {
                        throw new IllegalArgumentException("Arg must be a pure vector");
                }
                double s = 0;
                for (int i = 0; i < m; i++) {
                        s += A[i][0] * A[i][0];
                }
                return Math.sqrt(s);
        }

        //**************************************************

        //Methode normaliser
        public void normaliser() {
                if (n > 1) {
                        throw new IllegalArgumentException("method normaliser use only pure vector");
                }
                double s = 0;
                for (int i = 0; i < m; i++) {
                        s += A[i][0] * A[i][0];//calcul normeE
                }
                s = Math.sqrt(s);
                if (s != 0) {
                        for (int i = 0; i < m; i++) {
                                A[i][0] = A[i][0] / s;
                        }
                }
        }
        //**********************************************

        /** Infinity norm
        @return    maximum row sum.
        */

        public double normInf () {
                double f = 0;
                for (int i = 0; i < m; i++) {
                        double s = 0;
                        for (int j = 0; j < n; j++) {
                                s += Math.abs(A[i][j]);
                        }
                        f = Math.max(f, s);
                }
                return f;
        }
        //*********************REGRESSION****1********************************************
        public PlsMat regress1 (PlsMat V) {
                double num = 0;
                double denom = 0;
                double coeff = 0;
                PlsMat X = new PlsMat(n, 1);
                double[][] M = X.getArray();

                for (int j = 0 ; j < n; j++) {
                        num = 0;
                        denom = 0;
                        for (int i = 0; i < m ; i++) {
                                coeff = V.get (i, 0);
                                if (!(Double.isNaN(A[i][j])) &
                                        !(Double.isNaN(coeff))) {
                                        num = num + A[i][j] * coeff;
                                        denom = denom + coeff * coeff;
                                }
                        }
                        M[j][0] = num / denom;
                }


                return X;
        }

        //*************Regression2**************************************************
        public PlsMat regress2 (PlsMat V) {
                double num = 0;
                double denom = 0;
                double coeff = 0;
                PlsMat X = new PlsMat(m, 1);
                double[][] M = X.getArray();

                for (int i = 0 ; i < m; i++) {
                        num = 0;
                        denom = 0;
                        for (int j = 0; j < n ; j++) {
                                if (!(Double.isNaN(A[i][j]))) {
                                        coeff = V.get (j, 0);
                                        num = num + A[i][j] * coeff;
                                        denom = denom + coeff * coeff;
                                }
                        }
                        M[i][0] = num / denom;

                }


                return X;
        }
        //*********************************************************************************
        /** C = A + B
        @param B    another PlsMat
        @return     A + B
        */

        public PlsMat plus (PlsMat B) {
                checkPlsMatDimensions(B);
                PlsMat X = new PlsMat(m, n);
                double[][] C = X.getArray();
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < n; j++) {
                                C[i][j] = A[i][j] + B.A[i][j];
                        }
                }
                return X;
        }



        /** C = A - B
        @param B    another PlsMat
        @return     A - B
        */
        //Minus Avec**prise**en**charge*******donnees***manquantes************************************
        public PlsMat minus (PlsMat B) {
                checkPlsMatDimensions(B);
                PlsMat X = new PlsMat(m, n);
                double[][] C = X.getArray();
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < n; j++) {
                                if (Double.isNaN(A[i][j]) |
                                        Double.isNaN(B.A[i][j])) {
                                        C[i][j] = Double.NaN;
                                } else {
                                        C[i][j] = A[i][j] - B.A[i][j];
                                }
                        }
                }
                return X;
        }
        //********************************************************************************************

        /** Multiply a PlsMat by a scalar, C = s*A
        @param s    scalar
        @return     s*A
        */

        public PlsMat times (double s) {
                PlsMat X = new PlsMat(m, n);
                System.out.println("s="+s);
                double[][] C = X.getArray();
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < n; j++) {
                                C[i][j] = s * A[i][j];
                        }
                }
                return X;
        }



        /** Linear algebraic PlsMat multiplication, A * B
        @param B    another PlsMat
        @return     PlsMat product, A * B
        @exception  IllegalArgumentException PlsMat inner dimensions must agree.
        */

        public PlsMat times (PlsMat B) {
                if (B.m != n) {
                        throw new IllegalArgumentException("PlsMat inner dimensions must agree.");
                }
                PlsMat X = new PlsMat(m, B.n);
                double[][] C = X.getArray();
                double[] Bcolj = new double[n];
                for (int j = 0; j < B.n; j++) {
                        for (int k = 0; k < n; k++) {
                                Bcolj[k] = B.A[k][j];
                        }
                        for (int i = 0; i < m; i++) {
                                double[] Arowi = A[i];
                                double s = 0;
                                for (int k = 0; k < n; k++) {
                                        s += Arowi[k] * Bcolj[k];
                                }
                                C[i][j] = s;
                        }
                }
                return X;
        }

        //Inverse Matrix
        //en construction
        //************************MATRIX*********INVERSE************************************

        public PlsMat inverse () {
                if (m != n) {
                        throw new IllegalArgumentException("PlsMat must be squarred...");
                }
                PlsMat X = new PlsMat(m, m);
                double [][] N = new double[m][m];
                double [][] M = X.getArray();
                double mult;
                int l;
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < m; j++) {
                                N[i][j] = A[i][j];
                                if (i == j)
                                        M[i][j] = 1;
                                else
                                        M[i][j] = 0;
                        }
                }
                for (int j = 0; j < n; j++) {
                        for (int i = 0; i < m; i++) {
                                if (i != j) {
                                        if (Math.abs(N[j][j]) <
                                                0.01) {//si diag nulle on somme les autres lignes sur ligne j
                                                l = 0;
                                                do {
                                                        for (int k = 0;
                                                                k < m; k++) {
                                                                N[j][k] =
                                                                        N[j][k]
                                                                        + N[l][k];
                                                                M[j][k] =
                                                                        M[j][k]
                                                                        + M[l][k];
                                                                l++;
                                                        }
                                                } while ( Math.abs(
                                                        N[j][j]) <
                                                        0.01 & l < n)
                                                        ;
                                        }
                                        if (N[i][j] < 1E8) {
                                                mult = N[j][j] / N[i][j];
                                                for (int k = 0; k < m;
                                                        k++) {// preparation des lignes
                                                        N[ i][k] = N[i][k]
                                                        * mult;
                                                        M[i][k] = mult * M[
                                                                i][k];
                                                }
                                                for (int k = 0; k < m;
                                                        k++) {
                                                        N[i][k] = N[i][k]
                                                                - N[j][k]
                                                                ;//soustractions
                                                                M[
                                                                i][k] = M[
                                                                i][k] - M[
                                                                j][k];
                                                }
                                        }
                                }
                        }
                }

                for (int i = 0; i < m; i++) {
                        if (N[i][i] == 0)
                                throw new IllegalArgumentException("Non invertible matrix");
                        for (int j = 0; j < m; j++) {
                                M[i][j] = M[i][j] / N[i][i];
                        }
                }
                return X;
        }

        //*****************MATRIX*************INV***********************************************
        public PlsMat inv () {
                if (m != n) {
                        throw new IllegalArgumentException("PlsMat must be squarred...");
                }
                PlsMat X = new PlsMat(m, m);
                PlsMat Y = new PlsMat(m, m);
                double [][] N = Y.getArray();
                double [][] M = new double[m][m];
                double [][] P = X.getArray();
                double pivot;
                int lpivot;
                for (int i = 0; i < m; i++) { //initialisation matrice depard matrice identite
                        for (int j = 0; j < m; j++) {
                                N[i][j] = A[i][j];
                                if (i == j)
                                        M[i][j] = 1;
                                else
                                        M[i][j] = 0;
                        }
                }
                for (int cpivot = 0; cpivot < m; cpivot++) {
                        pivot = 0;
                        lpivot = 0;
                        for (int i = 0; i < m; i++) { //recherche pivot sur la colonne
                                if (N[i][cpivot] > pivot)
                                        lpivot = i;
                        }
                        pivot = N[lpivot][cpivot];
                        if (pivot < 1E-11)
                                throw new IllegalArgumentException("Non invertible matrix");
                        for (int i = 0; i < m; i++) {
                                // if (listepivot[i]){
                                if (i != lpivot) {
                                        for (int j = 0; j < m; j++) {//on compacte deux operations en une
                                                N[i][j] = (N[i][j] * pivot -
                                                        N[i][cpivot] *
                                                        N[lpivot][j])
                                                        / pivot;
                                                M[i][j] = (M[i][j] * pivot -
                                                        M[i][cpivot] *
                                                        M[lpivot][j])
                                                        / pivot;
                                        }
                                } else {
                                        for (int j = 0; j < m; j++) {
                                                N[lpivot][j] =
                                                        N[lpivot][j] /
                                                        pivot;//ligne du pivot
                                                        }
                                }
                                // }
                        }
                }
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < m; j++) {
                                if (N[i][j] == 1) {
                                        for (int k = 0; k < m; k++)
                                                P[j][k] = N[i][k];
                                }
                        }
                }

                return X;
        }
        //******************************************************
        //*****************MATRIX*************INV***********************************************
        public PlsMat inv2 () {
                if (m != n) {
                        throw new IllegalArgumentException("PlsMat must be squarred...");
                }
                PlsMat X = new PlsMat(m, m);
                PlsMat Y = new PlsMat(m, m);
		PlsMat Z = new PlsMat(m, m);
                double [][] N = Y.getArray();
		double [][] M = Z.getArray();
                //double [][] M = new double[m][m];
                double [][] P = X.getArray();
                double pivot; double mult;
                int lpivot;
                boolean [] pivotlibre = new boolean[m];
                for (int i = 0; i < m; i++) { //initialisation matrice depard matrice identite
                        pivotlibre[i] = true;
                        for (int j = 0; j < m; j++) {
                                N[i][j] = A[i][j];
                                if (i == j)
                                        M[i][j] = 1;
                                else
                                        M[i][j] = 0;
                        }
                }
		//Z.print(5,3);
                pivotlibre[0] = true;
                M[0][0] = 1;
                for (int cpivot = 0; cpivot < m; cpivot++) {
                        pivot = 0;
                        lpivot = 0;
                        for (int i = 0; i < m; i++) { //recherche pivot sur la colonne
                                if ((Math.abs(N[i][cpivot]) >
                                        Math.abs(pivot)) & pivotlibre[i]) {
                                        lpivot = i;
                                        pivot = N[i][cpivot];
                                }
                        }
                        //System.out.println("pivot=ligne"+lpivot);
                        pivotlibre[lpivot] = false; //il ne doit plus y avoir de pivot sur cette ligne
                        pivot = N[lpivot][cpivot];
			//System.out.println("pivot= "+pivot);
                        if (Math.abs(pivot) < 1E-50)
                                throw new IllegalArgumentException("Non invertible matrix");
                        for (int j = 0; j < m; j++) {// on "normalise" la ligne du pivot
                                N[lpivot][j] = N[lpivot][j] / pivot;
                                M[lpivot][j] = M[lpivot][j] / pivot;
                        }

                        for (int i = 0; i < m; i++) {// soustraction des lignes pour faire apparaitre les zeros
                                if (i != lpivot) {
				        mult = N[i][cpivot];
                                        for (int k = 0; k < m; k++) {
                                                N[i][k] = N[i][k] - N[lpivot][k]*mult;
                                                M[i][k] = M[i][k] - M[lpivot][k]*mult;
                                        }
                                }
                        }
		//System.out.println("Matrice Y");	
		//Y.print(5,3);
		//System.out.println("Matrice Z");	
		//Z.print(5,3);			
                }
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < m; j++) {
                                if (N[i][j] == 1) {
                                        for (int k = 0; k < m; k++)
                                                P[j][k] = M[i][k];
                                }
                        }
                }

                return X;
        }


        //***************************************************************************
        /** Print the PlsMat to stdout.   Line the elements up in columns
          * with a Fortran-like 'Fw.d' style format.
        @param w    Column width.
        @param d    Number of digits after the decimal.
        */

        public void print (int w, int d) {
                //System.out.println("row "+A.length);
                print(new PrintWriter(System.out, true), w, d);
        }

        /** Print the PlsMat to the output stream.   Line the elements up in
          * columns with a Fortran-like 'Fw.d' style format.
        @param output Output stream.
        @param w      Column width.
        @param d      Number of digits after the decimal.
        */

        public void print (PrintWriter output, int w, int d) {
                //System.out.println("row "+A.length);
                DecimalFormat format = new DecimalFormat();
                format.setDecimalFormatSymbols(
                        new DecimalFormatSymbols(Locale.US));
                format.setMinimumIntegerDigits(1);
                format.setMaximumFractionDigits(d);
                format.setMinimumFractionDigits(d);
                format.setGroupingUsed(false);
                print(output, format, w + 2);
        }

        /** Print the PlsMat to stdout.  Line the elements up in columns.
          * Use the format object, and right justify within columns of width
          * characters.
          * Note that is the PlsMat is to be read back in, you probably will want
          * to use a NumberFormat that is set to US Locale.
        @param format A  Formatting object for individual elements.
        @param width     Field width for each column.
        @see java.text.DecimalFormat#setDecimalFormatSymbols
        */

        public void print (NumberFormat format, int width) {
                print(new PrintWriter(System.out, true), format, width);
        }

        // DecimalFormat is a little disappointing coming from Fortran or C's printf.
        // Since it doesn't pad on the left, the elements will come out different
        // widths.  Consequently, we'll pass the desired column width in as an
        // argument and do the extra padding ourselves.

        /** Print the PlsMat to the output stream.  Line the elements up in columns.
          * Use the format object, and right justify within columns of width
          * characters.
          * Note that is the PlsMat is to be read back in, you probably will want
          * to use a NumberFormat that is set to US Locale.
        @param output the output stream.
        @param format A formatting object to format the PlsMat elements
        @param width  Column width.
        @see java.text.DecimalFormat#setDecimalFormatSymbols
        */

        public void print (PrintWriter output, NumberFormat format,
                int width) {
                output.println(); // start on new line.
                System.out.println("Number of row "+A.length);
                for (int i = 0; i < m; i++) {
                        for (int j = 0; j < n; j++) {
                                String s;
                                if (Double.isNaN(A[i][j])) {
                                        s = "NaN";
                                } else {
                                        s = format.format(A[i][j]);
                                } // format the number
                                int padding = Math.max(1,
                                        width - s.length()); // At _least_ 1 space
                                        for ( int k = 0; k < padding; k++)
                                        output.print(' ');
                                output.print(s);
                        }
                        output.println();
                }
                output.println(); // end with blank line.
        }



        //*******Read**Matrice*****************************************************************
        public static PlsMat readMat(BufferedReader input)
                throws java.io.IOException {

                StreamTokenizer tokenizer = new StreamTokenizer(input);
                //************
                tokenizer.resetSyntax(); //reset the tokenizer syntax table
                tokenizer.wordChars(0, 255); // consider as word
                tokenizer.whitespaceChars(0, ' '); //consider as whites spaces
                tokenizer.eolIsSignificant(true);//end of lines consider as token
                //********************
                java.util.Vector v = new java.util.Vector();//create a useful vector
                // Ignore initial empty lines
                while (tokenizer.nextToken() == StreamTokenizer.TT_EOL)
                        ;
                //***********test**empty**file
                if (tokenizer.ttype == StreamTokenizer.TT_EOF)
                        throw new java.io.IOException("Unexpected EOF on PlsMat read.");
                //************take*the*word*an**change*the*string*in*number*and*put*in*vector***
                if (tokenizer.ttype == StreamTokenizer.TT_WORD)// on prend le premier double
                {
                        v.addElement(tokenizer.sval);//on le conserve sous forme de string
                }

                //***Parcours**de**la**ligne*****
                while (tokenizer.nextToken() == StreamTokenizer.TT_WORD) {
                        v.addElement(tokenizer.sval);//on conserve le reste de la premiere ligne sous forme de string
                }

                int n = v.size(); // Now we've got the number of columns! (n interne diff du n general)



                double row[] = new double[n];// tableau pour stocker la premiere ligne

                for (int j = 0; j < n; j++) { // extract the elements of the 1st row
                        try {
                                row[j] = Double.parseDouble(
                                        (String) v.elementAt(j));//retourne l'element position j
                                        //(on enlve (Double))

                                        } catch (
                                        NumberFormatException iop) {
                                row[j] = Double.NaN;

                        }
                }
                //************************reinitialisation*du**vecteur**v**
                v.removeAllElements();
                v.addElement(row); // Start storing rows instead of columns.
                //on ajoute la premiere ligne comme un element de v


                while (tokenizer.ttype != StreamTokenizer.TT_EOF)//test si non fin de fichier
                {
                        while (tokenizer.nextToken() ==
                                StreamTokenizer.TT_EOL)
                                ;//test si pas de ligne vides apres
                        //la premiere ligne pleine deja lue apres test si pas 2 fin de ligne car
                        //on arrive ici apres une fin de ligne dans le if()

                        if (tokenizer.ttype ==
                                StreamTokenizer.TT_WORD) {//si on a un mot donc pas de fin de ligne
                                row =
                                new double[n];//reinitialisation de row
                                try {
                                        row[0] = Double.parseDouble(
                                                tokenizer.sval);//renvoie un double type simple
                                                //prend le premier nombre apres on a un nextToken
                                                } catch (
                                                NumberFormatException iopp) {
                                        row[0] = Double.NaN;
                                }

                                //marche jusque la
                                int ind = 0;

                                while (tokenizer.nextToken() ==
                                        StreamTokenizer.TT_WORD &&
                                        ind < n)//on stocke les colonnes
                                        {
                                        ind++;
                                        try {
                                                row[ind] =
                                                        Double.parseDouble(
                                                        tokenizer.sval)
                                                        ;//ind++ rajoute 1 apres donc ind =1
                                                        } catch (
                                                        NumberFormatException ioppp) {
                                                row[ind] = Double.NaN;
                                                //System.out.println(row[ind]);
                                        }
                                }

                                if (++ind != n || tokenizer.ttype ==
                                        StreamTokenizer.TT_WORD)//on regarde si on a un mot seulement si on pas en fin de ligne
                                        {
                                        int l = v.size();
                                        l++;
                                        throw new java.io.IOException(
                                                "Row " + l + " is too short or too long.");
                                }
                                v.add(row);//comme addelements ajoute des lignes
                        }
                }



                int m = v.size(); // Now we've got the number of rows.

                double[][] M = new double[m][];

                v.copyInto(M); // copy the rows out of the vector



                return new PlsMat(M);

        }
        //************************************************************************

        /* ------------------------
           Private Methods
         * ------------------------ */

        /** Check if size(A) == size(B) **/

        private void checkPlsMatDimensions (PlsMat B) {
                if (B.m != m || B.n != n) {
                        throw new IllegalArgumentException("PlsMat ii dimensions must agree.");
                }
        }

}

